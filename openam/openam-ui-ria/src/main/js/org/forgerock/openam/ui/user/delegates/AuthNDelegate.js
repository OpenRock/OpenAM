/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2015 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

/**
 * "Portions Copyrighted 2011-2015 ForgeRock Inc"
 */

/*global document, $, define, _, window */
define("org/forgerock/openam/ui/user/delegates/AuthNDelegate", [
    "org/forgerock/commons/ui/common/main/AbstractDelegate",
    "org/forgerock/commons/ui/common/main/Configuration",
    "org/forgerock/openam/ui/common/util/Constants",
    "org/forgerock/commons/ui/common/util/CookieHelper",
    "org/forgerock/commons/ui/common/main/EventManager",
    "org/forgerock/commons/ui/common/components/Messages",
    "org/forgerock/openam/ui/common/util/RealmHelper"
], function(AbstractDelegate, Configuration, Constants, CookieHelper, EventManager, Messages, RealmHelper) {
    var obj = new AbstractDelegate(Constants.host + "/"+ Constants.context + "/json/"),
        requirementList = [],
        knownAuth = {}; // to be used to keep track of the attributes associated with whatever requirementList contains

    obj.begin = function () {
        var url,
            args = {},
            tokenCookie,
            promise = $.Deferred();

        knownAuth = _.clone(Configuration.globalData.auth);

        /**
         * args is the URL query string
         * Configuration.globalData.auth.urlParams is fragment query string
         */
        if (Configuration.globalData.auth.urlParams) {
            _.extend(args, Configuration.globalData.auth.urlParams);
        }

        // In case user has logged in already update session
        tokenCookie = CookieHelper.getCookie(Configuration.globalData.auth.cookieName);
        if (tokenCookie) {
            args.sessionUpgradeSSOTokenId = tokenCookie;
        }

        url = RealmHelper.decorateURIWithSubRealm("__subrealm__/authenticate");

        if(RealmHelper.getOverrideRealm()) {
            args.realm = RealmHelper.getOverrideRealm();
        }

        url = url + "?" + $.param(args);

        obj.serviceCall({
                type: "POST",
                headers: {"Accept-API-Version": "protocol=1.0,resource=2.0"},
                data: "",
                url: url,
                errorsHandlers: {
                    "unauthorized": { status: "401"},
                    "bad request": { status: "400"}
                }
            })
        .done(function (requirements) {
            promise.resolve(requirements);
        })
        .fail(function (jqXHR) {
                   // some auth processes might throw an error fail immediately
                   var errorBody = $.parseJSON(jqXHR.responseText),
                       msg;

                   // if the error body contains an authId, then we might be able to
                   // continue on after this error to the next module in the chain
                   if (errorBody.hasOwnProperty("authId")) {
                       obj.submitRequirements(errorBody)
                          .done(function (requirements) {
                              obj.resetProcess();
                              promise.resolve(requirements);
                          })
                          .fail(function () {
                              promise.reject();
                          });
                   } else if(errorBody.code && errorBody.code === 400 ) {
                        msg = {
                                message: errorBody.message,
                                type: "error"
                        };
                        // in this case, the user has no way to login
                        promise.reject(msg);

                    } else {
                       // in this case, the user has no way to login
                       promise.reject();
                   }
               });

        return promise;

    };

    obj.handleRequirements = function (requirements) {
        if (requirements.hasOwnProperty("authId")) {
            requirementList.push(requirements);
        } else if (requirements.hasOwnProperty("tokenId")) {
            if (Configuration.globalData.auth.cookieDomains && Configuration.globalData.auth.cookieDomains.length !== 0){
                _.each(Configuration.globalData.auth.cookieDomains,function(cookieDomain){
                    CookieHelper.setCookie(Configuration.globalData.auth.cookieName, requirements.tokenId, "", "/", cookieDomain, Configuration.globalData.secureCookie);
                });
            } else {
                CookieHelper.setCookie(Configuration.globalData.auth.cookieName, requirements.tokenId, "", "/", location.hostname, Configuration.globalData.secureCookie);
            }
        }
    };

    obj.submitRequirements = function (requirements) {
        var promise = $.Deferred(),
            processSucceeded = function (requirements) {
                obj.handleRequirements(requirements);
                promise.resolve(requirements);
            },
            processFailed = function (reason) {
                var failedStage = requirementList.length;
                obj.resetProcess();
                promise.reject(failedStage, reason);
            },
            goToFailureUrl = function (errorBody) {
                if (errorBody.detail && errorBody.detail.failureUrl) {
                    console.log(errorBody.detail.failureUrl);
                    window.location.href = errorBody.detail.failureUrl;
                }

            },
            url;

            url = RealmHelper.decorateURIWithRealm("__subrealm__/authenticate");

            obj.serviceCall({
                type: "POST",
                headers: {"Accept-API-Version": "protocol=1.0,resource=2.0"},
                data: JSON.stringify(requirements),
                url: url,
                errorsHandlers: {
                    "unauthorized": { status: "401"},
                    "timeout": { status: "408" },
                    "Internal Server Error ": { status: "500" }
                }
            })
            .then(processSucceeded,
                  function (jqXHR) {
                    var oldReqs,errorBody,msg,
                        currentStage = requirementList.length,
                        responseMessage = jqXHR.responseJSON.message,
                        failReason = null,
                        countIndex,
                        warningText = 'Invalid Password!!Warning: Account lockout will occur after next ';
                    if (jqXHR.status === 408) {
                        // we timed out, so let's try again with a fresh session
                        oldReqs = requirementList[0];
                        obj.resetProcess();
                        obj.begin()
                            .done(function (requirements) {

                                obj.handleRequirements(requirements);

                                if (requirements.hasOwnProperty("authId")) {
                                    if (currentStage === 1) {
                                        // if we were at the first stage when the timeout occurred, try to do it again immediately.
                                        oldReqs.authId = requirements.authId;
                                        obj.submitRequirements(oldReqs)
                                            .done(processSucceeded)
                                            .fail(processFailed);
                                    } else {
                                        // restart the process at the beginning
                                        EventManager.sendEvent(Constants.EVENT_DISPLAY_MESSAGE_REQUEST, "loginTimeout");
                                        promise.resolve(requirements);
                                    }
                                } else {
                                    promise.resolve(requirements);
                                }
                            })
                            .fail(processFailed); // this is very unlikely, since it would require a call to .begin() to fail after having succeeded once before
                    } else if (jqXHR.status === 500) {
                        msg = {
                                message: responseMessage,
                                type: "error"
                        };
                    Messages.messages.addMessage(msg);
                    } else { // we have a 401 unauthorized response
                        errorBody = $.parseJSON(jqXHR.responseText);

                        // if the error body has an authId property, then we may be
                        // able to advance beyond this error
                        if (errorBody.hasOwnProperty("authId")) {

                            obj.submitRequirements(errorBody)
                               .done(processSucceeded)
                               .fail(processFailed);

                        } else {
                            // TODO to refactor this switch soon. Something like a map from error.message to failReason
                            // http://sources.forgerock.org/cru/CR-6216#CFR-114597
                            switch (errorBody.message) {
                                case "User Account Locked":
                                    failReason = "loginFailureLockout";
                                break;
                                case "Maximum Sessions Limit Reached.":
                                    failReason = "maxSessionsLimitOrSessionQuota";
                                break;
                                case " Your password has expired. Please contact service desk to reset your password":
                                    failReason = "loginFailureLockout";
                                break;
                                default:
                                    countIndex = errorBody.message.indexOf(warningText);
                                    if ( countIndex >= 0 ) {
                                        failReason = {key: "authenticationFailedWarning", count: errorBody.message.slice(warningText.length, warningText.length + 1)};
                                    } else {
                                        failReason = "authenticationFailed";
                                    }
                            }

                            processFailed(failReason);
                            goToFailureUrl(errorBody);
                        }
                    }
                });

        return promise;
    };

    obj.resetProcess = function () {
        requirementList = [];
    };

    obj.getRequirements = function (args) {
        var ret = $.Deferred();

        // if we don't have any requires yet, or if the realm changes.
        if (requirementList.length === 0 || !_.isEqual(Configuration.globalData.auth, knownAuth)) {

            obj.begin(args)
               .done(function (requirements) {
                   obj.handleRequirements(requirements);
                   ret.resolve(requirements);
               })
               .fail(function (error) {
                   ret.reject(error);
               });

        } else {
            ret.resolve(requirementList[requirementList.length-1]);
        }
        return ret;
    };

    obj.setGoToUrl = function (tokenId, urlGoTo){
        var args = {};
        args.goto = urlGoTo;
        return obj.serviceCall({
            type: "POST",
            headers: {"Accept-API-Version": "protocol=1.0,resource=2.0"},
            data: JSON.stringify(args),
            url: "",
            serviceUrl: Constants.host + "/" + Constants.context + "/json/users?_action=validateGoto",
            errorsHandlers: {"Bad Request": {status: "400"}}
        });
    };

    return obj;
});
