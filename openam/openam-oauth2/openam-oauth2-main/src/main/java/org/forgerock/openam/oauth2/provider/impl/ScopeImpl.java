package org.forgerock.openam.oauth2.provider.impl;

import org.forgerock.openam.oauth2.model.AccessToken;
import org.forgerock.openam.oauth2.provider.Scope;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScopeImpl implements Scope {

    @Override
    public Set<String> scopeToPresentOnAuthorizationPage(Set<String> requestedScope, Set<String> availableScopes){

        if (requestedScope == null){
            return null;
        }

        Set<String> scopes = new HashSet<String>(availableScopes);
        scopes.retainAll(requestedScope);
        return scopes;
    }

    @Override
    public Set<String> scopeRequestedForAccessToken(Set<String> requestedScope, Set<String> availableScopes){

        if (requestedScope == null){
            return null;
        }

        Set<String> scopes = new HashSet<String>(availableScopes);
        scopes.retainAll(requestedScope);
        return scopes;
    }

    @Override
    public Set<String> scopeRequestedForRefreshToken(Set<String> requestedScope,
                                                     Set<String> availableScopes,
                                                     Set<String> allScopes){

        if (requestedScope == null){
            return null;
        }

        Set<String> scopes = new HashSet<String>(availableScopes);
        scopes.retainAll(requestedScope);
        return scopes;
    }

    @Override
    public Map<String, Object> retrieveTokenInfoEndPoint(AccessToken token){
        Map<String, Object> map = new HashMap<String, Object>();
        Set<String> s = token.getScope();

        if (s != null){
            map.put("scope", s.toString());
        }

        return map;
    }

    @Override
    public void destroy(){

    }

}
