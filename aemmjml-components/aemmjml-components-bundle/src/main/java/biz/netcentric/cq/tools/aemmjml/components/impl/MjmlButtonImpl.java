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

import static org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL;

import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import biz.netcentric.cq.tools.aemmjml.MjmlComponent;
import biz.netcentric.cq.tools.aemmjml.components.MjmlButton;
import biz.netcentric.cq.tools.aemmjml.helper.AttributeMapping;

@Model(
        adaptables = Resource.class,
        adapters = { MjmlButtonImpl.class, MjmlButton.class, MjmlComponent.class },
        resourceType = "netcentric/aemmjml/core/components/button"
)
public class MjmlButtonImpl implements MjmlButton {

    private final String buttonText;
    private final Map<String, String> attributes;

    @Inject
    public MjmlButtonImpl(@Self Resource self, @ValueMapValue(name = "buttonText", injectionStrategy = OPTIONAL) String buttonText) {
        this.buttonText = buttonText;
        attributes = new AttributeMapping(self)
                .with("align", "center")
                .with("background-color", "#414141")
                .with("border")
                .with("border-top")
                .with("border-right")
                .with("border-bottom")
                .with("border-left")
                .with("border-radius", "3px")
                .with("color", "#ffffff")
                .with("container-background-color")
                .with("font-family")
                .with("font-size")
                .with("font-style")
                .with("font-weight")
                .with("height")
                .with("href")
                .with("inner-padding", "5px 10px")
                .with("padding", "0px")
                .with("padding-top")
                .with("padding-right")
                .with("padding-bottom")
                .with("padding-left")
                .with("text-align")
                .with("text-decoration")
                .with("text-transform")
                .with("vertical-align")
                .with("width")
                .getAttributes();
    }

    @Override
    @CheckForNull
    public String getButtonText() {
        return buttonText;
    }

    @Override
    @Nonnull
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
