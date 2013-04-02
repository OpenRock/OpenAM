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
     * Internal Code Location Meta Tag
     */
    private String codeLocationTag;

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
        this.urn=urn;

    }

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public XACML3EntitlementException(String message, String urn, String codeLocationTag) {
        super(message);
        this.urn = urn;
        this.codeLocationTag = codeLocationTag;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getCodeLocationTag() {
        return codeLocationTag;
    }

    public void setCodeLocationTag(String codeLocationTag) {
        this.codeLocationTag = codeLocationTag;
    }
}
