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

import mjml2html from 'mjml';

require('./components/index.js');

const fs = require('fs');
const requireCustomComponent = typeof __webpack_require__ === "function" ? __non_webpack_require__ : require;
const mjmlOptions = {
    minify: true,
    minifyOptions: {
        trimCustomFragments: true,
    }
}

fs.readdirSync('./')
    .filter(file => file.endsWith('.cc.js'))
    .forEach(file => { try { requireCustomComponent('./' + file) } catch(_) {} });

let buffer = '';

process.stdin
    .setEncoding('utf8')
    .on('data', chunk => {
        buffer += chunk

        if (buffer.indexOf("</mjml>") < 0) return;

        process.stdout.write(mjml2html(buffer, mjmlOptions).html + '\n')
        buffer = '';
    })
    .on('close', () => process.exit(0));
