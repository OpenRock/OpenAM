/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007 Sun Microsystems Inc. All Rights Reserved
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
 * 
 *
 */

package com.sun.identity.fedlet.ag;

import com.sun.identity.saml.xmlsig.KeyProvider;
import com.sun.identity.saml2.assertion.*;
import com.sun.identity.saml2.protocol.*;
import com.sun.identity.saml2.common.SAML2Constants;
import com.sun.identity.saml2.common.SAML2Exception;
import com.sun.identity.saml2.key.KeyUtil;

import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *This class is used to generate the SAML reponse and return a string
 *
 */ 
public class AssertionGen {
    private static SecureRandom random = new SecureRandom();
    protected static final String SAML2ID_PREFIX = "s2";
    private static final String USERID_LABEL = "ATTR_UID";
    protected Map<String,String> tags;
    protected String userID;
    protected String certificateAlias;

    /**
 *Generate SAML response and return the xml string
 *
 */
    private AssertionGen() {

    }
    public AssertionGen(String home, String cert, String target, String userID, String certAlias) {

        tags = new HashMap<String,String>();
        tags.put("@HOME_URL@",home);
        tags.put("@CERTIFICATE@",cert);
        tags.put("@TARGET_URL@",target);
        this.userID = userID;
        this.certificateAlias = certAlias;

        FedletAGConfigurationImpl.setTagSwap(tags);
    }


