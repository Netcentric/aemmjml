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

/**
 * Implementing {@link LinkExternalizer} allows to modify the implementation used by the rewriter pipeline to externalise urls without
 * the need to provide a full implementation of a {@link org.apache.sling.rewriter.TransformerFactory}.
 * <p>
 * By default {@link DefaultLinkExternalizer} will be used.
 */
public interface LinkExternalizer {

    /**
     * Implementations should return an absolute link for the given relative one. If this is not possible it may return the given link as
     * is.
     * <p>
     * This method is only called with non-null relative links.
     *
     * @param resourceResolver
     * @param link
     * @return An absolute link for the given link
     */
    @Nonnull String externalize(@Nonnull ResourceResolver resourceResolver, @Nonnull String link);

}
