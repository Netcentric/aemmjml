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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.rewriter.Generator;
import org.apache.sling.rewriter.GeneratorFactory;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import biz.netcentric.cq.tools.aemmjml.impl.parser.MjmlLexer;
import biz.netcentric.cq.tools.aemmjml.impl.parser.MjmlParser;
import biz.netcentric.cq.tools.aemmjml.impl.parser.MjmlParserBaseListener;

@Component(
        service = GeneratorFactory.class,
        property = {
                Constants.SERVICE_VENDOR + "=Netcentric",
                "pipeline.type=aemmjml-generator"
        }
)
public class GeneratorFactoryImpl implements GeneratorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorFactoryImpl.class);
    private static final int BUFFER_SIZE = 4096;

    @Override
    public Generator createGenerator() {
        return new MjmlGenerator();
    }

    private static String unwrap(String value, char wrappedChar) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }

        if (value.charAt(0) == wrappedChar && value.charAt(value.length() - 1) == wrappedChar) {
            final int startIndex = 0;
            final int endIndex = value.length() - 1;
            return value.substring(startIndex + 1, endIndex);
        } else {
            return value;
        }
    }

    private static Attributes asAttributes(List<? extends MjmlParser.MjmlAttributeContext> parsedAttrs) {
        AttributesImpl attributes = new AttributesImpl();

        if (parsedAttrs == null) {
            parsedAttrs = Collections.emptyList();
        }

        for (MjmlParser.MjmlAttributeContext parsedAttr : parsedAttrs) {
            String attrName = parsedAttr.mjmlAttributeName().getText();
            MjmlParser.MjmlAttributeValueContext parsedAttrValue = parsedAttr.mjmlAttributeValue();
            String attrValue = parsedAttrValue != null ? parsedAttrValue.getText() : null;

            attrValue = unwrap(attrValue, '\'');
            attrValue = unwrap(attrValue, '"');

            attributes.addAttribute(StringUtils.EMPTY, attrName, attrName, StringUtils.EMPTY, attrValue);
        }

        return attributes;
    }

    private class MjmlGenerator implements Generator {

        private final StringWriter buffer = new StringWriter(BUFFER_SIZE);
        private final PrintWriter writer = new PrintWriter(buffer);
        private ContentHandler contentHandler;

        @Override
        public void init(ProcessingContext processingContext, ProcessingComponentConfiguration processingComponentConfiguration) {
            // we don't Have anything to initialise
        }

        @Override
        public void setContentHandler(ContentHandler contentHandler) {
            this.contentHandler = contentHandler;
        }

        @Override
        public PrintWriter getWriter() {
            return writer;
        }

        @Override
        public void finished() throws SAXException {
            try {
                MjmlLexer lexer = new MjmlLexer(CharStreams.fromString(buffer.toString()));
                MjmlParser parser = new MjmlParser(new CommonTokenStream(lexer));
                ParseTreeWalker treeWalker = new ParseTreeWalker();
                treeWalker.walk(new MjmlParserListener(this.contentHandler), parser.mjmlDocument());
            } catch (ParserListenerException ex) {
                LOG.trace("Unwrapping ParserListenerException: " + ex.getMessage(), ex);
                throw ex.getCause();
            } catch (RecognitionException ex) {
                throw new SAXException(ex);
            }
        }

        @Override public void dispose() {
            // we don't have anything to clean up, buffers allocated with this class will implicitly reclaimed
        }
    }

    private class ParserListenerException extends RuntimeException {

        ParserListenerException(SAXException ex) {
            super(ex);
        }

        @Override
        public synchronized SAXException getCause() {
            return (SAXException) super.getCause();
        }
    }

    private class MjmlParserListener extends MjmlParserBaseListener {

        private final ContentHandler contentHandler;
        private final StringBuilder buffer = new StringBuilder();

        MjmlParserListener(ContentHandler contentHandler) {
            this.contentHandler = contentHandler;
        }

        @Override
        public void enterMjmlDocument(MjmlParser.MjmlDocumentContext ctx) {
            try {
                contentHandler.startDocument();
            } catch (SAXException ex) {
                throw new ParserListenerException(ex);
            }
        }

        @Override
        public void exitMjmlDocument(MjmlParser.MjmlDocumentContext ctx) {
            try {
                contentHandler.endDocument();
            } catch (SAXException ex) {
                throw new ParserListenerException(ex);
            }
        }

        @Override
        public void enterMjmlStartElement(MjmlParser.MjmlStartElementContext ctx) {
            this.flush();
            try {
                String tagName = ctx.mjmlTagName().getText();
                Attributes attributes = asAttributes(ctx.mjmlAttribute());
                contentHandler.startElement(StringUtils.EMPTY, tagName, tagName, attributes);
            } catch (SAXException ex) {
                throw new ParserListenerException(ex);
            }
        }

        @Override
        public void enterMjmlStartCloseElement(MjmlParser.MjmlStartCloseElementContext ctx) {
            this.flush();
            try {
                String tagName = ctx.mjmlTagName().getText();
                Attributes attributes = asAttributes(ctx.mjmlAttribute());
                contentHandler.startElement(StringUtils.EMPTY, tagName, tagName, attributes);
                // we reduce the <tagName/> to <tagName>
            } catch (SAXException ex) {
                throw new ParserListenerException(ex);
            }
        }

        @Override
        public void enterMjmlCloseElement(MjmlParser.MjmlCloseElementContext ctx) {
            this.flush();
            try {
                String tagName = ctx.mjmlTagName().getText();
                contentHandler.endElement(StringUtils.EMPTY, tagName, tagName);
            } catch (SAXException ex) {
                throw new ParserListenerException(ex);
            }
        }

        @Override
        public void enterMjmlChardata(MjmlParser.MjmlChardataContext ctx) {
            spool(ctx);
        }

        @Override
        public void enterMjmlSpoolElement(MjmlParser.MjmlSpoolElementContext ctx) {
            spool(ctx);
        }

        private void spool(RuleContext ctx) {
            spool(ctx.getText());
        }

        private void spool(CharSequence content) {
            buffer.append(content);
        }

        private void flush() {
            if (buffer.length() == 0) {
                return;
            }

            try {
                char[] chars = new char[buffer.length()];
                buffer.getChars(0, buffer.length(), chars, 0);
                this.contentHandler.characters(chars, 0, chars.length);
                this.buffer.setLength(0);
            } catch (SAXException ex) {
                throw new ParserListenerException(ex);
            }
        }

    }
}
