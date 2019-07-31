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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.Self;

import biz.netcentric.cq.tools.aemmjml.MjmlComponent;
import biz.netcentric.cq.tools.aemmjml.components.MjmlSocial;
import biz.netcentric.cq.tools.aemmjml.components.MjmlSocialElement;
import biz.netcentric.cq.tools.aemmjml.helper.AttributeMapping;

@Model(
        adaptables = Resource.class,
        adapters = { MjmlSocialImpl.class, MjmlSocial.class, MjmlComponent.class },
        resourceType = "netcentric/aemmjml/core/component/social"
)
public class MjmlSocialImpl implements MjmlSocial {

    private final Map<String, String> attributes;
    private final List<MjmlSocialElement> elements;

    @Inject
    public MjmlSocialImpl(@Self Resource self,
            @ChildResource(name = "elements", injectionStrategy = OPTIONAL) List<MjmlSocialElement> elements) {
        this.elements = elements != null ? new ArrayList<>(elements) : Collections.emptyList();
        attributes = new AttributeMapping(self)
                .with("align", "center")
                .with("border-radius", "3px")
                .with("color", "#333333")
                .with("container-background-color")
                .with("font-family", "Ubuntu, Helvetica, Arial, sans-serif")
                .with("font-size", "13px")
                .with("font-style")
                .with("font-weight")
                .with("icon-height")
                .with("icon-size", "20px")
                .with("inner-padding", "4px")
                .with("line-height", "22px")
                .with("mode", "horizontal")
                .with("padding", "0px")
                .with("padding-top")
                .with("padding-right")
                .with("padding-bottom")
                .with("padding-left")
                .with("icon-padding", "0px")
                .with("text-padding", "4px 0")
                .with("text-decoration")
                .getAttributes();

    }

    @Override
    @Nonnull
    public Iterable<MjmlSocialElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    @Nonnull
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
