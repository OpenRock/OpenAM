package org.forgerock.openam.xacml.v3.profiles;


import org.forgerock.openam.xacml.v3.model.DataBag;
import org.forgerock.openam.xacml.v3.model.DataDesignator;
import org.forgerock.openam.xacml.v3.model.XACML3Request;

import java.util.List;

/*
    This interface is the superclass for all Policy Attribute handlers
    Essentially a new handler would be implemented for each Profile that is supported.

    It should be able to return a value for any supported attribute type.

    It is the authoritive source,  and can use the request context, if needed.
 */
public interface XACML3AttributeHandler {
    /*
    return the string representing the prefix for any category that
    this handler will support.
     */
    public List<String> getProfileAttributes();
    public DataBag      resolve(DataDesignator designator, XACML3Request req);

}
