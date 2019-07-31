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

import javax.script.Bindings;

import org.apache.sling.scripting.sightly.pojo.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AllowedComponentsCompat implements Use {

    private static final Logger LOG = LoggerFactory.getLogger(AllowedComponentsCompat.class);

    private boolean knowsAllowedComponents;

    @Override
    public void init(Bindings bindings) {
        ClassLoader cl = this.getClass().getClassLoader();
        try {
            cl.loadClass("com.day.cq.wcm.foundation.model.AllowedComponents");
            knowsAllowedComponents = true;
        } catch (ClassNotFoundException ex) {
            LOG.trace("Cannot load AllowedComponents, use deprecated API", ex);
            knowsAllowedComponents = false;
        }
    }

    public boolean knowsAllowedComponents() {
        return knowsAllowedComponents;
    }
}
