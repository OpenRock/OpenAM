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

package com.apexidentity.heap;

// ApexIdentity Core Library
import com.apexidentity.io.TemporaryStorage;
import com.apexidentity.log.Logger;
import com.apexidentity.log.NullLogSink;

/**
 * A generic base class for heap objects with handy injected heap objects. This implementation
 * provides reasonable safe defaults, to be overriden by the concrete object's heaplet.
 *
 * @author Paul C. Bryan
 */
public class GenericHeapObject {

    /** Provides methods for various logging activities. */
    public Logger logger = new Logger(new NullLogSink(), getClass().getSimpleName());

    /** Allocates temporary buffers for caching streamed content during processing. */
    public TemporaryStorage storage = new TemporaryStorage();
}
