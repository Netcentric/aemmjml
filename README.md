mjml for Adobe Experience Manager
====================================================

mjml is a markup language designed to reduce the pain of coding responsive email templates. It rolls up years of experience in HTML email design and abstracts the whole layer of complexity related to responsive email design. ([Read more](https://mjml.io))

With mjml for AEM (AEM MJML) we provide an implementation that enables users of AEM to render components in mjml and deliver them on-the-fly as html. This can be used by both developers to build email templates and complex components, or by marketeers using the well know editing experience of AEM and the standard mjml components. 

Requirements & Compatibility
----------------------------

AEM MJML is compatible to AEM 6.3+ running  on MacOSX, Linux and Windows. It requires the [WCM Core Components](https://github.com/adobe/aem-core-wcm-components) to be installed at least in Version [2.0.4](https://github.com/adobe/aem-core-wcm-components/releases/tag/core.wcm.components.reactor-2.0.4).

**Known issues:**

- **6.4/6.5**: The new location of legacy cloud settings (`/libs/settings/cloudsettings/legacy`) is not supported by the `campaign_newsletterpage` yet. The default configuration for the contexthub needs to be copied to `/etc/cloudsettings/default` to enable personalisation.


Installation
------------

The latest release can be downloaded as an [aggregated package](https://github.com/netcentric/aemmjml/releases) and installed using AEM Package Manager.

For more information about the Package Manager please have a look at [How to Work With Packages](https://helpx.adobe.com/experience-manager/6-4/sites/administering/using/package-manager.html) documentation page.

Project structure
-----------------

AEM MJML consists of 2 main modules:

- `aemmjml-foundation`
- `aemmjml-components`

The `aemmjml-foundation` module implements the core of AEM MJML. It provides the foundation components (page, body and container as base for other parsys-like components) and the [Sling Rewriter Pipeline](https://sling.apache.org/documentation/bundles/output-rewriting-pipelines-org-apache-sling-rewriter.html) components that are used to transform mjml to html.

`aemmjml-components` on the other hand provides ready-to-use mjml standard components, policies and a template type. They act as reference implementation and might help to compose own components.

Further Documentation
---------------------

- [Implementing custom mjml components](doc/custom-components.md)

License
-------

(see [LICENSE.txt](LICENSE.txt) for full license details)

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