    public String getResponse(){
        try {
            Response res = ProtocolFactory.getInstance().createResponse();
            List assertionList = new ArrayList();
            Status status = ProtocolFactory.getInstance().createStatus();
            StatusCode scode = ProtocolFactory.getInstance().createStatusCode();
            MetaDataParser lparser = new MetaDataParser();
            String IDPEntityID = lparser.getIDPEntityID();
            String SPEntityID = lparser.getSPEntityID();


            Assertion assertion = getAssertion(USERID_LABEL, userID);
            assertionList.add(assertion);
            res.setAssertion(assertionList);
            res.setID(generateID());
            res.setVersion(SAML2Constants.VERSION_2_0);
            res.setIssueInstant(new Date());

            scode.setValue(SAML2Constants.SUCCESS);
            status.setStatusCode(scode);

            res.setStatus(status);
            Issuer issuer = AssertionFactory.getInstance().createIssuer();
            issuer.setValue(IDPEntityID);

            res.setIssuer(issuer);
            res.setDestination(SPEntityID);

            return res.toXMLString(true, true);
        } catch (SAML2Exception ex) {
            Logger.getLogger(AssertionGen.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
 /**
 *Generate SAML arrestion and return Assertion object
 *
 */
    private Assertion getAssertion(String attrName, String attrValue)
    {
        Assertion assertion = AssertionFactory.getInstance().createAssertion();
        MetaDataParser lparser = new MetaDataParser();
        String IDPEntityID = lparser.getIDPEntityID();
        String SPEntityID = lparser.getSPEntityID();
        String SPBaseUrl = lparser.getSPbaseUrl();

        try {
            assertion.setID(generateID());
            assertion.setVersion(SAML2Constants.VERSION_2_0);
            assertion.setIssueInstant(new Date());
            
            Issuer issuer = AssertionFactory.getInstance().createIssuer();
            issuer.setValue(IDPEntityID);

            assertion.setIssuer(issuer);
            assertion.setAuthnStatements(getAuthStatementList());
            assertion.setSubject(getSubject(SPEntityID, SPBaseUrl, IDPEntityID));
            assertion.setConditions(getCondition(SPEntityID));
            assertion.setAttributeStatements(getAttributeList(attrName, attrValue));
            
            KeyProvider kp = KeyUtil.getKeyProviderInstance();

            assertion.sign(kp.getPrivateKey( certificateAlias),kp.getX509Certificate(certificateAlias));
            
            return assertion;
        } catch (SAML2Exception ex) {
            Logger.getLogger(AssertionGen.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return assertion;
    }
    /**
     *Add attributes to the SAML assertion
     *
     */
    private List getAttributeList(String [] attrName, String [] attrValue) throws SAML2Exception{

        List attrStatementList = new ArrayList();
        AttributeStatement attrStatement = AssertionFactory.getInstance().createAttributeStatement();
        List AttributeList = new ArrayList();


        for(int i = 0; i < attrName.length; i++){

            Attribute attribute = AssertionFactory.getInstance().createAttribute();
            List AttributeValueList = new ArrayList();

            attribute.setName(attrName[i]);
            AttributeValueList.add(attrValue[i]);
            attribute.setAttributeValueString(AttributeValueList);

            AttributeList.add(attribute);
        }
        attrStatement.setAttribute(AttributeList);
        attrStatementList.add(attrStatement);
        return attrStatementList;
    } /**
     *Add attributes to the SAML assertion
     *
     */
    private List getAttributeList(String attrName, String attrValue) throws SAML2Exception{

        List attrStatementList = new ArrayList();
        AttributeStatement attrStatement = AssertionFactory.getInstance().createAttributeStatement();
        List AttributeList = new ArrayList();

        Attribute attribute = AssertionFactory.getInstance().createAttribute();
        List AttributeValueList = new ArrayList();

        attribute.setName(attrName);
        AttributeValueList.add(attrValue);
        attribute.setAttributeValueString(AttributeValueList);

        AttributeList.add(attribute);
        attrStatement.setAttribute(AttributeList);
        attrStatementList.add(attrStatement);
        return attrStatementList;
    }
 /**
 *Generate auth statements and retun a list of auth statements
 *
 */    
    private List getAuthStatementList(){
        AuthnStatement authnStatement = AssertionFactory.getInstance().createAuthnStatement();
        AuthnContext authnContext = AssertionFactory.getInstance().createAuthnContext();         
        List AuthStatementList = new ArrayList();
        
        try {

            authnContext.setAuthnContextClassRef(SAML2Constants.CLASSREF_PASSWORD_PROTECTED_TRANSPORT);
            authnStatement.setAuthnContext(authnContext);
            authnStatement.setAuthnInstant(new Date());
            
            authnStatement.setSessionIndex("session_index");
            AuthStatementList.add(authnStatement);
        
        } catch (SAML2Exception ex) {
            Logger.getLogger(AssertionGen.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    
        return AuthStatementList;
    }
 /**
 *Add subject to the SAML assertion
 *
 */ 
    private Subject getSubject(String SPEntityID, String SPBaseUrl, String IDPEntutyID){
        Subject subject = AssertionFactory.getInstance().createSubject();
        try {

            NameID nameID = AssertionFactory.getInstance().createNameID();
            SubjectConfirmation sc = AssertionFactory.getInstance().createSubjectConfirmation();
            List SubjectConformationList = new ArrayList();

            nameID.setFormat(SAML2Constants.NAMEID_TRANSIENT_FORMAT);
            nameID.setNameQualifier(IDPEntutyID);
            nameID.setSPNameQualifier(SPEntityID);
            nameID.setValue("nameidvalue");

            subject.setNameID(nameID);
            sc.setMethod(SAML2Constants.SUBJECT_CONFIRMATION_METHOD_BEARER);
            
            int effectiveTime = SAML2Constants.ASSERTION_EFFECTIVE_TIME;
            Date date = new Date();
            date.setTime(date.getTime() + effectiveTime * 1000);
                

            SubjectConfirmationData scd = AssertionFactory.getInstance().createSubjectConfirmationData();
            scd.setRecipient(SPBaseUrl);
            scd.setNotOnOrAfter(date);
            sc.setSubjectConfirmationData(scd);
            SubjectConformationList.add(sc);

            subject.setSubjectConfirmation(SubjectConformationList);

            return subject;
        } catch (SAML2Exception ex) {
            Logger.getLogger(AssertionGen.class.getName()).log(Level.SEVERE, null, ex);
        }
        return subject;
    }
   /**
 *Add condition to the SAML assertion
 *
 */  
    private Conditions getCondition(String SPEntityID){
        Conditions conditions = AssertionFactory.getInstance().createConditions();
        AudienceRestriction ar= AssertionFactory.getInstance().createAudienceRestriction();
        List SPIDList = new ArrayList();
        List ARList = new ArrayList();
        try {

            conditions.setNotBefore(new Date());
            SPIDList.add(SPEntityID);
            ar.setAudience(SPIDList);
            ARList.add(ar);
            conditions.setAudienceRestrictions(ARList);
            
        } catch (SAML2Exception ex) {
            Logger.getLogger(AssertionGen.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        return conditions;
    }
    /**
     * Converts byte array to <code>Hex</code> String.
     *
     * @param byteArray Byte Array to be converted.
     * @return result of the conversion.
     */
    private static String byteArrayToHexString(byte[] byteArray) {
        int readBytes = byteArray.length;
        StringBuffer hexData = new StringBuffer();
        int onebyte;
        for (int i=0; i < readBytes; i++) {
            onebyte = ((0x000000ff & byteArray[i]) | 0xffffff00);
            hexData.append(Integer.toHexString(onebyte).substring(6));
        }
        return hexData.toString();
    }

    /**
     * Generates ID.
     * @return ID value.
     */
    private static String generateID() {
        if (random == null) {
            return null;
        }
        byte bytes[] = new byte[SAML2Constants.ID_LENGTH];
        random.nextBytes(bytes);
        return (SAML2ID_PREFIX + byteArrayToHexString(bytes));
    }


}
