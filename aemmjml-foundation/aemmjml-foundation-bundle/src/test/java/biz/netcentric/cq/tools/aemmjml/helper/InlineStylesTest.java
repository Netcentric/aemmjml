package biz.netcentric.cq.tools.aemmjml.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.apache.sling.api.scripting.SlingBindings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.ui.clientlibs.ClientLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibraryManager;
import com.adobe.granite.ui.clientlibs.LibraryType;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
public class InlineStylesTest {

    private static final Logger LOG = LoggerFactory.getLogger(InlineStylesTest.class);

    private final AemContext context = new AemContext();
    private final Bindings bindings = new SimpleBindings();

    @Mock
    private HtmlLibraryManager libraryManager;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        bindings.put(SlingBindings.SLING, context.slingScriptHelper());
        bindings.put(SlingBindings.LOG, LOG);

        context.registerService(HtmlLibraryManager.class, libraryManager);
    }

    @Test
    public void testLibrariesMissing() {
        List<String> collectedLibraries = new ArrayList<>();
        InlineStyles inlineStyles = new InlineStyles();

        bindings.put(InlineStyles.BINDINGS_CATEGORIES, new String[] { "cat1", "cat2" });

        inlineStyles.init(bindings);
        inlineStyles.getLibraries().forEach(collectedLibraries::add);
        assertEquals(0, collectedLibraries.size());
    }

    @Test
    public void testLibrariesExist() throws IOException  {
        List<String> collectedLibraries = new ArrayList<>();
        InlineStyles inlineStyles = new InlineStyles();

        HtmlLibrary htmlLib1 = mock(HtmlLibrary.class);
        ClientLibrary lib1 = mock(ClientLibrary.class);
        ClientLibrary lib2 = mock(ClientLibrary.class);
        List<ClientLibrary> libs = Arrays.asList(lib1, lib2);

        when(lib1.getPath()).thenReturn("/foo/bar");
        when(lib2.getPath()).thenReturn("/bar/foo");

        when(libraryManager.getLibrary(eq(LibraryType.CSS), eq("/foo/bar"))).thenReturn(htmlLib1);
        when(htmlLib1.getInputStream()).thenReturn(new ByteArrayInputStream("lib1".getBytes(StandardCharsets.UTF_8)));

        bindings.put(InlineStyles.BINDINGS_CATEGORIES, new String[] { "cat1", "cat2", "cat3" });
        when(libraryManager.getLibraries(any(), eq(LibraryType.CSS), eq(true), eq(true))).thenReturn(libs);

        inlineStyles.init(bindings);
        inlineStyles.getLibraries().forEach(collectedLibraries::add);
        assertEquals(2, collectedLibraries.size());
        assertEquals("lib1", collectedLibraries.get(0));
        assertEquals("", collectedLibraries.get(1));
    }
}
