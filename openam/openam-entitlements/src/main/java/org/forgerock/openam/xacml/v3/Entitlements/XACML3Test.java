/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
 ~
 ~ The contents of this file are subject to the terms
 ~ of the Common Development and Distribution License
 ~ (the License). You may not use this file except in
 ~ compliance with the License.
 ~
 ~ You can obtain a copy of the License at
 ~ http://forgerock.org/license/CDDLv1.0.html
 ~ See the License for the specific language governing
 ~ permission and limitations under the License.
 ~
 ~ When distributing Covered Code, include this CDDL
 ~ Header Notice in each file and include the License file
 ~ at http://forgerock.org/license/CDDLv1.0.html
 ~ If applicable, add the following below the CDDL Header,
 ~ with the fields enclosed by brackets [] replaced by
 ~ your own identifying information:
 ~ "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.openam.xacml.v3.Entitlements;

import com.sun.identity.entitlement.xacml3.XACMLConstants;
import com.sun.identity.entitlement.xacml3.XACMLPrivilegeUtils;
import com.sun.identity.entitlement.xacml3.core.Policy;
import com.sun.identity.entitlement.xacml3.core.PolicySet;

import java.io.FileInputStream;
import java.util.Set;

public class XACML3Test {

    public static void main(String[] args) {
        try {


            FileInputStream fis = new FileInputStream("/Users/allan/A-SVN/xacml/PolicySet.xml");
            PolicySet ps = XACMLPrivilegeUtils.streamToPolicySet(fis);

            Set<Policy> policies
                    = XACMLPrivilegeUtils.getPoliciesFromPolicySet(ps);

            for (Policy policy : policies) {

                XACML3Policy pol = new XACML3Policy(policy);

                System.out.println("Done Parsing Policy");

            }
            System.out.println("ALL Done Parsing Policy");

        } catch (Exception ex) {

            System.out.println("Filed! -- " + ex);
        }

    }

}
