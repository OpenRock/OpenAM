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

package com.apexidentity.util;

/**
 * Consolidates all discrete list interface operations into a single interface.
 *
 * @author Paul C. Bryan
 */
public interface FullList<E> extends ReadableList<E>, WritableList<E>, Clearable {

    /** Used for duck typing full maps. */
    public static final DuckType<FullList> DUCK = new DuckType<FullList>(FullList.class);  
}
