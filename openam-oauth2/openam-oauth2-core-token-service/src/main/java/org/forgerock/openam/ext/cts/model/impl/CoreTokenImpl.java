package org.forgerock.openam.ext.cts.model.impl;


import org.forgerock.json.fluent.JsonValue;
import org.forgerock.openam.oauth2.model.CoreToken;

import java.util.*;

public class CoreTokenImpl extends JsonValue implements CoreToken {

    private String id;
    private static ResourceBundle rb = ResourceBundle.getBundle("CoreToken");

    public CoreTokenImpl(){
        super(new HashMap<String, Object>());
    }

    public CoreTokenImpl(String id, String userName, String realm, String expireTime){
        super(new HashMap<String, Object>());
        this.put("tokenID", id);
        this.put("userName", userName);
        this.put("realm", realm);
        this.put("expireTime", expireTime);
    }

    /**
     * @{inheritDoc}
     */
    public Map<String, Object> convertToMap(){
        Map<String, Object> tokenMap = new HashMap<String, Object>();
        tokenMap.put(rb.getString("tokenID"), getTokenID());
        tokenMap.put(rb.getString("userName"), getUserID());
        tokenMap.put(rb.getString("realm"), getRealm());
        tokenMap.put(rb.getString("expireTime"), getExpireTime());
        return tokenMap;
    }

    public void setTokenID(String tokenID){
        this.id = tokenID;
    }

    public void setUserName(String userName) {
        Set<String> s = new HashSet<String>();
        s.add(userName);
        this.put("userName", s);
    }

    /**
     * {@inheritDoc}
     */
    public void setRealm(String realm) {
        Set<String> s = new HashSet<String>();
        s.add(realm == null ? "/" : new String(realm));
        this.put("Realm", s);
    }

    /**
     * {@inheritDoc}
     */
    public void setExpireTime(long expireTime) {
        Set<String> s = new HashSet<String>();
        s.add(String.valueOf(expireTime));
        this.put("expireTime", s);
    }

    /**
     * @{inheritDoc}
     */
    public String getTokenID(){
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserID() {
        String username = null;
        Set usernameSet = (Set) get("userName").getObject();
        if (usernameSet != null && !usernameSet.isEmpty()){
            username = usernameSet.iterator().next().toString();
        }
        return username;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRealm() {
        String realm = null;
        Set realmSet = (Set) get("Realm").getObject();
        if (realmSet != null && !realmSet.isEmpty()){
            realm = realmSet.iterator().next().toString();
        }
        return realm;
    }

    /**
     * Returns the expiry time as stored.
     *
     * @return time of expiry expressed as milliseconds since the epoch.
     */
    public long getExpireTime() {
        Set expirySet = (Set) get("expireTime").getObject();
        if (expirySet != null && !expirySet.isEmpty()){
            return Long.parseLong(expirySet.iterator().next().toString());
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExpired() {
        return (System.currentTimeMillis() > getExpireTime());
    }

    public void create(Map<String, Set<String>> tokenData){
        Set<String> temp = tokenData.get("tokenID");
        if (temp != null && !temp.isEmpty()){
            setTokenID(temp.iterator().next());
        }
        temp = tokenData.get("expireTime");
        if (temp != null && !temp.isEmpty()){
            setExpireTime(Long.parseLong(temp.iterator().next()));
        }
        temp = tokenData.get("realm");
        if (temp != null && !temp.isEmpty()){
            setRealm(temp.iterator().next());
        }
        temp = tokenData.get("userName");
        if (temp != null && !temp.isEmpty()){
            setUserName(temp.iterator().next());
        }

    }

}
