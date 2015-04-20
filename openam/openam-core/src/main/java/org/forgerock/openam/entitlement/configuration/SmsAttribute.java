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
package org.forgerock.openam.entitlement.configuration;

import org.forgerock.util.Reject;

/**
 * Represents an attribute managed by the SMS layer.
 *
 * @since 13.0.0
 */
public final class SmsAttribute {

    private static final String ATTR_XML_KEYVAL = "sunxmlKeyValue";
    private static final String ATTR_KEYVAL = "sunKeyValue";

    private final String name;
    private final boolean searchable;

    private SmsAttribute(final String name, final boolean searchable) {
        this.name = name;
        this.searchable = searchable;
    }

    @Override
    public String toString() {
        final String smsKey = searchable ? ATTR_XML_KEYVAL : ATTR_KEYVAL;
        return smsKey + "=" + name;
    }

    public static SmsAttribute newSearchableInstance(final String name) {
        Reject.ifNull(name);
        return new SmsAttribute(name, true);
    }

    public static SmsAttribute newNoneSearchableInstance(final String name) {
        Reject.ifNull(name);
        return new SmsAttribute(name, false);
    }

}
