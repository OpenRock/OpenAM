/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock Incorporated. All Rights Reserved
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
package org.forgerock.identity.openam.xacml.v3.model;

import org.forgerock.identity.openam.xacml.v3.commons.ContentType;


/**
 * XACMLRequestInformation
 *
 * Simple POJO to hold all relevant information related to a
 * XACML Request.
 *
 * @author jeff.schenk@forgerock.com
 */
public class XACMLRequestInformation {

    /**
     * Requested URI
     */
    private String requestURI;
    /**
     * Meta Alias Information.
     */
    private String metaAlias;
    /**
     * Our PDP Entity ID.
     */
    private String pdpEntityID;
    /**
     * Realm.
     */
    private String realm;
    /**
     *
     */
    private boolean requestNodePresent;
    /**
     * Content Type
     */
    private ContentType contentType;
    /**
     * Original Request Content.
     */
    private String originalContent;
    /**
     * Content, can be either XML or JSON Object depending upon the specified ContentType.
     */
    private Object content;
    /**
     * Optional HTTP Digest Authorization Request
     */
    private String authenticationHeader;
    /**
     *  Indicates if this Request has been authenticated or not.
     */
    private boolean authenticated;
    /**
     * Content, can be either XML or JSON depending upon the specified ContentType.
     * If this object is Null, we have an Anonymous/Guest Request.
     */
    private Object authenticationContent;
    /**
     * XACMLAuthzDecisionQuery Fields.
     *
     * If this is an XACMLAuthzDecisionQuery, then the following fields
     * will be populated during Parsing of the XACMLAuthzDecisionQuery wrapper Document.
     *
     * These fields are:
     *      &lt;attribute name="ID" type="ID" use="required"/>
     *          example: ID="ID_1e469be0-ecc4-11da-8ad9-0800200c9a66"
     *      &lt;attribute name="Version" type="string" use="required"/>
     *          example: Version="2.0"
     *      &lt;attribute name="IssueInstant" type="dateTime" use="required"/>
     *          example: IssueInstant="2001-12-17T09:30:47.0Z"
     *      &lt;attribute name="Destination" type="anyURI" use="optional"/>
     *  	&lt;attribute name="Consent" type="anyURI" use="optional"/>
     *
     *
     */
    public class XACMLAuthzDecisionQuery {
        private static final String ID_NAME = "id";
        private String id;

        private static final String VERSION_NAME = "version";
        private String version;

        private static final String ISSUE_INSTANT_NAME = "issueinstant";
        private String issueInstant;

        private static final String DESTINATION_NAME = "destination";
        private String destination;

        private static final String CONSTENT_NAME = "consent";
        private String consent;

        XACMLAuthzDecisionQuery() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getIssueInstant() {
            return issueInstant;
        }

        public void setIssueInstant(String issueInstant) {
            this.issueInstant = issueInstant;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public String getConsent() {
            return consent;
        }

        public void setConsent(String consent) {
            this.consent = consent;
        }

        /**
         * Set Field By Name, using Reflection.
         *
         * @param nodeName
         * @param nodeValue
         * @return boolean - indicator true if field set correctly, otherwise false.
         */
        public boolean setByName(final String nodeName, final String nodeValue) {
            if ((nodeName == null) || (nodeName.isEmpty()) || (nodeValue == null) || (nodeValue.isEmpty())) {
                return false;
            }
            if (nodeName.toLowerCase().contains(ID_NAME)) {
                this.setId(nodeValue);
                return true;
            } else if (nodeName.toLowerCase().contains(VERSION_NAME)) {
                this.setVersion(nodeValue);
                return true;
            } else if (nodeName.toLowerCase().contains(DESTINATION_NAME)) {
                this.setDestination(nodeValue);
                return true;
            } else if (nodeName.toLowerCase().contains(CONSTENT_NAME)) {
                this.setConsent(nodeValue);
                return true;
            } else if (nodeName.toLowerCase().contains(ISSUE_INSTANT_NAME)) {
                this.setIssueInstant(nodeValue);
                return true;
            }
            // Indicate Field Not Set.
            return false;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer();
            sb.append("XACMLAuthzDecisionQuery");
            sb.append("{id='").append(id).append('\'');
            sb.append(", version='").append(version).append('\'');
            sb.append(", issueInstant='").append(issueInstant).append('\'');
            sb.append(", destination='").append(destination).append('\'');
            sb.append(", consent='").append(consent).append('\'');
            sb.append('}');
            return sb.toString();
        }
    } // End of Inner Class.

