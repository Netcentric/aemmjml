/*
 * (C) Copyright 2019 Netcentric, a Cognizant Digital Business.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.netcentric.cq.tools.aemmjml.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.sling.commons.threads.ThreadPool;
import org.apache.sling.commons.threads.ThreadPoolManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.netcentric.cq.tools.aemmjml.MjmlProcessor;
import biz.netcentric.cq.tools.aemmjml.spi.CustomComponentProvider;

@Component(
        service = MjmlProcessor.class,
        property = {
                Constants.SERVICE_VENDOR + "=Netcentric"
        },
        immediate = true
)
@Designate(ocd = MjmlProcessorImpl.Config.class)
public class MjmlProcessorImpl implements MjmlProcessor {

    @ObjectClassDefinition(name = "AEM MJML - MJML Processor") @interface Config {
        @AttributeDefinition(name = "ThreadPool name", description = "The name of the thread pool to be used. If it doesn't exist yet it "
                + "will be created using the default configuration.")
        String threadpool_name() default "mjml";
    }

    private enum NodejsPackage {
        NODEJS_MACOSX("nodejs-binaries-12.16.3-darwin-x64.tar.gz", "node-v12.16.3-darwin-x64/bin/node"),
        NODEJS_LINUX64("nodejs-binaries-12.16.3-linux-x64.tar.gz", "node-v12.16.3-linux-x64/bin/node"),
        NODEJS_WIN64("nodejs-binaries-12.16.3-win-x64.zip", "node-v12.16.3-win-x64/node.exe"); // untested

        private final String fileName;
        private final String executable;

        NodejsPackage(String fileName, String executable) {
            this.fileName = fileName;
            this.executable = executable;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(MjmlProcessorImpl.class);

    @Reference
    private ThreadPoolManager threadPoolManager;
    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    private List<CustomComponentProvider> customComponentProviders = new LinkedList<>();

    private final Map<Thread, Process> processes = new ConcurrentHashMap<>();
    private BundleContext bundleContext;
    private ThreadPool threadPool;
    private File nodejs;
    private File mjmljs;
    private File dataRoot;
    private final ReadWriteLock executionLock = new ReentrantReadWriteLock();

    @Activate
    protected void activate(BundleContext bundleContext, Config config) throws IOException, ArchiveException {
        this.bundleContext = bundleContext;
        this.threadPool = threadPoolManager.get(config.threadpool_name());

        final NodejsPackage bp;

        if (SystemUtils.IS_OS_MAC_OSX) {
            bp = NodejsPackage.NODEJS_MACOSX;
        } else if (SystemUtils.IS_OS_LINUX) {
            bp = NodejsPackage.NODEJS_LINUX64;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            bp = NodejsPackage.NODEJS_WIN64;
        } else {
            throw new UnsupportedOperationException("Operating system " + SystemUtils.OS_NAME + " not supported");
        }

        this.nodejs = unpackNodejs(bp);
        this.mjmljs = unpackMjmljs();

        File customComponentFolder = mjmljs.getParentFile();
        for (CustomComponentProvider customComponentProvider : customComponentProviders) {
            String name = getCustomComponentName(customComponentProvider);
            try (InputStream customComponentImpl = customComponentProvider.openJavascriptFile()) {
                if (customComponentImpl == null) {
                    LOG.warn("Could not load custom component implementation from: {}", customComponentProvider.getClass().getName());
                    continue;
                }

                File customComponentJs = new File(customComponentFolder, name);

                if (customComponentJs.exists() && !customComponentJs.delete()) {
                    throw new IOException("Custom component implementation already exists and cannot be removed.");
                }

                copyToFile(customComponentImpl, customComponentJs);
            } catch (IOException ex) {
                throw new IOException("Failed to unpack custom component implementation: " + name, ex);
            }
        }
    }

    @Deactivate
    protected void deactivate() throws IOException {
        this.threadPoolManager.release(this.threadPool);
        destroyAllProcesses();

        if (this.dataRoot != null) {
            FileUtils.deleteDirectory(this.dataRoot);
        }
    }

    private void destroyAllProcesses() {
        executionLock.writeLock().lock();
        try {
            for (Process process : processes.values()) {
                try {
                    process.destroyForcibly().waitFor();
                } catch (InterruptedException ex) {
                    LOG.debug("Interrupted termination of {}: {}", process, process.isAlive(), ex);
                }
            }
            processes.clear();
        } finally {
            executionLock.writeLock().unlock();
        }
    }

    @Override
    @Nonnull
    public String transform(@Nonnull String mjml) throws IOException, InterruptedException {
        try {
            return transformAsync(mjml).get();
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();

            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new IOException("Execution failed.", ex);
            }
        }
    }

    @Override
    @Nonnull
    public Future<String> transformAsync(@Nonnull String mjml) {
        return this.threadPool.submit(new MjmlCall(mjml));
    }

    private File getDataFolder(@Nullable String to) throws IOException {
        if (this.dataRoot == null) {
            this.dataRoot = bundleContext.getDataFile("temp").getParentFile();
        }
        return getDataFolder(this.dataRoot, to);
    }

    private File getDataFolder(@Nonnull File base, @Nullable String to) throws IOException {
        File targetFolder = base;

        if (Objects.nonNull(to)) {
            targetFolder = new File(targetFolder, to);
        }

        if (targetFolder.exists()) {
            FileUtils.deleteDirectory(targetFolder);
        }

        if (!targetFolder.mkdirs()) {
            throw new IOException("Failed to created target folder for NodejsPackage:" + targetFolder.getAbsolutePath());
        }

        return targetFolder;
    }

    private File unpackMjmljs() throws IOException, ArchiveException {
        final File targetFolder = unpackArchive("mjmljs.zip", "mjmljs");
        final File executable = new File(targetFolder, "mjml.js");

        if (!executable.exists()) {
            throw new IOException("Archive didn't contain executable: " + executable);
        }

        return executable;
    }

    private File unpackNodejs(NodejsPackage bp) throws IOException, ArchiveException {
        final File targetFolder = unpackArchive(bp.fileName);
        final File executable = new File(targetFolder, bp.executable);

        if (!executable.exists()) {
            throw new IOException("Archive didn't contain executable: " + bp.executable);
        } else if (!executable.canExecute() && !executable.setExecutable(true, true)) {
            throw new IOException("Failed to make binary executable for owner.");
        }

        return executable;
    }

    private File unpackArchive(String from) throws IOException, ArchiveException {
        return unpackArchive(from, null);
    }

    private File unpackArchive(String from, String to) throws IOException, ArchiveException {
        File targetFolder = getDataFolder(to);
        ArchiveEntry entry;

        try (ArchiveInputStream archiveIn = openArchive(from)) {
            if (archiveIn == null) {
                throw new IOException("Failed to unpack: " + from);
            }

            while ((entry = archiveIn.getNextEntry()) != null) {
                unpackArchiveEntry(targetFolder, entry, archiveIn);
            }

            return targetFolder;
        }
    }

    private static void unpackArchiveEntry(File targetFolder, ArchiveEntry entry, ArchiveInputStream stream) throws IOException {
        File entryFile = new File(targetFolder, entry.getName());
        if (entry.isDirectory()) {
            if (!entryFile.mkdirs()) {
                throw new IOException("Failed to extract directory entry: " + entry.getName());
            }
        } else {
            if (entryFile.getParentFile().exists() || entryFile.getParentFile().mkdirs()) {
                copyToFile(stream, entryFile);
            } else {
                throw new IOException("Failed to extract file entry: " + entry.getName());
            }
        }
    }

    private static void copyDetect(String data, OutputStream to, String pattern, Charset charset) throws IOException {
        try (Reader from = new PatternDetectingReader(new StringReader(data), pattern)) {
            IOUtils.copy(from, to, charset);
        }
    }

    private static void copyDetect(InputStream data, Writer to, String pattern, Charset charset) throws IOException {
        try (Reader from = new PatternDetectingReader(new InputStreamReader(new CloseShieldInputStream(data), charset), pattern)) {
            IOUtils.copy(from, to);
        }
    }

    private static ArchiveInputStream openArchive(String archiveName) throws IOException, ArchiveException {
        InputStream resource = MjmlProcessorImpl.class.getClassLoader().getResourceAsStream(archiveName);
        if (resource == null) {
            return null;
        }

        final InputStream decompressedIn = StringUtils.endsWith(archiveName, ".gz") ? new GzipCompressorInputStream(resource) : resource;
        final InputStream bufferedDecompressedIn = new BufferedInputStream(decompressedIn);

        return new ArchiveStreamFactory().createArchiveInputStream(bufferedDecompressedIn);
    }

    private static void copyToFile(InputStream in, File dest) throws IOException {
        try (OutputStream out = FileUtils.openOutputStream(dest)) {
            IOUtils.copy(in, out);
        }
    }

    private static String getCustomComponentName(CustomComponentProvider provider) {
        return StringUtils.replaceChars(provider.getClass().getName(), File.separatorChar, '_') + '@' + provider.hashCode() + ".cc.js";
    }

    private class MjmlCall implements Callable<String> {

        private final String mjml;

        MjmlCall(String mjml) {
            this.mjml = mjml;
        }

        @Override
        public String call() throws Exception {
            executionLock.readLock().lock();
            Process process = null;

            try {
                Thread currentThread = Thread.currentThread();
                process = processes.get(currentThread);

                if (process != null && !process.isAlive()) {
                    LOG.debug("MJML process is not alive anymore: {}", process.exitValue());
                    process = null;
                }
                if (process == null) {
                    process = new ProcessBuilder(nodejs.getAbsolutePath(), mjmljs.getAbsolutePath())
                            .directory(mjmljs.getParentFile())
                            .start();
                    processes.put(currentThread, process);
                }

                // mjmljs will block until </mjml> was found, to prevent that blocking the java side we have to detect the same pattern and
                // throw if we don't.
                final StringWriter buffer = new StringWriter(mjml.length() * 6);
                copyDetect(mjml, process.getOutputStream(), "</mjml>", UTF_8);
                copyDetect(process.getInputStream(), buffer, "</html>", UTF_8);
                return buffer.toString();
            } catch (IOException ex) {
                // we close the process so it gets restarted in a fresh state
                if (process != null) {
                    process.destroyForcibly().waitFor();
                }
                throw ex;
            } finally {
                executionLock.readLock().unlock();
            }
        }
    }
}
