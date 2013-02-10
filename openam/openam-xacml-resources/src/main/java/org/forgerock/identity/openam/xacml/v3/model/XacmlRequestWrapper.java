package org.forgerock.identity.openam.xacml.v3.model;


import com.sun.identity.entitlement.xacml3.core.Request;

public class XacmlRequestWrapper {

    private Request request;

    public XacmlRequestWrapper() {
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}
