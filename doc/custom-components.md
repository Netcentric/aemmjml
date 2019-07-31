<!--
    (C) Copyright 2019 Netcentric, a Cognizant Digital Business.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->

Custom mjml components
===================================

To implement custom mjml components the following has to be provided to the framework:

- a Javascript implementation of the component
- a [CustomComponentProvider](../aemmjml-foundation/aemmjml-foundation-bundle/src/main/java/biz/netcentric/cq/tools/aemmjml/spi/CustomComponentProvider.java) implementation  

An example can be found in [aemmjml-customcpm](../samples/aemmjml-customcpm).

It is recommended to combine the Javascript and the Java implementation into a bundle and install it together with the AEM component as a content package. 

```
my-component/
|- my-component-bundle/
|-- src/main/
|--- java/
|---- MyComponentProvider.java
|--- js/ 
|---- my-component.js
|---- package.json
|-- pom.xml
|- my-component-bundle/
|-- pom.xml
```
 
First of all create a nodejs project for the custom component and add the necessary dependencies to implement mjml components. Note that it is required to use webpack to bundle the component into a single executable file.

**package.json**
```
{
    "name": "custom-component",
    "dependencies": {
        "mjml": "4.4.0"
    },
    "devDependencies": {
        "webpack": "4.4.1",
        "webpack-cli": "2.0.13"
    }
}

```

Additionally to that with using webpack `aemmjml-foundation-bundle` provides a [dll](https://webpack.js.org/plugins/dll-plugin/) that custom components can refer to. It already packs all of the mjml dependencies and so does not require to bundle them again. The most convenient way to use that dll is to use maven to unpack the dll manifest and include it in the `webpack.config.js`. Note it is required to use the dll as it will make sure that all components are registered in the same context.

**pom.xml**
```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <id>unpack-manifest</id>
            <phase>initialize</phase>
            <goals>
                <goal>unpack</goal>
            </goals>
            <configuration>
                <artifactItems>
                    <artifactItem>
                        <groupId>biz.netcentric.cq.tools.aemmjml</groupId>
                        <artifactId>aemmjml-foundation-bundle</artifactId>
                        <version>1.0.0</version>
                        <classifier>mainfest</classifier>
                        <type>zip</type>
                    </artifactItem>
                </artifactItems>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**webpack.config.js**
```
plugins: [
    new webpack.DllReferencePlugin({
        manifest: '../../../target/dependency/mjmljs-mainfest.json',
        sourceType: 'commonjs2'
    })
]
```  