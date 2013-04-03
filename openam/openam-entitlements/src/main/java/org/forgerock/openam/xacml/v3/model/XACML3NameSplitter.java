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
 * $Id: ResourceNameSplitter.java,v 1.2 2009/08/28 06:16:31 veiming Exp $
 */

/*
 * Portions Copyrighted 2011 ForgeRock AS
 */

package org.forgerock.openam.xacml.v3.model;

import com.sun.identity.entitlement.ResourceSearchIndexes;
import com.sun.identity.entitlement.interfaces.ISearchIndex;

import java.util.HashSet;
import java.util.Set;

/*
 * This class splits resource name (URL) to different parts so that they
 * can be used for resource name comparison.
 */
public class XACML3NameSplitter implements ISearchIndex {
    public XACML3NameSplitter() {
    }

    /**
     * Returns the different components on a resource that can be
     * used to search for policies.
     *
     * @param resName Resource name.
     * @return the different components on a resource.
     */
    public ResourceSearchIndexes getIndexes(String resName, String realm)  {
            Set<String> hostIndexes = new HashSet<String>();

            Set<String> pathIndexes = new HashSet<String>();
            pathIndexes.add(resName);

            Set<String> pathParentIndexes = new HashSet<String>();

            return new ResourceSearchIndexes(hostIndexes, pathIndexes,
                    pathParentIndexes);
    }

}
