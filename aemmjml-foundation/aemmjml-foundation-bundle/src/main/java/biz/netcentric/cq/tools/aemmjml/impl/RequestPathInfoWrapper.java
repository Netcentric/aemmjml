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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ConsumerType;
import org.osgi.annotation.versioning.ProviderType;

public abstract class RequestPathInfoWrapper implements RequestPathInfo {

    private final RequestPathInfo wrapped;

    protected RequestPathInfoWrapper(@Nonnull RequestPathInfo wrapped) {
        this.wrapped = wrapped;
    }

    @Nonnull @Override public String getResourcePath() {
        return wrapped.getResourcePath();
    }

    @CheckForNull @Override public String getExtension() {
        return wrapped.getExtension();
    }

    @CheckForNull @Override public String getSelectorString() {
        return wrapped.getSelectorString();
    }

    @Nonnull @Override public String[] getSelectors() {
        return wrapped.getSelectors();
    }

    @CheckForNull @Override public String getSuffix() {
        return wrapped.getSuffix();
    }

    @CheckForNull @Override public Resource getSuffixResource() {
        return wrapped.getSuffixResource();
    }
}
