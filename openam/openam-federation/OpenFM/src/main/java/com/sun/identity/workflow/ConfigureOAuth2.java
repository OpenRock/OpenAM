package com.sun.identity.workflow;

import com.iplanet.sso.SSOToken;
import com.sun.identity.policy.*;
import com.sun.identity.policy.interfaces.Subject;
import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.sm.ServiceConfigManager;

import java.security.AccessController;
import java.util.*;

public class ConfigureOAuth2 extends Task {
    private static final String SERVICE_NAME = "OAuth2Provider";

    private static final String AUTHZ_CODE_LIFETIME_NAME = "forgerock-oauth2-provider-authorization-code-lifetime";
    private static final String REFRESH_TOKEN_LIFETIME_NAME = "forgerock-oauth2-provider-refresh-token-lifetime";
    private static final String ACCESS_TOKEN_LIFETIME_NAME = "forgerock-oauth2-provider-access-token-lifetime";
    private static final String ISSUE_REFRESH_TOKEN = "forgerock-oauth2-provider-issue-refresh-token";
    private static final String SCOPE_PLUGIN_CLASS= "forgerock-oauth2-provider-scope-implementation-class";

    //params
    private static final String REALM = "realm";

    //service params
    private static final String RTL = "rtl";
    private static final String ACL = "acl";
    private static final String ATL = "atl";
    private static final String IRT = "irt";
    private static final String SIC = "sic";

    //policy params
    private static final String PN = "pn";
    private static final String RN = "rn";
    private static final String SN = "sn";
    private static final String POLICY_URL = "policyURL";

    public ConfigureOAuth2(){

    }

    public String execute(Locale locale, Map params)
            throws WorkflowException {
        String realm = getString(params, REALM);

        //get the service params
        String refreshTokenLifetime = getString(params, RTL);
        String accessCodeLifetime = getString(params, ACL);
        String accessTokenLifetime = getString(params, ATL);
        String issueRefreshToken = getString(params, IRT);
        String scopeImplementationClass = getString(params, SIC);

        //create service attrs
        Map<String,Set<String>> attrValues = new HashMap<String, Set<String>>();
        Set<String> temp = new HashSet<String>();
        temp.add(refreshTokenLifetime);
        attrValues.put(REFRESH_TOKEN_LIFETIME_NAME, temp);
        temp = new HashSet<String>();
        temp.add(accessCodeLifetime);
        attrValues.put(AUTHZ_CODE_LIFETIME_NAME, temp);
        temp = new HashSet<String>();
        temp.add(accessTokenLifetime);
        attrValues.put(ACCESS_TOKEN_LIFETIME_NAME, temp);
        temp = new HashSet<String>();
        temp.add(issueRefreshToken);
        attrValues.put(ISSUE_REFRESH_TOKEN, temp);
        temp = new HashSet<String>();
        temp.add(scopeImplementationClass);
        attrValues.put(SCOPE_PLUGIN_CLASS, temp);

        //create service
        SSOToken token = null;
        try {
            token = (SSOToken) AccessController
                    .doPrivileged(AdminTokenAction.getInstance());
            ServiceConfigManager sm = new ServiceConfigManager(SERVICE_NAME,token);
            sm.createOrganizationConfig(realm,attrValues);
        } catch (Exception e){
            throw new WorkflowException("ConfigureOAuth2.execute() : Unable to create Service");
        }

        if (realm.equalsIgnoreCase("/")){
            //get policy paramaters
            String policyName = getString(params, PN);
            String ruleName = getString(params, RN);
            String subjectName = getString(params, SN);
            String policyURL = getString(params, POLICY_URL);

            //build the policy
            Policy policy = null;
            try {
                policy = new Policy(policyName);
            } catch (Exception e){
                throw new WorkflowException("ConfigureOAuth2.execute() : Unable create policy");
            }
            Map<String,Set<String>> actions = new HashMap<String,Set<String>>();
            temp = new HashSet<String>();
            temp.add("true");
            actions.put("POST", temp);
            temp = new HashSet<String>();
            temp.add("true");
            actions.put("GET", temp);

            Rule policyURLRule = null;
            Subject sub = null;

            try {
                policyURLRule = new Rule(ruleName,
                    "iPlanetAMWebAgentService",
                    policyURL,
                    actions);
                PolicyManager pm = new PolicyManager(token, realm);
                SubjectTypeManager stm = pm.getSubjectTypeManager();
                sub = stm.getSubject("AuthenticatedUsers");
            } catch (Exception e){
                throw new WorkflowException("ConfigureOAuth2.execute() : Unable to get Subject");
            }
            try {
                policy.addSubject(subjectName, sub);
                policy.addRule(policyURLRule);
            } catch (Exception e){
                throw new WorkflowException("ConfigureOAuth2.execute() : Unable add subject and rule to policy");
            }
            PolicyManager mgr = null;
            try {
                mgr = new PolicyManager(token, realm);
                mgr.addPolicy(policy);
            } catch (Exception e){
                throw new WorkflowException("ConfigureOAuth2.execute() : Unable to add policy");
            }
        }
        return "Successful Configure for Realm: " + realm;
    }
}
