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

/**
 * Overlay of the onSucess behaviour of th design dialogs.
 *
 * Since 6.4 the entire design page is not reloaded once a dialog was successfully submitted. For mjml pages this is necessary though.
 * Unfortunately cq:editListeners are not taken into account, so the behaviour was overwritten here.
 */
;(function ($, ns, channel, window, undefined) {
    "use strict";
    // The original implementation calls the EditableAction.REFRESH directly without involving any of listeners defined in the editConfig
    var _super = ns.design.Dialog.prototype.onSuccess;

    ns.design.Dialog.prototype.onSuccess = function(arg1) {
        let rootEditable = arg1.editable;

        while(rootEditable && rootEditable.type !== "netcentric/aemmjml/foundation/components/body") {
            rootEditable = ns.editables.getParent(rootEditable);
        }

        if (rootEditable) {
            // we are on a MJML page with an foundation body, so refresh the page instead of refreshing the component
            ns.ContentFrame.reload(true);
        } else {
            _super.apply(this, arguments);
        }
    };

}(jQuery, Granite.author, jQuery(document), this));
