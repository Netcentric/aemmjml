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

Build and Development
===================================

AEM MJML uses the standard tooling common to the Adobe Experience Manager ecosystem. After completing the below prerequisites, you can simply clone and build it using

```shell script
git clone https://github.com/Netcentric/aemmjml.git .
mvn clean install
```   

There are maven profiles available to deploy the binaries to a running AEM instance using the `com.day.jcr.vault:content-package-maven-plugin`. 

```shell script
mvn clean install -PauthoInstallPackage
```

Details about how to specify the host, port, user or password can be found in the root [pom.xml](https://github.com/Netcentric/aemmjml/blob/develop/pom.xml#L68).

Prerequisites
-------------

In order to build and package AEM MJML you need to provide a maven repository hosting the nodejs artifacts, which are packaged together with the implementation. We recommend to use [Maven NodeJS Proxy](https://maven-nodejs-proxy.pvtool.org/) for this but please follow the instructions outlined in the repository and never use it without having a caching proxy in front of it. If you are using Nexus user, you can simply create a [proxy repository](https://help.sonatype.com/learning/repository-manager-3/first-time-installation-and-setup/lesson-2%3A-proxy-and-hosted-maven-repositories#Lesson2:ProxyandHostedMavenRepositories-MavenProxyRepository) of the aforementioned.   
