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
 * $Id: EntitlementConfiguration.java,v 1.7 2010/01/08 23:59:31 veiming Exp $
 *
 * Portions Copyrighted 2011-2015 ForgeRock AS
 */

package com.sun.identity.entitlement;

import com.sun.identity.entitlement.util.SearchFilter;
import org.forgerock.openam.entitlement.PolicyConstants;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;

/**
 * Entitlement Configuration
 */
public abstract class EntitlementConfiguration {
    public static final String POLICY_EVAL_THREAD_SIZE = "evalThreadSize";
    public static final String POLICY_SEARCH_THREAD_SIZE = "searchThreadSize";
    public static final String POLICY_CACHE_SIZE = "policyCacheSize";
    public static final String INDEX_CACHE_SIZE = "indexCacheSize";
    public static final String RESOURCE_COMPARATOR = "resourceComparator";
    
    private Subject adminSubject;

    /**
     * Returns an instance of entitlement configuration.
     *
     * @param adminSubject Admin Subject who has rights to query and modify
     *        configuration datastore.
     * @param realm Realm name.
     * @return an instance of entitlement configuration.
     */
    public static EntitlementConfiguration getInstance(
        Subject adminSubject, String realm) {

        final Class clazz;
        try {
            //RFE: load different configuration plugin
            clazz = Class.forName(
                    "com.sun.identity.entitlement.opensso.EntitlementService");
        } catch (ClassNotFoundException e) {
            PolicyConstants.DEBUG.error("EntitlementConfiguration.<init>", e);
            return null;
        }

        Class[] parameterTypes = {String.class};
        try {
            Constructor constructor = clazz.getConstructor(parameterTypes);
            Object[] args = {realm};
            EntitlementConfiguration impl = (EntitlementConfiguration)
                constructor.newInstance(args);
            impl.adminSubject = adminSubject;
            return impl;
        } catch (InstantiationException ex) {
            PolicyConstants.DEBUG.error("PrivilegeIndexStore.getInstance",
                ex);
        } catch (IllegalAccessException ex) {
            PolicyConstants.DEBUG.error("PrivilegeIndexStore.getInstance",
                ex);
        } catch (IllegalArgumentException ex) {
            PolicyConstants.DEBUG.error("PrivilegeIndexStore.getInstance",
                ex);
        } catch (InvocationTargetException ex) {
            PolicyConstants.DEBUG.error("PrivilegeIndexStore.getInstance",
                ex);
        } catch (NoSuchMethodException ex) {
            PolicyConstants.DEBUG.error("PrivilegeIndexStore.getInstance",
                ex);
        } catch (SecurityException ex) {
            PolicyConstants.DEBUG.error("PrivilegeIndexStore.getInstance",
                ex);
        }
        return null;
    }

    /**
     * Returns the application with the specified name.
     *
     * @return The application or null if the application could not be found.
     */
    public abstract Application getApplication(String name);

    /**
     * Returns a set of registered applications.
     *
     * @return a set of registered applications.
     */
    public abstract Set<Application> getApplications();

    /**
     * Removes application.
     *
     * @param name name of application to be removed.
     * @throws EntitlementException if application cannot be removed.
     */
    public abstract void removeApplication(String name)
        throws EntitlementException;

    /**
     * Stores the application to data store.
     *
     * @param application Application object.
     * @throws EntitlementException if application cannot be stored.
     */
    public abstract void storeApplication(Application application)
        throws EntitlementException;

    /**
     * Returns a set of registered application type.
     *
     * @return A set of registered application type.
     */
    public abstract Set<ApplicationType> getApplicationTypes();

    /**
     * Removes application type.
     *
     * @param name name of application type to be removed.
     * @throws EntitlementException  if application type cannot be removed.
     */
    public abstract void removeApplicationType(String name)
        throws EntitlementException;

    /**
     * Stores the application type to data store.
     *
     * @param applicationType Application type  object.
     * @throws EntitlementException if application type cannot be stored.
     */
    public abstract void storeApplicationType(ApplicationType applicationType)
        throws EntitlementException;

    /**
     * Returns set of attribute values of a given attribute name,
     *
     * @param attributeName attribute name.
     * @return set of attribute values of a given attribute name,
     */
    public abstract Set<String> getConfiguration(String attributeName);

    /**
     * Returns subject attribute names.
     *
     * @param application Application name.
     * @return subject attribute names.
     * @throws EntitlementException if subject attribute names cannot be
     * returned.
     */
    public abstract Set<String> getSubjectAttributeNames(String application)
        throws EntitlementException;

    /**
     * Adds subject attribute names.
     *
     * @param application Application name.
     * @param names Set of subject attribute names.
     * @throws EntitlementException if subject attribute names cannot be
     *         added.
     */
    public abstract void addSubjectAttributeNames(String application,
        Set<String> names) throws EntitlementException;

