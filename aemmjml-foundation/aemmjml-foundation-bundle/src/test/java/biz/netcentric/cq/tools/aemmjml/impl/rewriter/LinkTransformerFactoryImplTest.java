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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.day.cq.commons.Externalizer;

import biz.netcentric.cq.tools.aemmjml.spi.rewriter.DefaultLinkExternalizer;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
public class LinkTransformerFactoryImplTest {

    private final LinkTransformerFactoryImpl toTest = new LinkTransformerFactoryImpl();
    private final AemContext aemContext = new AemContext();

    @Mock
    private ProcessingContext processingContext;
    @Mock
    private SlingHttpServletRequest request;
    @Mock
    private SlingHttpServletResponse response;
    @Mock
    private ContentHandler contentHandler;
    @Mock
    private Externalizer externalizer;
    @Mock
    private ProcessingComponentConfiguration componentConfiguration;

    private Map<String, Object> configurationProperties = new HashMap<>();

    @BeforeEach
    public void setup() {
        initMocks(this);

        when(processingContext.getRequest()).thenReturn(request);
        when(processingContext.getResponse()).thenReturn(response);
        when(request.getResourceResolver()).thenReturn(aemContext.resourceResolver());
        when(componentConfiguration.getConfiguration()).thenReturn(new ValueMapDecorator(configurationProperties));

        when(externalizer.publishLink(any(), anyString())).thenAnswer(invocation -> {
            String link = invocation.getArgumentAt(1, String.class);
            assertNotNull(link); // null links should not be externalized at all
            return "https://foo.bar" + link;
        });

        aemContext.registerService(Externalizer.class, externalizer);
        aemContext.registerInjectActivateService(new DefaultLinkExternalizer());
    }

    private void setupPathInfo(String selectorString) {
        RequestPathInfo pathInfo = mock(RequestPathInfo.class);
        when(pathInfo.getSelectorString()).thenReturn(selectorString);
        when(request.getRequestPathInfo()).thenReturn(pathInfo);
    }

    private void setupTags(String... tags) {
        configurationProperties.put("tags", tags);
    }

    @Test
    public void testNotEnabledOnRequest() throws SAXException, IOException {
        setupPathInfo("bar.foo");
        aemContext.registerInjectActivateService(toTest, "enabled.selectors", new String[] { "foo.bar" });
        testTransform("/foo/bar.html", "/foo/bar.html");
    }

    @Test
    public void testEnabledOnRequestNoTags() throws SAXException, IOException {
        setupPathInfo("campaign.content");
        aemContext.registerInjectActivateService(toTest);
        testTransform("/foo/bar.html", "/foo/bar.html");
    }

    @Test
    public void testEnabledOnRequestInvalidTags() throws SAXException, IOException {
        setupPathInfo("campaign.content");
        setupTags("a=>href");
        aemContext.registerInjectActivateService(toTest);
        testTransform("/foo/bar.html", "/foo/bar.html");
    }

    @Test
    public void testEnabledOnRequestNoneMatchingTag() throws SAXException, IOException {
        setupPathInfo("campaign.content");
        setupTags("mj-image:src;href", "*:background-image");
        aemContext.registerInjectActivateService(toTest);
        testTransform("/foo/bar.html", "/foo/bar.html");
    }

    @Test
    public void testEnabledOnRequestMatchingTag() throws SAXException, IOException {
        setupPathInfo("campaign.content");
        setupTags("a:href");
        aemContext.registerInjectActivateService(toTest);
        testTransform("/foo/bar.html", "https://foo.bar/foo/bar.html");
    }

    @Test
    public void testEnabledOnRequestMatchingWildcardTag() throws SAXException, IOException {
        setupPathInfo("campaign.content");
        setupTags("*:href");
        aemContext.registerInjectActivateService(toTest);
        testTransform("/foo/bar.html", "https://foo.bar/foo/bar.html");
    }

    @Test
    public void testEnabledOnRequestMatchingWildcardTagScriptlets() throws SAXException, IOException {
        setupPathInfo("campaign.content");
        setupTags("*:href");
        aemContext.registerInjectActivateService(toTest);
        testTransform(
                "/foo/bar.html?lang=<%=recipient.language%>&amp;<%@ include view='campaignParameter' %>",
                "https://foo.bar/foo/bar.html?lang=<%=recipient.language%>&amp;<%@ include view='campaignParameter' %>",
                "/foo/bar/<%=recipient.language%>.html?<%@ include view='campaignParameter' %>",
                "https://foo.bar/foo/bar/<%=recipient.language%>.html?<%@ include view='campaignParameter' %>",
                "/foo/bar/<%=recipient.language%>.html#<%@ include view='campaignParameter' %>",
                "https://foo.bar/foo/bar/<%=recipient.language%>.html#<%@ include view='campaignParameter' %>");
    }

    private void testTransform(String... params) throws IOException, SAXException {
        for (int i = 0; i + 1 < params.length; i += 2) {
            String given = params[i];
            String expected = params[i + 1];

            doAnswer(invocationOnMock -> {
                Attributes attributes = invocationOnMock.getArgumentAt(3, Attributes.class);
                assertEquals(expected, attributes.getValue("href"));
                return null;
            }).when(contentHandler).startElement(any(), any(), any(), any());

            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute("", "href", "href", "", given);

            Transformer transformer = toTest.createTransformer();
            transformer.setContentHandler(contentHandler);
            transformer.init(processingContext, componentConfiguration);
            transformer.startElement("", "a", "a", attributes);
        }

        verify(contentHandler, times(Math.round(params.length / 2))).startElement(any(), any(), any(), any());
    }
}
