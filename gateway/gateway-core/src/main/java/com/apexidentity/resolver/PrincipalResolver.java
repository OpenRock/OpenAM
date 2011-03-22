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

package com.apexidentity.resolver;

// Java Standard Edition
import java.security.Principal;
import java.util.Collections;
import java.util.Set;

// ApexIdentity Core Library
import com.apexidentity.util.EnumUtil;

/**
 * Resolves {@link Principal} objects.
 *
 * @author Paul C. Bryan
 */
public class PrincipalResolver implements Resolver {

    private enum Element { name }; 

    @Override
    public Class getKey() {
        return Principal.class;
    }

    @Override
    public Object get(Object object, Object element) {
        if (object instanceof Principal) {
            Principal principal = (Principal)object;
            Element e = EnumUtil.valueOf(Element.class, element);
            if (e != null) {
                switch (e) {
                    case name: return principal.getName();
                }
            }
        }
        return Resolver.UNRESOLVED;
    }

    @Override
    public Object put(Object object, Object element, Object value) {
        return Resolver.UNRESOLVED; // immutable
    }

    @Override
    public Object remove(Object object, Object element) {
        return Resolver.UNRESOLVED; // immutable
    }

    @Override
    public boolean containsKey(Object object, Object element) {
        return (object instanceof Principal ? EnumUtil.valueOf(Element.class, element) != null : false);
    }

    @Override
    public Set<?> keySet(Object object) {
        return (object instanceof Principal ? EnumUtil.names(Element.class) : Collections.emptySet());
    }
}
