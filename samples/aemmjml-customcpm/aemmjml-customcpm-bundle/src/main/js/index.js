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

import { registerComponent } from 'mjml-core';
import MjText from 'mjml-text';

class CustomComponent extends MjText {
    static endingTag = true;

    static allowedAttributes = Object.assign({}, MjText.allowedAttributes, {
        'greeter': 'string'
    });

    getContent() {
        return 'Hello ' + (this.getAttribute('greeter') || 'World');
    }
}

registerComponent(CustomComponent);