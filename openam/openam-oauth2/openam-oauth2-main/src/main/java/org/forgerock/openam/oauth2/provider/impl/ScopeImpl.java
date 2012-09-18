package org.forgerock.openam.oauth2.provider.impl;

import org.forgerock.openam.oauth2.exceptions.OAuthProblemException;
import org.forgerock.openam.oauth2.model.AccessToken;
import org.forgerock.openam.oauth2.provider.Scope;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScopeImpl implements Scope {

    public void init(){

    }

    public Map<String, Object> process(AccessToken token, OAuthProblemException error){
        Map<String, Object> map = new HashMap<String, Object>();
        Set<String> s = token.getScope();

        map.put("authenticated", "true");
        return map;
    }

    public void destroy(){

    }
}