    /**
     * Adds a new action.
     *
     * @param appName Application name,
     * @param name Action name.
     * @param defVal Default value.
     * @throws EntitlementException if action cannot be added.
     */
    public abstract void addApplicationAction(
        String appName,
        String name,
        Boolean defVal
    ) throws EntitlementException;

    /**
     * Returns subject attributes collector names.
     *
     * @return subject attributes collector names.
     * @throws EntitlementException if subject attributes collector names
     * cannot be returned.
     */
    public abstract Set<String> getSubjectAttributesCollectorNames()
        throws EntitlementException;

    /**
     * Returns subject attributes collector configuration.
     *
     * @param name subject attributes collector name
     * @return subject attributes collector configuration.
     * @throws EntitlementException if subject attributes collector
     * configuration cannot be returned.
     */
    public abstract Map<String, Set<String>>
        getSubjectAttributesCollectorConfiguration(String name)
        throws EntitlementException;

    /**
     * Sets subject attributes collector configuration.
     *
     * @param name subject attributes collector name
     * @param attrMap subject attributes collector configuration map.
     * @throws EntitlementException if subject attributes collector
     * configuration cannot be set.
     */
    public abstract void setSubjectAttributesCollectorConfiguration(
        String name, Map<String, Set<String>> attrMap)
        throws EntitlementException;

    /**
     * Returns <code>true</code> if OpenAM policy data is migrated to a
     * form that entitlements service can operates on them.
     *
     * @return <code>true</code> if OpenAM policy data is migrated to a
     * form that entitlements service can operates on them.
     */
    public abstract boolean hasEntitlementDITs();

    /**
     * Returns <code>true</code> if the system is migrated to support
     * entitlement services.
     *
     * @return <code>true</code> if the system is migrated to support
     * entitlement services.
     */
    public abstract boolean migratedToEntitlementService();
    
    /**
     * Returns <code>true</code> if the network monitoring for entitlements
     * is enabled
     *
     * @return <code>true</code> if the network monitoring for entitlements
     * is enabled.
     */
    public abstract boolean networkMonitorEnabled();
    
    /**
     * Allows the network monitoring to be enabled/disabled
     * 
     * @param enabled Is the network monitoring enabled
     */
    public abstract void setNetworkMonitorEnabled(boolean enabled);

    protected Subject getAdminSubject() {
        return adminSubject;
    }
    
    /**
     * Returns <code>true</code> if the system stores privileges in
     * XACML format and supports exporting privileges in XACML format
     *
     *
     * @return <code>true</code> if the system stores privileges in
     * XACML format and supports exporting privileges in XACML format
     */
    public abstract boolean xacmlPrivilegeEnabled();

    /**
     * Returns a set of application names for a given search criteria.
     *
     * @param adminSubject Admin Subject
     * @param filters Set of search filter.
     * @return a set of application names for a given search criteria.
     * @throws EntitlementException if search failed.
     */
    public abstract Set<String> searchApplicationNames(
        Subject adminSubject,
        Set<SearchFilter> filters) throws EntitlementException;

    public abstract void reindexApplications();

    public abstract Set<String> getParentAndPeerRealmNames()
        throws EntitlementException;

    public abstract String getRealmName(String realm);

    public abstract boolean doesRealmExist();
    
    /**
     * For the passed in Entitlement environment, update the value associated with the key "am.policy.realmDN".
     *
     * @param environment The Entitlement environment to update with new realm DN value.
     * @param subRealm The Sub Realm whose DN value should be stored in the environment map.
     * @return The existing realm DN value stored in the environment map to enable it to be restored.
     * @see #restoreEnvironmentRealmDn
     */
    public abstract Set<String> updateEnvironmentRealmDn(Map<String, Set<String>> environment, String subRealm);
    
    /**
     * For the passed in Entitlement environment, replace the existing realm DN with the previous value savedRealmDn.
     *
     * @param environment The Entitlement environment to update with the saved realm DN value.
     * @param savedRealmDn The value to assign to the "am.policy.realmDN" key in the environment map.
     * @see #updateEnvironmentRealmDn
     */
    public abstract void restoreEnvironmentRealmDn(Map<String, Set<String>> environment, Set<String> savedRealmDn);

    /**
     * For letting us know whether or not the Agent monitoring is enabled in core.
     *
     * @return true if monitoring is enabled, false otherwise
     */
    public abstract boolean isMonitoringRunning();

    /**
     * Informs us of the size of the policy window set in the configurable options.
     *
     * @return the value of the window size as configured.
     */
    public abstract int getPolicyWindowSize();
}
