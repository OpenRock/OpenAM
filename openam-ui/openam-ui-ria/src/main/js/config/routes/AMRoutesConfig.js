/**
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
 * Portions copyright 2011-2015 ForgeRock AS.
 */

define("config/routes/AMRoutesConfig", [], function () {
    return {
        continuePasswordReset: {
            view: "org/forgerock/openam/ui/user/anonymousProcess/PasswordResetView",
            url: /continuePasswordReset(\/[^\&]*)(\&.+)?/,
            pattern: "continuePasswordReset??",
            forceUpdate: true,
            defaults: ["/", ""],
            argumentNames: ["realm", "additionalParameters"]
        },
        continueSelfRegister: {
            view: "org/forgerock/openam/ui/user/anonymousProcess/SelfRegistrationView",
            url: /continueRegister(\/[^\&]*)(\&.+)?/,
            pattern: "continueRegister??",
            forceUpdate: true,
            defaults: ["/", ""],
            argumentNames: ["realm", "additionalParameters"]
        },
        confirmLogin: {
            view: "org/forgerock/openam/ui/user/login/RESTConfirmLoginView",
            role: "ui-user",
            url: "confirmLogin/",
            forceUpdate: true
        },
        dashboard: {
            view: "org/forgerock/openam/ui/dashboard/views/DashboardView",
            role: "ui-self-service-user",
            url: "dashboard/",
            forceUpdate: true,
            navGroup: "user"
        },
        oauth2Tokens: {
            view: "org/forgerock/openam/ui/user/oauth2/TokensView",
            role: "ui-user",
            url: "oauth2/tokens",
            forceUpdate: true
        },
        loggedOut: {
            view: "org/forgerock/openam/ui/user/logout/RESTLogoutView",
            url: /loggedOut([^\&]+)?(&.+)?/,
            pattern: "loggedOut??",
            defaults: ["/", ""],
            argumentNames: ["realm", "additionalParameters"]
        }
    };
});
