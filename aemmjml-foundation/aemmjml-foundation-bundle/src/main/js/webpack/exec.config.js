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

const path = require('path');
const webpack = require('webpack');
const base = require('./base.config.js');

module.exports = Object.assign({}, base, {
  entry: './index.js',
  output: {
    path: process.env.PROJECT_BASEDIR,
    filename: 'target/generated-sources/mjmljs/mjml.js'
  },
  plugins: [
      new webpack.DllReferencePlugin({manifest: `${process.env.PROJECT_BASEDIR}/target/generated-sources/mjmljs-mainfest.json`, sourceType: 'commonjs2'}),
  ]
});