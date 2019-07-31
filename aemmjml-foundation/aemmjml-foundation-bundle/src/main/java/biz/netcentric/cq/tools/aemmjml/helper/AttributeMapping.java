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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.day.cq.wcm.api.policies.ContentPolicy;
import com.day.cq.wcm.api.policies.ContentPolicyManager;

public final class AttributeMapping {

    private static final String PREFIX = "mjml";

    private final ValueMap policyProperties;
    private final ValueMap resourceProperties;
    private final Map<String, String> attributes = new HashMap<>();

    public AttributeMapping(@Nonnull Resource self) {
        resourceProperties = self.getValueMap();
        policyProperties = Optional.ofNullable(self.getResourceResolver().adaptTo(ContentPolicyManager.class))
                .map(contentPolicyManager -> contentPolicyManager.getPolicy(self))
                .map(ContentPolicy::getProperties)
                .orElseGet(() -> new ValueMapDecorator(Collections.emptyMap()));
    }

    @Nonnull
    public AttributeMapping with(@Nonnull String name) {
        return with(name, null);
    }

    @Nonnull
    public AttributeMapping with(@Nonnull String name, @Nullable String defaultValue) {
        String propertyName = toPropertyName(name);
        String value = defaultValue != null ?
                policyProperties.get(propertyName, defaultValue) :
                policyProperties.get(propertyName, String.class);
        value = value != null ?
                resourceProperties.get(propertyName, value) :
                resourceProperties.get(propertyName, String.class);

        putNotBlank(attributes, name, value);

        return this;
    }

    @Nonnull
    public Map<String, String> getAttributes() {
        return new HashMap<>(attributes);
    }

    public static void putNotBlank(@Nonnull Map<String, String> target, @Nonnull String key, CharSequence value) {
        if (StringUtils.isNotBlank(value)) {
            target.put(key, String.valueOf(value));
        }
    }

    @Nonnull
    private static String toPropertyName(@Nonnull String name) {
        if (name.isEmpty()) {
            return name;
        }

        StringBuilder builder = new StringBuilder(name.length() + PREFIX.length());
        builder.append(PREFIX);

        for (int i = 0; i < name.length(); i++) {
            char character = name.charAt(i);
            if (i == 0) {
                builder.append(Character.toUpperCase(character));
            } else if (character == '-') {
                builder.append(Character.toUpperCase(name.charAt(++i)));
            } else {
                builder.append(character);
            }
        }

        return builder.toString();
    }
}
