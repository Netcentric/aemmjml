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
package biz.netcentric.cq.tools.aemmjml.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
public class AttributeMappingTest {

    private final AemContext context = new AemContext();

    @Test
    public void testAttributeMapping() {
        Resource given = context.create().resource("/foo/bar",
                "mjmlWidth", "100px",
                "mjmlBackgroundColor", "red",
                "mjmlContainerBackgroundColor", "yellow");
        Map<String, String> actual = new AttributeMapping(given)
                .with("width")
                .with("background-color")
                .with("container-background-color")
                .with("background-url")
                .getAttributes();
        // expected
        assertEquals("100px", actual.get("width"));
        assertEquals("red", actual.get("background-color"));
        assertEquals("yellow", actual.get("container-background-color"));
        assertNull(actual.get("background-url"));
    }

    @Test
    public void testAttributeMappingDefaults() {
        Resource given = context.create().resource("/foo/bar", "mjmlWidth", "100px");
        Map<String, String> actual = new AttributeMapping(given)
                .with("width", "200px")
                .with("background-color", "red")
                .with("container-background-color", "yellow")
                .with("background-url")
                .getAttributes();
        // expected
        assertEquals("100px", actual.get("width"));
        assertEquals("red", actual.get("background-color"));
        assertEquals("yellow", actual.get("container-background-color"));
        assertNull(actual.get("background-url"));
    }
}
