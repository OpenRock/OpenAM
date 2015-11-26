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

package org.forgerock.openam.selfservice;

import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.forgerock.openam.selfservice.config.beans.KbaConsoleConfig.KbaBuilder;

import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ActionRequest;
import org.forgerock.json.resource.ActionResponse;
import org.forgerock.json.resource.NotSupportedException;
import org.forgerock.json.resource.PatchRequest;
import org.forgerock.json.resource.ReadRequest;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.json.resource.Responses;
import org.forgerock.json.resource.SingletonResourceProvider;
import org.forgerock.json.resource.UpdateRequest;
import org.forgerock.openam.rest.RealmContext;
import org.forgerock.openam.sm.config.ConsoleConfigHandler;
import org.forgerock.openam.selfservice.config.beans.KbaConsoleConfig;
import org.forgerock.services.context.Context;
import org.forgerock.util.promise.Promise;
import org.forgerock.util.promise.Promises;

import javax.inject.Inject;

/**
 * KBA resource is responsible for delivering up configured security questions.
 *
 * @since 13.0.0
 */
final class KbaResource implements SingletonResourceProvider {

    private final ConsoleConfigHandler configHandler;

    @Inject
    KbaResource(ConsoleConfigHandler configHandler) {
        this.configHandler = configHandler;
    }

    @Override
    public Promise<ResourceResponse, ResourceException> readInstance(Context context, ReadRequest readRequest) {
        String realm = RealmContext.getRealm(context);
        KbaConsoleConfig kbaConsoleConfig = configHandler.getConfig(realm, KbaBuilder.class);

        JsonValue jsonQuestions = json(
                object(
                        field("questions", kbaConsoleConfig.getSecurityQuestions()),
                        field("minimumAnswersToDefine", kbaConsoleConfig.getMinimumAnswersToDefine()),
                        field("minimumAnswersToVerify", kbaConsoleConfig.getMinimumAnswersToVerify())));

        ResourceResponse response = Responses.newResourceResponse("1", "1.0", jsonQuestions);
        return Promises.newResultPromise(response);
    }

    @Override
    public Promise<ActionResponse, ResourceException> actionInstance(Context context, ActionRequest actionRequest) {
        return new NotSupportedException().asPromise();
    }

    @Override
    public Promise<ResourceResponse, ResourceException> patchInstance(Context context, PatchRequest patchRequest) {
        return new NotSupportedException().asPromise();
    }

    @Override
    public Promise<ResourceResponse, ResourceException> updateInstance(Context context, UpdateRequest updateRequest) {
        return new NotSupportedException().asPromise();
    }

}
