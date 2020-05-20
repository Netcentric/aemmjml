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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.adobe.cq.wcm.core.components.models.Image;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
public class MjmlImageImplTest {

    private static final AemContext CONTEXT = new AemContext();

    @Test
    public void testSrcSet() {
        Image image = mock(Image.class);
        when(image.getWidths()).thenReturn(new int[] { 1920, 640, 800 });
        when(image.getSrcUriTemplate()).thenReturn("/content/path/to/image.coreimg{.width}.jpg");
        Resource resouce = CONTEXT.create().resource("/content/path/to/image");

        MjmlImageImpl mjmlImage = new MjmlImageImpl(resouce, image);
        String srcset = mjmlImage.getAttributes().get("srcset");
        assertNotNull(srcset);
        assertEquals("/content/path/to/image.coreimg.640.jpg 640w, "
                        + "/content/path/to/image.coreimg.800.jpg 800w, "
                        + "/content/path/to/image.coreimg.1920.jpg 1920w",
                srcset);
    }
}
