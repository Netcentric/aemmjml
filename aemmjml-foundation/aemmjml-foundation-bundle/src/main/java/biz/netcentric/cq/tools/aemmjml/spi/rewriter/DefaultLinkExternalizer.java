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
package biz.netcentric.cq.tools.aemmjml.spi.rewriter;

import javax.annotation.Nonnull;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.commons.Externalizer;

@Component(
        service = { DefaultLinkExternalizer.class, LinkExternalizer.class },
        property = {
                Constants.SERVICE_VENDOR + "=Netcentric"
        }
)
@ProviderType
public final class DefaultLinkExternalizer implements LinkExternalizer {

    @Reference
    private Externalizer externalizer;

    @Override
    @Nonnull
    public String externalize(@Nonnull ResourceResolver resourceResolver, @Nonnull String link) {
        return externalizer.publishLink(resourceResolver, link);
    }
}
