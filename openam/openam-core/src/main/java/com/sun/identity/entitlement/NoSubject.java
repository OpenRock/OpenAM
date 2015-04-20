/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: NoSubject.java,v 1.2 2009/10/29 19:05:18 veiming Exp $
 */

package com.sun.identity.entitlement;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;

/**
 *
 * @author dennis
 */
public class NoSubject implements SubjectImplementation {

    public void setState(String state) {
    }

    public String getState() {
        return "nosubject";
    }

    public Map<String, Set<String>> getSearchIndexAttributes() {
        return Collections.EMPTY_MAP;
    }

    public Set<String> getRequiredAttributeNames() {
        return Collections.EMPTY_SET;
    }

    public SubjectDecision evaluate(
        String realm, SubjectAttributesManager mgr, Subject subject,
        String resourceName, Map<String, Set<String>> environment
    ) throws EntitlementException {
        return new SubjectDecision(false, Collections.EMPTY_MAP);
    }

    public boolean isIdentity() {
        return true;
    }

}
