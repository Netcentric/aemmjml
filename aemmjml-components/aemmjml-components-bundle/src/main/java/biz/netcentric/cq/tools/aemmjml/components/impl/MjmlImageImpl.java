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
package biz.netcentric.cq.tools.aemmjml.components.impl;

import static biz.netcentric.cq.tools.aemmjml.helper.AttributeMapping.putNotBlank;

import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.adobe.cq.wcm.core.components.models.Image;

import biz.netcentric.cq.tools.aemmjml.MjmlComponent;
import biz.netcentric.cq.tools.aemmjml.helper.AttributeMapping;

@Model(
        adaptables = SlingHttpServletRequest.class,
        adapters = { MjmlImageImpl.class, MjmlComponent.class },
        resourceType = "netcentric/aemmjml/core/components/image"
)
public class MjmlImageImpl implements MjmlComponent {

    private final Map<String, String> attributes;

    @Inject
    public MjmlImageImpl(@Self @Via("resource") Resource self, @Self Image image) {
        attributes = new AttributeMapping(self)
                .with("align", "center")
                .with("border")
                .with("border-radius")
                .with("container-background-color")
                .with("fluid-on-mobile")
                .with("height", "auto")
                .with("padding", "0px")
                .with("padding-top")
                .with("padding-right")
                .with("padding-bottom")
                .with("padding-left")
                .with("width")
                .getAttributes();

        putNotBlank(attributes, "href", image.getLink());
        putNotBlank(attributes, "src", image.getSrc());
        putNotBlank(attributes, "title", image.getTitle());
        putNotBlank(attributes, "alt", image.getAlt());

        String srcUriTemplate = image.getSrcUriTemplate();
        if (StringUtils.isNotBlank(srcUriTemplate)) {
            int[] imageWidths = image.getWidths();
            Arrays.sort(imageWidths);
            StringBuilder srcset = new StringBuilder(imageWidths.length * srcUriTemplate.length());

            for (int i = 0; i < imageWidths.length; i++) {
                srcset.append(srcUriTemplate.replace("{.width}", "." + imageWidths[i]));
                srcset.append(' ');
                srcset.append(imageWidths[i]);
                srcset.append('w');
                if (i + 1 < imageWidths.length) {
                    srcset.append(',');
                    srcset.append(' ');
                }
            }

            putNotBlank(attributes, "srcset", srcset);
        }
    }

    @Override
    @Nonnull
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
