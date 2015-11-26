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

define("org/forgerock/openam/ui/admin/views/realms/CreateUpdateRealmDialog", [
    "jquery",
    "underscore",
    "org/forgerock/commons/ui/common/main/AbstractView",
    "org/forgerock/commons/ui/common/components/BootstrapDialog",
    "org/forgerock/openam/ui/admin/models/Form",
    "org/forgerock/openam/ui/admin/delegates/SMSGlobalDelegate"
], function ($, _, AbstractView, BootstrapDialog, Form, SMSGlobalDelegate) {
    var CreateUpdateRealmDialog = AbstractView.extend({
        /**
         * CreateUpdateRealmDialog.show(options);
         * The options object can contain up to 3 parameters, realmPath, allRealmPaths, callback.
         * If you are editing an exsisting realm then pass in the option.realmPath. It is used by this
         * view to determine if the realm is a new one or not.
         * If option.allRealmPaths are used to populate the parent dropdown. Not all views have this list availiable,
         * so if none are passed in this view will make another call to get this data.
         * If option.callback called after the new changes are saved the the server. The call back fires regardless as
         * to whether the call was sucessfull or not. This being used to re-render the parent view.
         * @example
         * CreateUpdateRealmDialog.show({
         *    allRealmPaths :  this.data.allRealmPaths,
         *      realmPath : realm.parentPath,
         *      callback : function(){
         *          self.render();
         *      }
         * });
         * @param {Map} options A Map containing the options for the create/update realm dialog.
         */
        show: function (options) {
            var self = this,
                promise,
                newRealm,
                allRealmsPromise = $.Deferred();

            options = options ? options : {};
            newRealm = _.isEmpty(options.realmPath);

            this.data.newRealm = newRealm;

            if (options.allRealmPaths) {
                allRealmsPromise.resolve();
            } else {
                allRealmsPromise = SMSGlobalDelegate.realms.all();
            }

            if (newRealm) {
                promise = SMSGlobalDelegate.realms.schema();
            } else {
                promise = SMSGlobalDelegate.realms.get(options.realmPath);
            }

            $.when(promise, allRealmsPromise).done(function (data, allRealmsData) {
                var i18nTitleKey = newRealm ? "createTitle" : "updateTitle",
                    i18nButtonKey = newRealm ? "create" : "save",
                    realmName = data.values.name === "/" ? $.t("console.common.topLevelRealm") : data.values.name;

                if (!options.allRealmPaths) {
                    options.allRealmPaths = [];
                    _.each(allRealmsData[0].result, function (realm) {
                        if (realm.parentPath) {
                            options.allRealmPaths.push(realm.parentPath);
                        }
                    });
                }

                if (newRealm) {
                    // Only create dropdowns if the field is editable
                    data.schema.properties.parentPath["enum"] = options.allRealmPaths;
                    data.schema.properties.parentPath.options = { "enum_titles": options.allRealmPaths };
                } else {
                    // Once created, it should not be possible to edit a realm's name or who it's parent is.
                    data.schema.properties.name.readonly = true;
                    data.schema.properties.parentPath.readonly = true;
                }

                BootstrapDialog.show({
                    title: $.t("console.realms.createUpdateRealmDialog." + i18nTitleKey, { realmPath: realmName }),
                    message: function (dialog) {
                        var element = $("<div></div>");
                        dialog.form = new Form(element[0], data.schema, data.values);
                        return element;
                    },
                    buttons: [{
                        label: $.t("common.form.cancel"),
                        action: function (dialog) {
                            dialog.close();
                        }
                    }, {
                        id: "submitButton",
                        label: $.t("common.form." + i18nButtonKey),
                        cssClass: "btn-primary",
                        action: function (dialog) {
                            var self = this,
                                promise;

                            this.disable();

                            if (newRealm) {
                                promise = SMSGlobalDelegate.realms.create(dialog.form.data());
                            } else {
                                promise = SMSGlobalDelegate.realms.update(dialog.form.data());
                            }

                            promise.done(function () {
                                dialog.close();
                            }).always(function () {
                                self.enable();

                                if (options.callback) {
                                    options.callback();
                                }
                            });
                        }
                    }],
                    onshow: function (dialog) {
                        if (!self.data.newRealm) {
                            dialog.$modalBody.find(".container-path").hide();
                            dialog.$modalBody.find(".container-name").hide();
                        } else {
                            dialog.getButton("submitButton").disable();
                        }

                        dialog.$modalBody.on("change keyup", "input[name=\"root[name]\"]", function () {
                            var realmName = _.trim(dialog.$modalBody.find("input[name=\"root[name]\"]").val());
                            if (realmName.length > 0) {
                                dialog.getButton("submitButton").enable();
                            } else {
                                dialog.getButton("submitButton").disable();
                            }
                        });
                    },
                    onshown: function (dialog) {
                        dialog.$modalBody.find("[data-toggle='popover-realm-status']").popover({
                            content: $.t("console.realms.realmStatusPopover.content"),
                            placement: "left",
                            title: $.t("console.realms.realmStatusPopover.title"),
                            trigger: "focus"
                        });

                        dialog.$modalBody.find("[data-toggle='popover-realm-aliases']").popover({
                            content: $.t("console.realms.realmAliasesPopover.content"),
                            html: true,
                            placement: "left",
                            title: $.t("console.realms.realmAliasesPopover.title"),
                            trigger: "focus"
                        });
                    }
                });
            });
        }
    });

    return new CreateUpdateRealmDialog();
});
