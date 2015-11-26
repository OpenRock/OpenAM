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
package org.forgerock.openam.audit.events.handlers;

import static com.iplanet.am.util.SystemProperties.CONFIG_PATH;
import static com.sun.identity.shared.Constants.AM_SERVICES_DEPLOYMENT_DESCRIPTOR;
import static com.sun.identity.shared.datastruct.CollectionHelper.*;

import com.iplanet.am.util.SystemProperties;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.audit.AuditException;
import org.forgerock.audit.events.handlers.AuditEventHandler;
import org.forgerock.audit.handlers.csv.CsvAuditEventHandler;
import org.forgerock.audit.handlers.csv.CsvAuditEventHandlerConfiguration;
import org.forgerock.audit.providers.DefaultSecureStorageProvider;
import org.forgerock.openam.audit.AuditEventHandlerFactory;
import org.forgerock.openam.audit.configuration.AuditEventHandlerConfiguration;
import org.forgerock.openam.utils.StringUtils;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This factory is responsible for creating an instance of the {@link CsvAuditEventHandler}.
 *
 * @since 13.0.0
 */
@Singleton
public class CsvAuditEventHandlerFactory implements AuditEventHandlerFactory {

    private final Debug debug = Debug.getInstance("amAudit");

    @Override
    public AuditEventHandler create(AuditEventHandlerConfiguration configuration) throws AuditException {
        Map<String, Set<String>> attributes = configuration.getAttributes();

        CsvAuditEventHandlerConfiguration csvHandlerConfiguration = new CsvAuditEventHandlerConfiguration();
        String location = getMapAttr(attributes, "location");
        csvHandlerConfiguration.setLogDirectory(location.replaceAll("%BASE_DIR%", SystemProperties.get(CONFIG_PATH)).
                replaceAll("%SERVER_URI%", SystemProperties.get(AM_SERVICES_DEPLOYMENT_DESCRIPTOR)));
        csvHandlerConfiguration.setTopics(attributes.get("topics"));
        csvHandlerConfiguration.setName(configuration.getHandlerName());
        csvHandlerConfiguration.setEnabled(getBooleanMapAttr(attributes, "enabled", true));
        setFileRotationPolicies(csvHandlerConfiguration, attributes);
        setFileRetentionPolicies(csvHandlerConfiguration, attributes);

        return new CsvAuditEventHandler(csvHandlerConfiguration, configuration.getEventTopicsMetaData(),
            new DefaultSecureStorageProvider());
    }

    private void setFileRotationPolicies(CsvAuditEventHandlerConfiguration csvHandlerConfiguration,
            Map<String, Set<String>> attributes) {
        boolean enabled = getBooleanMapAttr(attributes, "rotationEnabled", false);
        csvHandlerConfiguration.getFileRotation().setRotationEnabled(enabled);
        String maxFileSizeAttribute = getMapAttr(attributes, "rotationMaxFileSize");
        if (StringUtils.isNotEmpty(maxFileSizeAttribute)) {
            long maxFileSize = Long.parseLong(maxFileSizeAttribute);
            csvHandlerConfiguration.getFileRotation().setMaxFileSize(maxFileSize);
        }
        String filePrefix = getMapAttr(attributes, "rotationFilePrefix");
        csvHandlerConfiguration.getFileRotation().setRotationFilePrefix(filePrefix);
        String fileSuffix = getMapAttr(attributes, "rotationFileSuffix");
        csvHandlerConfiguration.getFileRotation().setRotationFileSuffix(fileSuffix);
        String interval = getMapAttr(attributes, "rotationInterval");
        csvHandlerConfiguration.getFileRotation().setRotationInterval(interval);
        List<String> times = new ArrayList<>();
        Set<String> rotationTimesAttribute = attributes.get("rotationTimes");
        if (rotationTimesAttribute != null && !rotationTimesAttribute.isEmpty()) {
            times.addAll(rotationTimesAttribute);
            csvHandlerConfiguration.getFileRotation().setRotationTimes(times);
        }
    }

    private void setFileRetentionPolicies(CsvAuditEventHandlerConfiguration csvHandlerConfiguration,
            Map<String, Set<String>> attributes) {
        int maxNumberOfHistoryFiles = getIntMapAttr(attributes, "retentionMaxNumberOfHistoryFiles", -1, debug);
        csvHandlerConfiguration.getFileRetention().setMaxNumberOfHistoryFiles(maxNumberOfHistoryFiles);
        long maxDiskSpaceToUse = getLongMapAttr(attributes, "retentionMaxDiskSpaceToUse", -1L, debug);
        csvHandlerConfiguration.getFileRetention().setMaxDiskSpaceToUse(maxDiskSpaceToUse);
        int minFreeSpaceRequired = getIntMapAttr(attributes, "retentionMinFreeSpaceRequired", -1, debug);
        csvHandlerConfiguration.getFileRetention().setMinFreeSpaceRequired(minFreeSpaceRequired);
    }
}
