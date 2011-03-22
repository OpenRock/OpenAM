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
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright © 2010–2011 ApexIdentity Inc. All rights reserved.
 */

package com.apexidentity.header;

import com.apexidentity.http.Message;
import com.apexidentity.util.Clearable;
import com.apexidentity.util.Indexed;

/**
 * An HTTP message header.
 *
 * @author Paul C. Bryan
 */
public interface Header extends Clearable, Indexed<String> {

    /**
     * Returns the name of the header, as it would canonically appear within an HTTP message.
     */
    @Override
    String getKey();

    /**
     * Populates the content of the header from the specified message. If the message does
     * not contain the appropriate header, callign this message has no effect (will not
     * change the values of the header).
     */
    void fromMessage(Message message);

    /**
     * Populates the content of the header from the specified string value.
     */
    void fromString(String string);

    /**
     * Sets the header in the specified message. If the header is empty, calling this method
     * has no effect (will not erase existing instances of the header in the message).
     */
    void toMessage(Message message);

    /**
     * Returns the header as a single string value. If the header is empty, this method will
     * return {@code null}.
     */
    String toString();
}
