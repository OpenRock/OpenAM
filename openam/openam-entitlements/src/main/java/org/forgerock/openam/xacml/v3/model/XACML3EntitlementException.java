package org.forgerock.openam.xacml.v3.model;

/**
 * XACML 3 Entitlement Exception Class.
 *
 *
 * @author allan.foster@forgerock.com
 */
public class XACML3EntitlementException extends Exception {

    /**
     * Optional URN Associated with this Entitlement Exception.
     */
    private String urn;

    /**
     * Default Constructor
     */
    public XACML3EntitlementException() {
        super();
    }

    /**
     * Constructor with specified Exception Message.
     * @param message
     */
    public XACML3EntitlementException(String message) {
        super(message);
    }

    /**
     * Constructor with specified Exception Message and
     * URN.
     *
     * @param message
     * @param urn
     */
    public XACML3EntitlementException(String message, String urn) {
        super(message);

    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }
}
