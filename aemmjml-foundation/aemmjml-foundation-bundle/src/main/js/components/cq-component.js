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

import { initComponent, BodyComponent } from 'mjml-core';
import { trimEnd } from 'lodash';

export class CqComponent extends BodyComponent {

    getChildComponents() {
        if (!this.childComponents) {
            this.childComponents = this.props.children.map(child => initComponent({
                    name: child.tagName,
                    initialDatas: {
                        ...child,
                        attributes: {
                            ...child.attributes,
                        },
                        context: this.getChildContext(),
                        props: {
                            ...this.props
                        }
                    },
                }))
                .filter(childComponent => !!childComponent);
        }
        return this.childComponents;
    }

    withContentChild(doer) {
        const children = this.getChildComponents();
        if (children && children.length > 0) {
            return doer.call(this, children[0]);
        }
    }

    withCqChild(doer) {
        const children = this.getChildComponents();
        if (children && children.length > 1) {
            return doer.call(this, children[children.length - 1]);
        }
    }

    getStyles() {
        return this.withContentChild(child => child.getStyles());
    }

    getAttribute(name) {
        return this.withContentChild(child => child.getAttribute(name));
    }

    render() {
        const classes = this.attributes.class || ''
        return this.withContentChild(child => {
            const isContainer = !!classes.match(/(?<=\s|^)container(?=\s|$)/);

            if (isContainer) {
                // for containers we inject a comment as child
                const identifier = `<!--${Math.round(Math.random() * 1000000).toString()}-->`;

                child.props.children.push({
                    absoluteFilePath: '/dev/null',
                    attributes: {},
                    children: [],
                    content: identifier,
                    globalAttributes: {},
                    includedIn: [],
                    line: -1,
                    tagName: 'mj-raw'
                });

                const rendered = child.render();
                const renderedCq = this.withCqChild(child => child.render());

                return rendered.replace(identifier, renderedCq || '')
            } else {
                // for all other elements we have to place the cq child at the end
                const rendered = child.render();

                let output = '';
                let renderedTrim = trimEnd(rendered);

                while (renderedTrim.endsWith('-->')) { // ends with an conditional wrapper
                    renderedTrim = trimEnd(renderedTrim.substr(0, renderedTrim.lastIndexOf('<!--')));
                }

                const closingTagPos = renderedTrim.lastIndexOf('</');

                if (closingTagPos < 0) {
                    output += rendered;
                } else {
                    output += rendered.substr(0, closingTagPos);
                }

                const renderedCq = this.withCqChild(child => child.render());

                if (renderedCq) {
                    output += renderedCq;
                }

                if (closingTagPos >= 0) {
                    output += rendered.substr(closingTagPos);
                }

                return output;
            }
        });
    }
}
