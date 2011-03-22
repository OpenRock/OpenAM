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

package com.apexidentity.filter;

// Java Standard Edition
import java.io.IOException;

// ApexIdentity Core Library
import com.apexidentity.handler.HandlerException;
import com.apexidentity.heap.GenericHeapObject;
import com.apexidentity.http.Exchange;

/**
 * A generic base class for filters with handy injected heap objects. 
 *
 * @author Paul C. Bryan
 * @see GenericHeapObject
 */
public abstract class GenericFilter extends GenericHeapObject implements Filter {

    @Override // Filter
    public abstract void filter(Exchange exchange, Chain chain) throws HandlerException, IOException;
}
