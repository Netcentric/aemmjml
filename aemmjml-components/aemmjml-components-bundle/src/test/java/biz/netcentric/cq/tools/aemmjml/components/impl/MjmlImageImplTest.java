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
