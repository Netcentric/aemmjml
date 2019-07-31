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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.rewriter.DefaultTransformer;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.rewriter.TransformerFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import biz.netcentric.cq.tools.aemmjml.spi.rewriter.LinkExternalizer;

@Component(
        service = TransformerFactory.class,
        property = {
                Constants.SERVICE_VENDOR + "=Netcentric",
                "pipeline.type=aemmjml-link-transformer"
        }
)
@Designate(ocd = LinkTransformerFactoryImpl.Config.class)
public class LinkTransformerFactoryImpl implements TransformerFactory {

    @ObjectClassDefinition(name = "AEM MJML - MJML Link Transformer") @interface Config {
        @AttributeDefinition(name = "Enabled Selectors", description = "If any of the given selectors matches, the transformer will be "
                + "enabled. To restrict for multiple selectors, concat them with a dot as you would do in the URL. If not set at all the"
                + "transformer will not be enabled.")
        String[] enabled_selectors() default { "campaign.content" };
    }

    private static final Logger LOG = LoggerFactory.getLogger(LinkTransformerFactoryImpl.class);
    private static final Pattern SCRIPTLET_PATTERN = Pattern.compile("(<%[=@].*?%>)");
    private static final int CONFIG_TAGS_PARTS = 2;

    @Reference
    private LinkExternalizer externalizer;

    private String[] enabledSelectors;

    @Activate
    protected void activate(Config config) {
        this.enabledSelectors = config.enabled_selectors();
    }

    @Override
    public Transformer createTransformer() {
        return new MjmlLinkTransformer();
    }

    private boolean isEnabledForRequest(SlingHttpServletRequest request) {
        if (enabledSelectors == null) {
            return false;
        }

        RequestPathInfo pathInfo = request.getRequestPathInfo();
        String selectors = StringUtils.defaultIfEmpty(pathInfo.getSelectorString(), StringUtils.EMPTY);

        for (String necessarySelectors : enabledSelectors) {
            if (StringUtils.equals(necessarySelectors, selectors)) {
                return true;
            }
        }

        return false;
    }

    private static String createPlaceholder(String link, int counter) {
        StringBuilder placeholder = new StringBuilder();
        placeholder.append('_');
        placeholder.append(counter);

        while (link.contains(placeholder)) {
            placeholder.insert(0, '_');
        }

        return placeholder.toString();
    }

    private static String escapeScriptlets(String link, Map<String, String> replacements) {
        Matcher matcher = SCRIPTLET_PATTERN.matcher(link);
        int counter = 0;

        // remove all scriptlets from the given link, so that we don't break them in further processing
        while (matcher.find()) {
            String scriptlet = matcher.group(1);
            String placeholder = createPlaceholder(link, counter++);
            link = link.replaceFirst(Pattern.quote(scriptlet), placeholder);
            replacements.put(placeholder, scriptlet);
        }

        return link;
    }

    private static String unescapeScriptlets(String link, Map<String, String> replacements) {
        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            link = link.replaceFirst(replacement.getKey(), replacement.getValue());
        }

        return link;
    }

    private static String decodeUrlQuietly(String link, String encoding) {
        try {
            // only the path is remaining
            return URLDecoder.decode(link, encoding);
        } catch (UnsupportedEncodingException ex) {
            LOG.debug("Encoding not supported: {}. Continuing without decoding URL.", encoding, ex);
            return link;
        }
    }

    private class MjmlLinkTransformer extends DefaultTransformer {

        private String encoding;
        private Map<String, String[]> tags = new HashMap<>();
        private ResourceResolver resourceResolver;
        private boolean isEnabled;

        @Override
        public void init(ProcessingContext context, ProcessingComponentConfiguration config) throws IOException {
            super.init(context, config);
            SlingHttpServletRequest request = context.getRequest();
            isEnabled = isEnabledForRequest(request);
            LOG.debug("LinkTransformer {} for URL {}", isEnabled ? "enabled" : "disabled", request.getRequestURI());

            if (isEnabled) {
                resourceResolver = request.getResourceResolver();
                encoding = StringUtils.defaultIfEmpty(context.getResponse().getCharacterEncoding(), StandardCharsets.UTF_8.name());
                String[] givenTags = config.getConfiguration().get("tags", new String[0]);

                for (String tag : givenTags) {
                    String[] parts = tag.split(":", CONFIG_TAGS_PARTS);

                    if (parts.length != CONFIG_TAGS_PARTS || StringUtils.isEmpty(parts[1])) {
                        LOG.debug("Invalid definition: {}", tag);
                        continue;
                    }

                    tags.put(parts[0], parts[1].split(";"));
                }
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            Attributes newAtts;
            if (this.isEnabled) {
                newAtts = processElement(qName, atts);
            } else {
                newAtts = atts;
            }

            super.startElement(uri, localName, qName, newAtts);
        }

        private Attributes processElement(String qName, Attributes atts) {
            AttributesImpl mutableAttribs = null;

            for (Map.Entry<String, String[]> tagConfig : this.tags.entrySet()) {
                String tag = tagConfig.getKey();

                if (!tag.equals(qName) && !"*".equals(tag)) {
                    continue;
                }

                for (String attrib : tagConfig.getValue()) {
                    int index = atts.getIndex(attrib);

                    if (index < 0) {
                        continue;
                    }

                    String link = atts.getValue(index);

                    if (shouldExternalize(link)) {
                        mutableAttribs = mutableAttribs != null ? mutableAttribs : new AttributesImpl(atts);
                        mutableAttribs.setAttribute(index, atts.getURI(index), atts.getLocalName(index), atts.getQName(index),
                                atts.getType(index), externalize(link));
                    }
                }
            }

            return mutableAttribs != null ? mutableAttribs : atts;
        }

        private boolean shouldExternalize(String link) {
            // we only externalise links starting with a /
            return link != null && link.startsWith("/");
        }

        private String externalize(String givenLink) {
            if (!shouldExternalize(givenLink)) {
                return givenLink;
            }

            String link = StringEscapeUtils.unescapeHtml(givenLink);
            //  escape any scriptlets by replacing them with placeholders
            Map<String, String> replacements = new HashMap<>();
            link = escapeScriptlets(link, replacements);
            // remove any fragment (query or hash)
            String frag = org.apache.commons.lang.StringUtils.EMPTY;
            int fragPos = link.indexOf('?');

            if (fragPos > 0) {
                frag = link.substring(fragPos);
                link = link.substring(0, fragPos);
            } else {
                fragPos = link.indexOf('#');
                if (fragPos > 0) {
                    frag = link.substring(fragPos);
                    link = link.substring(0, fragPos);
                }
            }
            // decode and externalize link using upstream apis
            link = decodeUrlQuietly(link, encoding);
            link = externalizer.externalize(resourceResolver, link) + frag;
            link = StringEscapeUtils.escapeHtml(link);
            // restore the scriptlets
            link = unescapeScriptlets(link, replacements);

            return link;
        }
    }
}
