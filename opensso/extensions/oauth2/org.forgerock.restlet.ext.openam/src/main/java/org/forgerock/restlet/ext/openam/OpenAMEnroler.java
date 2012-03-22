/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2012 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * $Id$
 */
package org.forgerock.restlet.ext.openam;

import com.iplanet.sso.SSOException;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdType;
import com.sun.identity.idm.IdUtils;
import org.restlet.data.ClientInfo;
import org.restlet.security.Enroler;
import org.restlet.security.Role;

import java.util.Set;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class OpenAMEnroler implements Enroler {

    @Override
    public void enrole(ClientInfo clientInfo) {
        if (clientInfo.getUser() instanceof OpenAMUser) {
            clientInfo.getRoles().clear();
            try {
                AMIdentity id = IdUtils.getIdentity(((OpenAMUser) clientInfo.getUser()).getToken());
                Set<AMIdentity> groups = id.getMemberships(IdType.GROUP);
                if (groups != null && !groups.isEmpty()) {
                    for (AMIdentity group : groups) {
                        clientInfo.getRoles().add(new Role(group.getName(), ""));
                    }
                }
            } catch (IdRepoException e) {
                e.printStackTrace();
            } catch (SSOException e) {
                e.printStackTrace();
            }
        }
    }
}