    /**
     * Wrapper Object for the XACMLAuthzDecisionQuery Element Attributes
     */
    XACMLAuthzDecisionQuery xacmlAuthzDecisionQuery;
     /**
     * Response Field for Request,
     * Digest Valid Indicator.
     * Can be False, if a XACMLAuthzDecisionQuery is performed.
     */
    private boolean digestValid = false;
    /**
     * Response Field for Request,
     * Digest Valid Indicator.
     * Ca
     */
    private String xacmlStringResponse = null;


    /**
     * Default Constructor.
     *
     * @param requestURI
     * @param metaAlias
     * @param pdpEntityID
     * @param realm
     */
    public XACMLRequestInformation(ContentType contentType, String requestURI, String metaAlias, String pdpEntityID,
                                   String realm) {
              this.contentType = contentType;
              this.requestURI = requestURI;
              this.metaAlias = metaAlias;
              this.pdpEntityID = pdpEntityID;
              this.realm = realm;
              this.xacmlAuthzDecisionQuery = new XACMLAuthzDecisionQuery();
    }



    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public String getMetaAlias() {
        return metaAlias;
    }

    public void setMetaAlias(String metaAlias) {
        this.metaAlias = metaAlias;
    }

    public String getPdpEntityID() {
        return pdpEntityID;
    }

    public void setPdpEntityID(String pdpEntityID) {
        this.pdpEntityID = pdpEntityID;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getAuthenticationHeader() {
        return authenticationHeader;
    }

    public void setAuthenticationHeader(String authenticationHeader) {
        this.authenticationHeader = authenticationHeader;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public XACMLAuthzDecisionQuery getXacmlAuthzDecisionQuery() {
        return xacmlAuthzDecisionQuery;
    }

    public void setXacmlAuthzDecisionQuery(XACMLAuthzDecisionQuery xacmlAuthzDecisionQuery) {
        this.xacmlAuthzDecisionQuery = xacmlAuthzDecisionQuery;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public Object getAuthenticationContent() {
        return authenticationContent;
    }

    public void setAuthenticationContent(Object authenticationContent) {
        this.authenticationContent = authenticationContent;
    }

    public boolean isRequestNodePresent() {
        return requestNodePresent;
    }

    public void setRequestNodePresent(boolean requestNodePresent) {
        this.requestNodePresent = requestNodePresent;
    }

    // **********************************************
    // Response Fields for Request
    // **********************************************

    public boolean isDigestValid() {
        return digestValid;
    }

    public void setDigestValid(boolean digestValid) {
        this.digestValid = digestValid;
    }

    public String getXacmlStringResponse() {
        return xacmlStringResponse;
    }

    public void setXacmlStringResponse(String xacmlStringResponse) {
        this.xacmlStringResponse = xacmlStringResponse;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("XACMLRequestInformation");
        sb.append("{requestURI='").append(requestURI).append('\'');
        sb.append(", metaAlias='").append(metaAlias).append('\'');
        sb.append(", pdpEntityID='").append(pdpEntityID).append('\'');
        sb.append(", realm='").append(realm).append('\'');
        sb.append(", requestNodePresent=").append(requestNodePresent);
        sb.append(", contentType=").append(contentType);
        sb.append(", originalContent='").append(originalContent).append('\'');
        sb.append(", content=").append(content);
        sb.append(", authenticationHeader='").append(authenticationHeader).append('\'');
        sb.append(", authenticated=").append(authenticated);
        sb.append(", authenticationContent=").append(authenticationContent);
        sb.append(", xacmlAuthzDecisionQuery=").append(xacmlAuthzDecisionQuery);
        sb.append(", digestValid=").append(digestValid);
        sb.append(", xacmlStringResponse='").append(xacmlStringResponse).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
