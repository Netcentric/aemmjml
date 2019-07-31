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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Serializer;
import org.apache.sling.rewriter.SerializerFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import biz.netcentric.cq.tools.aemmjml.MjmlProcessor;

@Component(
        service = SerializerFactory.class,
        property = {
                Constants.SERVICE_VENDOR + "=Netcentric",
                "pipeline.type=aemmjml-serializer"
        }
)
public class SerializerFactoryImpl implements SerializerFactory {

    public static final String REQUEST_RAW_ATTR = SerializerFactoryImpl.class + "@requestRaw";

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private MjmlProcessor mjmlProcessor;

    @Override
    public Serializer createSerializer() {
        return new MjmlSerializer();
    }

    private class MjmlSerializer extends DefaultHandler implements Serializer {

        private PrintWriter output;
        private StringWriter buffer;
        private PrintWriter responseWriter;
        private SlingHttpServletResponse response;
        private SlingHttpServletRequest request;

        @Override
        public void init(ProcessingContext processingContext, ProcessingComponentConfiguration processingComponentConfiguration)
                throws IOException {
            responseWriter = processingContext.getWriter();
            response = processingContext.getResponse();
            request = processingContext.getRequest();

            if (mjmlProcessor == null) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                output = responseWriter;
            } else if (Boolean.TRUE.equals(processingContext.getRequest().getAttribute(SerializerFactoryImpl.REQUEST_RAW_ATTR))) {
                output = responseWriter;
            } else {
                buffer = new StringWriter();
                output = new PrintWriter(buffer);
            }
        }

        @Override
        public void dispose() {
            // noop
        }

        @Override
        public void endDocument() throws SAXException {
            if (buffer != null) {
                try {
                    // content has been buffered for transformation
                    if (StringUtils.equalsIgnoreCase(request.getMethod(), "get")
                            && response.getStatus() < HttpServletResponse.SC_MULTIPLE_CHOICES) {
                        // only if we have a non-error status we transform otherwise we will run in IOExceptions
                        responseWriter.write(mjmlProcessor.transform(buffer.toString()));
                    } else {
                        responseWriter.write(buffer.toString());
                    }
                    responseWriter.flush();
                } catch (IOException | InterruptedException ex) {
                    throw new SAXException("Failed to transform document: " + ex.getMessage(), ex);
                }
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            int attrsLength = attrs.getLength();

            output.write('<');
            output.write(qName);

            if (attrsLength > 0) {
                output.write(' ');
            }

            for (int i = 0; i < attrsLength; i++) {
                output.write(attrs.getQName(i));

                String value = attrs.getValue(i);

                if (value != null) {
                    output.write('=');
                    output.write('"');
                    output.write(value);
                    output.write('"');
                }

                if (i + 1 < attrsLength) {
                    output.write(' ');
                }
            }
            output.write('>');

        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            output.write('<');
            output.write('/');
            output.write(qName);
            output.write('>');

        }

        @Override
        public void characters(char[] ch, int start, int length) {
            output.write(ch, start, length);
        }
    }
}
