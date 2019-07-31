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
package biz.netcentric.cq.tools.aemmjml;

import java.io.IOException;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import org.osgi.annotation.versioning.ProviderType;

/**
 * A service that can be used to transpile mjml xml to html.
 */
@ProviderType
public interface MjmlProcessor {

    /**
     * Transforms the given input blocking.
     * <p>
     * The calling thread can be interrupted.
     *
     * @param mjml
     * @return html
     * @throws IOException
     * @throws InterruptedException
     */
    @Nonnull
    String transform(@Nonnull String mjml) throws IOException, InterruptedException;

    /**
     * Transforms the given input non-blocking.
     *
     * @param mjml
     * @return html
     */
    @Nonnull
    Future<String> transformAsync(@Nonnull String mjml);

}
