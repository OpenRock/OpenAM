/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015 ForgeRock AS.
 */

define("org/forgerock/openam/ui/admin/views/realms/authentication/ModulesView", [
    "jquery",
    "underscore",
    "org/forgerock/commons/ui/common/main/AbstractView",
    "org/forgerock/openam/ui/admin/views/realms/authentication/AddModuleDialog",
    "org/forgerock/openam/ui/common/util/array/arrayify",
    "org/forgerock/commons/ui/common/main/Configuration",
    "org/forgerock/openam/ui/admin/views/realms/authentication/EditModuleDialog",
    "org/forgerock/openam/ui/admin/models/Form",
    "org/forgerock/openam/ui/admin/utils/FormHelper",
    "org/forgerock/commons/ui/common/components/Messages",
    "org/forgerock/openam/ui/common/util/Promise",
    "org/forgerock/openam/ui/admin/delegates/SMSRealmDelegate",

    // jquery dependencies
    "selectize"
], function ($, _, AbstractView, AddModuleDialog, arrayify, Configuration, EditModuleDialog, Form, FormHelper, Messages,
             Promise, SMSRealmDelegate) {
    function getModuleInfoFromElement (element) {
        return $(element).closest("tr").data();
    }
    function performDeleteModules (realmPath, moduleInfos) {
        return Promise.all(arrayify(moduleInfos).map(function (moduleInfo) {
            return SMSRealmDelegate.authentication.modules.remove(realmPath, moduleInfo.moduleName,
                                                                  moduleInfo.moduleType);
        }));
    }

    var ModulesView = AbstractView.extend({
        template: "templates/admin/views/realms/authentication/ModulesTemplate.html",
        events: {
            "click #addModule": "addModule",
            "change input.select-module": "moduleSelected",
            "click button.delete-module-button": "deleteModule",
            "click #deleteModules": "deleteModules",
            "click .check-before-edit": "editModule"
        },
        partials: [
            "partials/alerts/_Alert.html"
        ],
        data: {},
        addModule: function (event) {
            event.preventDefault();
            var self = this;

            SMSRealmDelegate.authentication.modules.types.all(this.data.realmPath).then(function (data) {
                AddModuleDialog(self.data.realmPath, data.result);
            }, function (response) {
                Messages.addMessage({
                    type: Messages.TYPE_DANGER,
                    response: response
                });
            });
        },
        moduleSelected: function (event) {
            var hasModuleSelected = this.$el.find("input[type=checkbox]").is(":checked"),
                row = $(event.currentTarget).closest("tr"),
                checked = $(event.currentTarget).is(":checked");

            this.$el.find("#deleteModules").prop("disabled", !hasModuleSelected);
            if (checked) {
                row.addClass("selected");
            } else {
                row.removeClass("selected");
            }
        },
        editModule: function (event) {
            event.preventDefault();
            var data = $(event.currentTarget).closest("tr").data(),
                href = event.currentTarget.href;

            EditModuleDialog(data.moduleName, data.moduleChains, href);
        },
        deleteModule: function (event) {
            var self = this,
                element = event.currentTarget,
                moduleInfo = getModuleInfoFromElement(element);

            $(element).prop("disabled", true);

            performDeleteModules(this.data.realmPath, moduleInfo).then(function () {
                self.render(self.data.args);
            }, function (response) {
                Messages.addMessage({
                    type: Messages.TYPE_DANGER,
                    response: response
                });
                $(element).prop("disabled", false);
            });
        },
        deleteModules: function (event) {
            var self = this,
                element = event.currentTarget,
                elements = this.$el.find("input[type=checkbox]:checked"),
                moduleInfos = _(elements).toArray().map(getModuleInfoFromElement).value();

            $(element).prop("disabled", true);

            performDeleteModules(this.data.realmPath, moduleInfos).then(function () {
                self.render([self.data.realmPath]);
            }, function (response) {
                Messages.addMessage({
                    type: Messages.TYPE_DANGER,
                    response: response
                });
                $(element).prop("disabled", false);
            });
        },
        render: function (args, callback) {
            var self = this,
                chainsPromise,
                modulesPromise;

            this.data.args = args;
            this.data.realmPath = args[0];

            chainsPromise = SMSRealmDelegate.authentication.chains.all(this.data.realmPath);
            modulesPromise = SMSRealmDelegate.authentication.modules.all(this.data.realmPath);

            Promise.all([chainsPromise, modulesPromise]).then(function (values) {
                _.each(values[1][0].result, function (module) {
                    _.each(values[0].values.result, function (chain) {
                        _.each(chain.authChainConfiguration, function (link) {
                            if (link.module === module._id) {
                                module.chains = module.chains || [];
                                module.chains.push(chain._id);
                            }
                        });
                    });
                    module.chains = _.uniq(module.chains);
                });

                self.data.formData = values[1][0].result;

                self.parentRender(function () {
                    if (callback) {
                        callback();
                    }
                });
            }, function (errorChains, errorModules) {
                Messages.addMessage({
                    type: Messages.TYPE_DANGER,
                    response: errorChains ? errorChains : errorModules
                });

                self.parentRender(function () {
                    if (callback) {
                        callback();
                    }
                });
            });
        },
        save: function (event) {
            var promise = SMSRealmDelegate.authentication.update(this.data.form.data());

            FormHelper.bindSavePromiseToElement(promise, event.currentTarget);
        }
    });

    return ModulesView;
});
