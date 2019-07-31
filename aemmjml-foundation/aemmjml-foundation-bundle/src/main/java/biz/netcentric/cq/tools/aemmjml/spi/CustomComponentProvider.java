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
package biz.netcentric.cq.tools.aemmjml.spi;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.CheckForNull;

/**
 * Implementations of {@link CustomComponentProvider} are called when the mjml processor is activated. They provide javascript that is
 * executed before the forked nodejs process is accepting markup to be transformed. This allows the implementation to provide and register
 * custom components.
 */
public interface CustomComponentProvider {

    /**
     * Returns an {@link InputStream} of the data of a javascript file. The stream will be closed by the caller.
     * <p>
     * The implementation might return null.
     *
     * @return the data of the javascript file to load
     * @throws IOException
     */
    @CheckForNull
    InputStream openJavascriptFile() throws IOException;
}
