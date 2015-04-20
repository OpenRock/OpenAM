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
 * $Id: PolicyCondition.java,v 1.2 2010/01/08 22:12:49 farble1670 Exp $
 */

/*
 * Portions Copyrighted 2010-2015 ForgeRock AS.
 */

package com.sun.identity.entitlement.opensso;

import static com.sun.identity.entitlement.EntitlementException.INVALID_PROPERTY_VALUE_UNKNOWN_VALUE;
import static com.sun.identity.entitlement.EntitlementException.POLICY_CLASS_CAST_EXCEPTION;
import static com.sun.identity.entitlement.EntitlementException.POLICY_CLASS_NOT_ACCESSIBLE;
import static com.sun.identity.entitlement.EntitlementException.POLICY_CLASS_NOT_INSTANTIABLE;
import static com.sun.identity.entitlement.EntitlementException.UNKNOWN_POLICY_CLASS;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.entitlement.ConditionDecision;
import com.sun.identity.entitlement.EntitlementConditionAdaptor;
import com.sun.identity.entitlement.EntitlementException;
import com.sun.identity.policy.PolicyException;
import com.sun.identity.policy.interfaces.Condition;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.forgerock.openam.entitlement.PolicyConstants;
import org.forgerock.openam.utils.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.security.auth.Subject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This condition wraps all OpenAM policy conditions.
 */

public class PolicyCondition extends  EntitlementConditionAdaptor {
    private String className;
    private String name;
    private Map<String, Set<String>> properties;

    public PolicyCondition() {
    }
    
    /**
     * Constructor.
     *
     * @param name Name of condition.
     * @param className Implementation class name.
     * @param properties Properties of the condition.
     */
    public PolicyCondition(
        String name,
        String className,
        Map<String, Set<String>> properties) {
		this.name = name;
        this.className = className;
        this.properties = properties;
    }

    /**
     * Returns class name.
     *
     * @return class name.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns name.
     *
     * @return name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns properties.
     *
     * @return properties.
     */
    public Map<String, Set<String>> getProperties() {
        return properties;
    }

    /**
     * Sets the state of this condition.
     *
     * @param state State of this condition.
     */
    public void setState(String state) {
        try {
            JSONObject jo = new JSONObject(state);
            setState(jo);
            this.name = jo.optString("name");
            this.className = jo.optString("className");
            this.properties = getProperties((JSONObject)jo.opt("properties"));
        } catch (JSONException e) {
            PolicyConstants.DEBUG.error("PolicyCondition.setState", e);
        }
    }

    private Map<String, Set<String>> getProperties(JSONObject jo) 
        throws JSONException {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        for (Iterator i = jo.keys(); i.hasNext(); ) {
            String key = (String)i.next();
            JSONArray arr = (JSONArray)jo.opt(key);
            Set set = new HashSet<String>();
            result.put(key, set);

            for (int j = 0; j < arr.length(); j++) {
                set.add(arr.getString(j));
            }
        }
        return result;
    }

    /**
     * Returns state of this condition.
     *
     * @return state of this condition.
     */
    public String getState() {
        JSONObject jo = new JSONObject();

        try {
            toJSONObject(jo);
            jo.put("className", className);
            jo.put("name", name);
            jo.put("properties", properties);
            return jo.toString(2);
        } catch (JSONException ex) {
            PolicyConstants.DEBUG.error("PolicyCondition.getState", ex);
        }
        return "";
    }

    /**
     * Returns condition decision.
     *
     * @param realm Realm name.
     * @param subject Subject to be evaluated.
     * @param resourceName Resource name.
     * @param environment Environment map.
     * @return condition decision.
     * @throws com.sun.identity.entitlement.EntitlementException if error occur.
     */
    public ConditionDecision evaluate(
        String realm,
        Subject subject,
        String resourceName,
        Map<String, Set<String>> environment
    ) throws EntitlementException {
        try {
            SSOToken token = (subject != null) ? getSSOToken(subject) : null;
            Condition cond = getPolicyCondition();
            com.sun.identity.policy.ConditionDecision dec =
                cond.getConditionDecision(token, environment);
            return new ConditionDecision(dec.isAllowed(), dec.getAdvices(), dec.getTimeToLive());
        } catch (SSOException ex) {
            throw new EntitlementException(510, ex);
        } catch (PolicyException ex) {
            throw new EntitlementException(510, ex);
        }
    }

    private static SSOToken getSSOToken(Subject subject) {
        Set privateCred = subject.getPrivateCredentials();
        for (Iterator i = privateCred.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (o instanceof SSOToken) {
                return (SSOToken)o;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        PolicyCondition other = (PolicyCondition)obj;
        if (!CollectionUtils.genericCompare(this.className, other.className)) {
            return false;
        }
        if (!CollectionUtils.genericCompare(this.name, other.name)) {
            return false;
        }
        return CollectionUtils.genericCompare(this.properties, other.properties);
    }

	@Override
	public String getDisplayType() {
		return "policy";
	}

    /**
     * Constructs a legacy policy {@link Condition} object based on the information contained in this adapter.
     *
     * @return the legacy policy condition.
     * @throws EntitlementException if an error occurs constructing the condition.
     */
    @JsonIgnore
    public Condition getPolicyCondition() throws EntitlementException {
        try {
            Condition cond = Class.forName(className).asSubclass(Condition.class).newInstance();
            cond.setProperties(properties);
            return cond;
        } catch (ClassNotFoundException cnfe) {
            throw new EntitlementException(UNKNOWN_POLICY_CLASS, new String[]{className}, cnfe);
        } catch (ClassCastException cce) {
            throw new EntitlementException(POLICY_CLASS_CAST_EXCEPTION, new String[]{className, Condition.class.getName()}, cce);
        } catch (InstantiationException ie) {
            throw new EntitlementException(POLICY_CLASS_NOT_INSTANTIABLE, new String[]{className}, ie);
        } catch (IllegalAccessException iae) {
            throw new EntitlementException(POLICY_CLASS_NOT_ACCESSIBLE, new String[]{className}, iae);
        } catch (PolicyException pe) {
            throw new EntitlementException(INVALID_PROPERTY_VALUE_UNKNOWN_VALUE, new String[]{className}, pe);
        }
    }

    @Override
    public void validate() throws EntitlementException {
        // Attempt to load the policy condition to validate it
        getPolicyCondition();
    }

    @Override
    public int hashCode() {
        int hc = super.hashCode();
        if (className != null) {
            hc = 31*hc + className.hashCode();
        }
        if (name != null) {
            hc = 31*hc + name.hashCode();
        }
        if (properties != null) {
            hc = 31*hc + properties.hashCode();
        }
        return hc;
    }
}
