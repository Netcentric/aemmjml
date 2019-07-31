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
package biz.netcentric.cq.tools.aemmjml.impl;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;

import biz.netcentric.cq.tools.aemmjml.MjmlComponent;
import biz.netcentric.cq.tools.aemmjml.MjmlPage;

@Model(
        adaptables = Resource.class,
        adapters = { MjmlPageImpl.class, MjmlPage.class, MjmlComponent.class },
        resourceType = "netcentric/aemmjml/foundation/components/page"
)
public class MjmlPageImpl implements MjmlPage {

    private final MjmlBodyImpl body;

    @Inject
    public MjmlPageImpl(@ChildResource(name = "root") MjmlBodyImpl body) {
        this.body = body;
    }

    @Override
    @Nonnull
    public MjmlBodyImpl getBody() {
        return body;
    }

    @Override
    @Nonnull
    public Map<String, String> getAttributes() {
        return Collections.emptyMap();
    }
}
