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
package biz.netcentric.cq.tools.aemmjml.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.Bindings;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.scripting.sightly.pojo.Use;
import org.slf4j.Logger;

import com.adobe.granite.ui.clientlibs.ClientLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibraryManager;
import com.adobe.granite.ui.clientlibs.LibraryType;

public final class InlineStyles implements Use {

    public static final String BINDINGS_CATEGORIES = "categories";

    private static final int BUFFER_SIZE = 4096;

    private String encoding;
    private Logger log;
    private HtmlLibraryManager libraryManager;
    private List<String> categories;

    @Override
    public void init(Bindings bindings) {
        SlingScriptHelper scriptHelper = (SlingScriptHelper) bindings.get(SlingBindings.SLING);
        encoding = StringUtils.defaultIfEmpty(scriptHelper.getResponse().getCharacterEncoding(), StandardCharsets.UTF_8.name());
        libraryManager = scriptHelper.getService(HtmlLibraryManager.class);
        log = (Logger) bindings.get(SlingBindings.LOG);

        Object categoriesObject = bindings.get(BINDINGS_CATEGORIES);

        if (categoriesObject != null) {
            Object[] givenCategories;
            if (!(categoriesObject instanceof Object[])) {
                if (categoriesObject instanceof String) {
                    givenCategories = ((String) categoriesObject).split(",");
                } else {
                    givenCategories = new Object[] { categoriesObject };
                }
            } else {
                givenCategories = (Object[]) categoriesObject;
            }

            this.categories = Arrays.stream(givenCategories)
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .filter(StringUtils::isNoneEmpty)
                    .collect(Collectors.toList());
        } else {
            this.categories = Collections.emptyList();
        }
    }

    public Iterable<String> getLibraries() {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(BUFFER_SIZE);
        final Map<String, ClientLibrary> htmlLibrariesToInline = new LinkedHashMap<>();
        final Collection<ClientLibrary> libraries = libraryManager
                .getLibraries(categories.toArray(new String[0]), LibraryType.CSS, true, true);

        for (ClientLibrary library : libraries) {
            htmlLibrariesToInline.putAll(library.getDependencies(true));
            htmlLibrariesToInline.put(library.getPath(), library);
        }

        return () -> new Iterator<String>() {

            private Iterator<ClientLibrary> libraries = htmlLibrariesToInline.values().iterator();

            @Override public boolean hasNext() {
                return libraries.hasNext();
            }

            @Override public String next() {
                ClientLibrary library = libraries.next();
                HtmlLibrary htmlLibrary = libraryManager.getLibrary(LibraryType.CSS, library.getPath());

                if (htmlLibrary != null) {
                    try {
                        buffer.reset();
                        IOUtils.copy(htmlLibrary.getInputStream(), buffer);
                        return new String(buffer.toByteArray(), encoding);
                    } catch (IOException ex) {
                        log.warn("Failed to inline {}", library.getPath(), ex);
                    }
                }

                return StringUtils.EMPTY;
            }
        };
    }
}
