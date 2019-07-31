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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.sling.commons.threads.ModifiableThreadPoolConfig;
import org.apache.sling.commons.threads.ThreadPool;
import org.apache.sling.commons.threads.ThreadPoolManager;
import org.apache.sling.commons.threads.impl.DefaultThreadPoolManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.ImmutableMap;

import biz.netcentric.cq.tools.aemmjml.spi.CustomComponentProvider;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
public class MjmlProcessorImplTest {

    private final AemContext context = new AemContext();
    private final MjmlProcessorImpl toTest = new MjmlProcessorImpl();
    private ThreadPoolManager tpm = new DefaultThreadPoolManager(context.bundleContext(), new Hashtable<>());
    private Map<String, Object> toTestProps;

    @BeforeEach
    public void setup() {
        context.registerService(ThreadPoolManager.class, tpm);
        // we work with a threadpool of size 1 to test concurrency issues better
        ModifiableThreadPoolConfig config = new ModifiableThreadPoolConfig();
        config.setMinPoolSize(1);
        config.setMaxPoolSize(1);
        ThreadPool tp = tpm.create(config, "default");
        toTestProps = ImmutableMap.of("threadpool.name", tp.getName());
    }

    @Test
    public void testHelloWorld() throws IOException, InterruptedException {
        context.registerInjectActivateService(toTest, toTestProps);
        testFiles("helloworld.mjml", "helloworld.html");
    }

    @Test
    public void testHelloWorldNoWs() throws IOException, InterruptedException {
        context.registerInjectActivateService(toTest, toTestProps);
        testFiles("helloworld-nows.mjml", "helloworld.html");
    }

    @Test
    public void testHelloWorldRecovery() throws InterruptedException {
        context.registerInjectActivateService(toTest, toTestProps);

        ThreadPool tp = tpm.get("test");
        Semaphore gate = new Semaphore(1);
        gate.acquire();

        tp.submit(() -> {
            try {
                assertThrows(IOException.class, () -> testFiles("helloworld-broken.mjml", "helloworld.html"));
            } finally {
                gate.release();
            }
            return null;
        });

        assertTrue(gate.tryAcquire(5, TimeUnit.SECONDS));

        tp.submit(() -> {
            try {
                testFiles("helloworld-nows.mjml", "helloworld.html");
            } finally {
                gate.release();
            }
            return null;
        });

        assertTrue(gate.tryAcquire(5, TimeUnit.SECONDS));

        tpm.release(tp);
    }

    @Test
    public void testCustomComponent() throws IOException, InterruptedException {
        context.registerService(CustomComponentProvider.class,
                () -> this.getClass().getClassLoader().getResourceAsStream("test-component.js"));
        context.registerInjectActivateService(toTest, toTestProps);
        testFiles("test-component.mjml", "test-component.html");
    }

    private void testFiles(String given, String expected) throws IOException, InterruptedException {
        String html;

        try (InputStream mjmlIn = this.getClass().getClassLoader().getResourceAsStream(given)) {
            assert mjmlIn != null;

            StringWriter buffer = new StringWriter();
            IOUtils.copy(mjmlIn, buffer, StandardCharsets.UTF_8.name());
            html = toTest.transform(buffer.toString());
        }

        try (InputStream htmlIn = this.getClass().getClassLoader().getResourceAsStream(expected)) {
            assert htmlIn != null;

            StringWriter buffer = new StringWriter();
            IOUtils.copy(htmlIn, buffer, StandardCharsets.UTF_8.name());

            assertEquals(buffer.toString(), html);
        }
    }
}
