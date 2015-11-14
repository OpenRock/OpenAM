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

package org.forgerock.openam.selfservice.config;

import com.iplanet.am.util.SystemProperties;
import com.sun.identity.shared.Constants;
import org.forgerock.json.jose.jwe.EncryptionMethod;
import org.forgerock.json.jose.jwe.JweAlgorithm;
import org.forgerock.json.jose.jws.JwsAlgorithm;
import org.forgerock.selfservice.core.StorageType;
import org.forgerock.selfservice.core.config.ProcessInstanceConfig;
import org.forgerock.selfservice.core.config.StageConfig;
import org.forgerock.selfservice.stages.captcha.CaptchaStageConfig;
import org.forgerock.selfservice.stages.email.VerifyEmailAccountConfig;
import org.forgerock.selfservice.stages.kba.KbaConfig;
import org.forgerock.selfservice.stages.kba.SecurityAnswerVerificationConfig;
import org.forgerock.selfservice.stages.reset.ResetStageConfig;
import org.forgerock.selfservice.stages.tokenhandlers.JwtTokenHandlerConfig;
import org.forgerock.selfservice.stages.user.UserQueryConfig;
import org.forgerock.services.context.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * The default forgotten password configuration definition.
 *
 * @since 13.0.0
 */
public final class DefaultForgottenPasswordConfigProvider
        implements ServiceConfigProvider<ForgottenPasswordConsoleConfig> {

    @Override
    public boolean isServiceEnabled(ForgottenPasswordConsoleConfig config) {
        return config.isEnabled();
    }

    @Override
    public ProcessInstanceConfig getServiceConfig(
            ForgottenPasswordConsoleConfig config, Context context, String realm) {

        List<StageConfig> stages = new ArrayList<>();

        if (config.isCaptchaEnabled()) {
            stages.add(new CaptchaStageConfig()
                    .setRecaptchaSiteKey(config.getCaptchaSiteKey())
                    .setRecaptchaSecretKey(config.getCaptchaSecretKey())
                    .setRecaptchaUri(config.getCaptchaVerificationUrl()));
        }

        stages.add(new UserQueryConfig()
                .setValidQueryFields(new HashSet<>(Arrays.asList("uid", "mail", "sn", "givenName")))
                .setIdentityIdField("/uid/0")
                .setIdentityUsernameField("/username")
                .setIdentityEmailField("/mail/0")
                .setIdentityServiceUrl("/users"));

        if (config.isEmailVerificationEnabled()) {
            String serverUrl = config.getEmailUrl() + "&realm=" + realm;
            stages.add(new VerifyEmailAccountConfig()
                    .setEmailServiceUrl("/email")
                    .setIdentityEmailField("mail")
                    .setSubjectTranslations(config.getSubjectTranslations())
                    .setMessageTranslations(config.getMessageTranslations())
                    .setMimeType("text/html")
                    .setVerificationLinkToken("%link%")
                    .setVerificationLink(serverUrl));
        }

        if (config.isKbaEnabled()) {
            stages.add(new SecurityAnswerVerificationConfig(new KbaConfig())
                    .setQuestions(config.getSecurityQuestions())
                    .setKbaPropertyName("kbaInformation")
                    .setNumberOfQuestionsUserMustAnswer(config.getMinQuestionsToAnswer())
                    .setIdentityServiceUrl("/users"));
        }

        stages.add(new ResetStageConfig()
                .setIdentityServiceUrl("/users")
                .setIdentityPasswordField("userPassword"));

        String secret = SystemProperties.get(Constants.ENC_PWD_PROPERTY);
        JwtTokenHandlerConfig jwtTokenConfig = new JwtTokenHandlerConfig()
                .setSharedKey(secret)
                .setKeyPairAlgorithm("RSA")
                .setKeyPairSize(1024)
                .setJweAlgorithm(JweAlgorithm.RSAES_PKCS1_V1_5)
                .setEncryptionMethod(EncryptionMethod.A128CBC_HS256)
                .setJwsAlgorithm(JwsAlgorithm.HS256)
                .setTokenLifeTimeInSeconds(config.getTokenExpiry());

        return new ProcessInstanceConfig()
                .setStageConfigs(stages)
                .setSnapshotTokenConfig(jwtTokenConfig)
                .setStorageType(StorageType.STATELESS);
    }

}
