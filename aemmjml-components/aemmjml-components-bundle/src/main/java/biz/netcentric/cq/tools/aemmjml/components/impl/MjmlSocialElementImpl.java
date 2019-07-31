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
import static org.apache.sling.models.annotations.injectorspecific.InjectionStrategy.OPTIONAL;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import biz.netcentric.cq.tools.aemmjml.components.MjmlSocialElement;

@Model(
        adaptables = Resource.class,
        adapters = { MjmlSocialElementImpl.class, MjmlSocialElement.class }
)
public class MjmlSocialElementImpl implements MjmlSocialElement {

    private final Map<String, String> attributes = new HashMap<>();
    private final String label;

    @Inject
    public MjmlSocialElementImpl(
            @ValueMapValue(name = "url") String url,
            @ValueMapValue(name = "label") String label,
            @ValueMapValue(name = "icon", injectionStrategy = OPTIONAL) String icon
    ) {
        this.label = label;
        putNotBlank(attributes, "href", url);
        putNotBlank(attributes, "src", icon);
    }

    @Override
    @Nonnull
    public String getLabel() {
        return label;
    }

    @Override
    @Nonnull
    public Map<String, String> getAttributes() {
        return attributes;
    }
}