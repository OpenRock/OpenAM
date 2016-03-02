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
* Copyright 2016 ForgeRock AS.
*/
package org.forgerock.openam.services.push;

import static org.forgerock.openam.services.push.PushNotificationConstants.*;

import com.sun.identity.shared.datastruct.CollectionHelper;
import com.sun.identity.sm.ServiceConfig;

/**
 * Helper for reading a PushNotificationService config, to decouple and aid testing.
 */
public class PushNotificationServiceConfigHelper {

    private ServiceConfig serviceConfig;

    /**
     * Produce a new PushNotificationServiceConfigHelper for the provided ServiceConfig.
     *
     * @param serviceConfig The realm-specific service config to read.
     */
    public PushNotificationServiceConfigHelper(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    /**
     * Retrieve the factory class used to generate PushNotificationDelegates described by this config.
     * @return A String containing the classname of the PushNotificationDelegateFactory class to use.
     */
    public String getFactoryClass() {
        return CollectionHelper.getMapAttr(serviceConfig.getAttributes(), DELEGATE_FACTORY_CLASS,
                DEFAULT_DELEGATE_FACTORY_CLASS);
    }

    /**
     * Retrieve a new PushNotificationServiceConfig from this Helper.
     * @return A valid PushNotificationServiceConfig for the delegate described by this service config.
     */
    public PushNotificationServiceConfig getConfig() throws PushNotificationException {

        String senderId = CollectionHelper.getMapAttr(serviceConfig.getAttributes(), DELEGATE_USERNAME);
        String apiKey = CollectionHelper.getMapAttr(serviceConfig.getAttributes(), DELEGATE_PASSWORD);
        String endpoint = CollectionHelper.getMapAttr(serviceConfig.getAttributes(), DELEGATE_ENDPOINT);
        int port = CollectionHelper.getIntMapAttr(serviceConfig.getAttributes(), DELEGATE_PORT, DEFAULT_PORT, null);

        return new PushNotificationServiceConfig.Builder()
                .withSenderId(senderId)
                .withApiKey(apiKey)
                .withEndpoint(endpoint)
                .withPort(port)
                .build();
    }

}