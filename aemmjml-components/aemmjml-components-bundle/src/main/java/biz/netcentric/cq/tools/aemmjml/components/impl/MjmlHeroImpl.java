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

import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import biz.netcentric.cq.tools.aemmjml.MjmlComponent;
import biz.netcentric.cq.tools.aemmjml.MjmlContainer;
import biz.netcentric.cq.tools.aemmjml.helper.AttributeMapping;

@Model(
        adaptables = Resource.class,
        adapters = { MjmlHeroImpl.class, MjmlContainer.class, MjmlComponent.class },
        resourceType = "netcentric/aemmjml/core/components/hero"
)
public class MjmlHeroImpl implements MjmlContainer {

    private final Map<String, String> attributes;

    @Inject
    public MjmlHeroImpl(@Self Resource self) {
        attributes = new AttributeMapping(self)
                .with("background-color")
                .with("background-height")
                .with("background-position", "center center")
                .with("background-url")
                .with("background-width")
                .with("height")
                .with("mode", "fluid-height")
                .with("padding", "0px")
                .with("padding-top")
                .with("padding-right")
                .with("padding-bottom")
                .with("padding-left")
                .with("vertical-align", "top")
                .with("width")
                .getAttributes();
    }

    @Override
    public boolean isDropzoneOutsideTable() {
        return false;
    }

    @Override
    @Nonnull
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
