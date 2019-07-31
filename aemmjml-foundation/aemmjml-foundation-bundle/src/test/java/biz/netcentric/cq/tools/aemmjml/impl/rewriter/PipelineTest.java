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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.commons.threads.ThreadPoolManager;
import org.apache.sling.commons.threads.impl.DefaultThreadPoolManager;
import org.apache.sling.rewriter.Generator;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Serializer;
import org.apache.sling.rewriter.Transformer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xml.sax.SAXException;

import com.day.cq.commons.Externalizer;

import biz.netcentric.cq.tools.aemmjml.impl.MjmlProcessorImpl;
import biz.netcentric.cq.tools.aemmjml.spi.rewriter.DefaultLinkExternalizer;
import io.wcm.testing.mock.aem.junit5.AemContext;

public class PipelineTest {

    private static final AemContext CONTEXT = new AemContext();
    private static final GeneratorFactoryImpl GENERATOR_FACTORY = new GeneratorFactoryImpl();
    private static final SerializerFactoryImpl SERIALIZER_FACTORY = new SerializerFactoryImpl();
    private static final LinkTransformerFactoryImpl LINK_TRANSFORMER_FACTORY = new LinkTransformerFactoryImpl();

    @Mock
    private ProcessingContext processingContext;
    @Mock
    private SlingHttpServletRequest request;
    @Mock
    private SlingHttpServletResponse response;
    @Mock
    private ProcessingComponentConfiguration componentConfiguration;

    private Map<String, Object> configurationProperties = new HashMap<>();
    private Generator generator;
    private Transformer transformer;
    private Serializer serializer;
    private StringWriter buffer = new StringWriter();

    @BeforeAll
    public static void setup() {
        Externalizer externalizer = mock(Externalizer.class);
        when(externalizer.publishLink(any(), anyString())).thenAnswer(invocation -> {
            String link = invocation.getArgumentAt(1, String.class);
            assertNotNull(link); // null links should not be externalized at all
            return "https://foo.bar" + link;
        });

        CONTEXT.registerService(ThreadPoolManager.class, new DefaultThreadPoolManager(CONTEXT.bundleContext(), new Hashtable<>()));
        CONTEXT.registerInjectActivateService(new MjmlProcessorImpl());
        CONTEXT.registerService(Externalizer.class, externalizer);
        CONTEXT.registerInjectActivateService(new DefaultLinkExternalizer());
        CONTEXT.registerInjectActivateService(GENERATOR_FACTORY);
        CONTEXT.registerInjectActivateService(SERIALIZER_FACTORY);
        CONTEXT.registerInjectActivateService(LINK_TRANSFORMER_FACTORY, "enabled.selectors", "campaign.content");
    }

    @BeforeEach
    public void init() throws IOException {
        initMocks(this);

        PrintWriter output = new PrintWriter(buffer);
        RequestPathInfo pathInfo = mock(RequestPathInfo.class);
        when(pathInfo.getSelectorString()).thenReturn("campaign.content");

        when(processingContext.getRequest()).thenReturn(request);
        when(processingContext.getResponse()).thenReturn(response);
        when(processingContext.getWriter()).thenReturn(output);

        when(request.getResourceResolver()).thenReturn(CONTEXT.resourceResolver());
        when(request.getRequestPathInfo()).thenReturn(pathInfo);
        when(request.getMethod()).thenReturn("get");

        when(componentConfiguration.getConfiguration()).thenReturn(new ValueMapDecorator(configurationProperties));

        configurationProperties.put("tags", new String[] {
                "mj-image:src",
                "*:href"
        });

        generator = GENERATOR_FACTORY.createGenerator();
        generator.init(processingContext, componentConfiguration);
        serializer = SERIALIZER_FACTORY.createSerializer();
        serializer.init(processingContext, componentConfiguration);
        transformer = LINK_TRANSFORMER_FACTORY.createTransformer();
        transformer.init(processingContext, componentConfiguration);

        generator.setContentHandler(transformer);
        transformer.setContentHandler(serializer);
    }

    @Test
    public void testHelloWorld() throws SAXException, IOException, InterruptedException {
        testFiles("helloworld.mjml", "helloworld.html");
    }

    @Test
    public void testFullWorld() throws InterruptedException, SAXException, IOException {
        testFiles("fullworld.mjml", "fullworld.html");
    }

    private void testFiles(String given, String expected) throws SAXException, IOException, InterruptedException {
        try (InputStream mjmlIn = this.getClass().getClassLoader().getResourceAsStream(given)) {
            assert mjmlIn != null;
            IOUtils.copy(mjmlIn, generator.getWriter(), StandardCharsets.UTF_8);
            generator.finished();
        }

        try (InputStream htmlIn = this.getClass().getClassLoader().getResourceAsStream(expected)) {
            assert htmlIn != null;

            StringWriter htmlBuffer = new StringWriter();
            IOUtils.copy(htmlIn, htmlBuffer, StandardCharsets.UTF_8.name());

            assertEquals(htmlBuffer.toString(), buffer.toString());
        }
    }
}
