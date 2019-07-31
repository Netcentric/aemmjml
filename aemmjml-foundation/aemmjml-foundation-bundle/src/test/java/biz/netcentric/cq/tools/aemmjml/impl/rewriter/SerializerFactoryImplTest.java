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
package biz.netcentric.cq.tools.aemmjml.impl.rewriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.commons.threads.ThreadPoolManager;
import org.apache.sling.commons.threads.impl.DefaultThreadPoolManager;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.google.common.collect.ImmutableMap;

import biz.netcentric.cq.tools.aemmjml.impl.MjmlProcessorImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
public class SerializerFactoryImplTest {

    public static final AemContext CONTEXT = new AemContext();
    public static final SerializerFactoryImpl TO_TEST = new SerializerFactoryImpl();

    @BeforeAll
    public static void setup() {
        CONTEXT.registerService(ThreadPoolManager.class, new DefaultThreadPoolManager(CONTEXT.bundleContext(), new Hashtable<>()));
        CONTEXT.registerInjectActivateService(new MjmlProcessorImpl());
        CONTEXT.registerInjectActivateService(TO_TEST);
    }

    @Test
    public void testHelloWorldHtml() throws IOException, SAXException, InterruptedException {
        testFile("helloworld.html", Collections.emptyMap());
    }

    @Test
    public void testHelloWorldMjml() throws IOException, SAXException, InterruptedException {
        testFile("helloworld-nows.mjml", ImmutableMap.of(SerializerFactoryImpl.REQUEST_RAW_ATTR, Boolean.TRUE));
    }

    private void testFile(String expectedResource, Map<String, Object> requestAttrs)
            throws IOException, SAXException, InterruptedException {
        String expected;

        try (InputStream htmlIn = this.getClass().getClassLoader().getResourceAsStream(expectedResource)) {
            assert htmlIn != null;

            StringWriter buffer = new StringWriter();
            IOUtils.copy(htmlIn, buffer, StandardCharsets.UTF_8.name());

            expected = buffer.toString();
        }

        Semaphore gate = new Semaphore(1);
        gate.acquire();

        StringWriter buffer = new StringWriter();
        ProcessingContext context = mock(ProcessingContext.class);
        SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
        SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);

        requestAttrs.forEach((key, value) -> when(request.getAttribute(eq(key))).thenReturn(value));
        when(request.getMethod()).thenReturn("get");
        when(response.getStatus()).thenReturn(200);
        when(context.getWriter()).thenReturn(new PrintWriter(buffer));
        when(context.getRequest()).thenReturn(request);
        when(context.getResponse()).thenReturn(response);

        Serializer serializer = TO_TEST.createSerializer();
        serializer.init(context, mock(ProcessingComponentConfiguration.class));
        serializer.startDocument();
        startElement(serializer, "mjml");
        startElement(serializer, "mj-body");
        startElement(serializer, "mj-section");
        startElement(serializer, "mj-column");
        startElement(serializer, "mj-image", ImmutableMap.of("width", "100px", "src", "https://foo.bar/assets/img/logo-small.png"));
        endElement(serializer, "mj-image");
        startElement(serializer, "mj-divider", ImmutableMap.of("border-color", "#F45E43"));
        endElement(serializer, "mj-divider");
        startElement(serializer, "mj-text", ImmutableMap.of("font-size", "20px", "color", "#F45E43", "font-family", "helvetica"));
        characters(serializer, "Hello World");
        endElement(serializer, "mj-text");
        endElement(serializer, "mj-column");
        endElement(serializer, "mj-section");
        endElement(serializer, "mj-body");
        endElement(serializer, "mjml");
        serializer.endDocument();

        String actual = buffer.toString();
        assertEquals(expected, actual);
    }

    private static void startElement(Serializer serializer, String name) throws SAXException {
        startElement(serializer, name, Collections.emptyMap());
    }

    private static void startElement(Serializer serializer, String name, Map<String, String> attrs) throws SAXException {
        AttributesImpl attributes = new AttributesImpl();
        for (Map.Entry<String, String> e : attrs.entrySet()) {
            attributes.addAttribute("", e.getKey(), e.getKey(), "", e.getValue());
        }
        serializer.startElement("", name, name, attributes);
    }

    private static void endElement(Serializer serializer, String name) throws SAXException {
        serializer.endElement("", name, name);
    }

    private static void characters(Serializer serializer, String text) throws SAXException {
        char[] chars = text.toCharArray();
        serializer.characters(chars, 0, chars.length);
    }
}
