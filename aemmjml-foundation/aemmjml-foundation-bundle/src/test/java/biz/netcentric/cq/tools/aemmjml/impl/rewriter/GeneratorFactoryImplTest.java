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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.sling.rewriter.Generator;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class GeneratorFactoryImplTest {

    @Test
    public void testHelloWorld() throws IOException, SAXException {
        Generator toTest = new GeneratorFactoryImpl().createGenerator();
        ContentHandler handler = mock(ContentHandler.class);

        doAnswer(invocationOnMock -> {
            Attributes attributes = invocationOnMock.getArgumentAt(3, Attributes.class);
            assertAttributes(attributes, "border-color", "#F45E43");
            return null;
        }).when(handler).startElement(eq(""), eq("mj-divider"), eq("mj-divider"), any());

        doAnswer(invocationOnMock -> {
            Attributes attributes = invocationOnMock.getArgumentAt(3, Attributes.class);
            assertAttributes(attributes, "width", "100px", "src", "https://foo.bar/assets/img/logo-small.png");
            return null;
        }).when(handler).startElement(eq(""), eq("mj-image"), eq("mj-image"), any());

        doAnswer(invocationOnMock -> {
            Attributes attributes = invocationOnMock.getArgumentAt(3, Attributes.class);
            assertAttributes(attributes, "font-size", "20px", "color", "#F45E43", "font-family", "helvetica");
            return null;
        }).when(handler).startElement(eq(""), eq("mj-text"), eq("mj-text"), any());

        try (InputStream mjmlIn = this.getClass().getClassLoader().getResourceAsStream("helloworld.mjml")) {
            assert mjmlIn != null;

            StringWriter buffer = new StringWriter();
            IOUtils.copy(mjmlIn, buffer, StandardCharsets.UTF_8.name());

            toTest.setContentHandler(handler);
            toTest.getWriter().append(buffer.getBuffer());
            toTest.finished();

            verify(handler).startDocument();
            verify(handler).endDocument();

            for (String tag : Arrays.asList("mjml", "mj-body", "mj-section", "mj-divider", "mj-image", "mj-text")) {
                verify(handler).startElement(eq(""), eq(tag), eq(tag), any());
                verify(handler).endElement(eq(""), eq(tag), eq(tag));
            }

            verify(handler).characters(aryEq("Hello World".toCharArray()), eq(0), eq(11));
        }
    }

    @Test
    public void testInvalid() throws IOException, SAXException {
        Generator toTest = new GeneratorFactoryImpl().createGenerator();
        ContentHandler handler = mock(ContentHandler.class);

        doThrow(new SAXException()).when(handler).endDocument();

        try (InputStream mjmlIn = this.getClass().getClassLoader().getResourceAsStream("helloworld.mjml")) {
            assert mjmlIn != null;
            assertThrows(SAXException.class, () -> {
                StringWriter buffer = new StringWriter();
                IOUtils.copy(mjmlIn, buffer, StandardCharsets.UTF_8.name());

                toTest.setContentHandler(handler);
                toTest.getWriter().append(buffer.getBuffer());
                toTest.finished();
            });
        }
    }

    private static void assertAttributes(Attributes attributes, String... expected) {
        assertEquals(attributes.getLength(), Math.floor(expected.length / 2));
        for (int i = 0, c = expected.length; i + 1 < c; i += 2) {
            String name = expected[i];
            String value = expected[i + 1];
            int idx = (int) Math.round(Math.floor(i / 2));
            assertEquals(name, attributes.getQName(idx));
            assertEquals(name, attributes.getLocalName(idx));
            assertEquals(value, attributes.getValue(idx));
        }
    }
}
