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

import org.forgerock.selfservice.core.config.ProcessInstanceConfig;
import org.forgerock.services.context.Context;

/**
 * Provides self service config instances based of the passed console configuration instance.
 *
 * @param <C>
 *         the console configuration type
 *
 * @since 13.0.0
 */
public interface ServiceConfigProvider<C extends ConsoleConfig> {


    /**
     * Determines whether the specific service is enabled.
     *
     * @param config
     *         the console config
     *
     * @return whether the service is enabled
     */
    boolean isServiceEnabled(C config);

    /**
     * Provides the self service config for the appropriate flow.
     *
     * @param config
     *         the console config
     * @param context
     *         CREST context
     * @param realm
     *         the current realm
     *
     * @return service config
     */
    ProcessInstanceConfig getServiceConfig(C config, Context context, String realm);

}
