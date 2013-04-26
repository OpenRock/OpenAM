/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
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
package org.forgerock.openam.xacml.v3.model;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * DN utility for Parsing Directory Naming Constructs
 * either X500 or LDAP Naming based.
 *
 * X500Name  -- Originally Written 2001.
 *
 */
public class DN implements Serializable {

    // ************************************************
    // Global Fields.
    private String aOriginalName = null;

    private String[] aName = new String[3];

    private boolean Quoted = false;
    private boolean Valid = false;

    /**
     * Provides proper parsing and manipulation of a DN.
     * <p/>
     * This does not handle multi-part DNs and will flag the DN entry as
     * not Valid.
     *
     * @param aDN - Distinguished Name.
     */
    public DN(String aDN) {
        parse(aDN);
    } // End of idxParseDN Constructor.

    /**
     * clear method, called to clear our existing DN.
     */
    public void clear() {
        aName[0] = ""; // Naming Attribute Name.
        aName[1] = ""; // Naming Attribute Value.
        aName[2] = ""; // Remaining DN or Parent Level DN.

        Quoted = false;
        Valid = false;

        aOriginalName = ""; // Original Incoming valid to constructor.

    } // End of Clear method.


    /**
     * parse method, called to parse out the DN.
     */
    public void parse(String aDN) {

        clear();

        // *********************************
        // Save our Existing incoming Value.
        aOriginalName = aDN;

        // *************************************
        // Besure to trim before starting tests
        if (aDN != null) {
            aDN = aDN.trim();
        }

        // **********************************
        // Start Parsing...
        if ((!"".equals(aDN)) &&
                (aDN != null)) {
            // **********************************
            // Remove Any Quotes.
            do {
                int thequote = aDN.indexOf("\042");
                if (thequote == 0) {
                    Quoted = true;
                    aDN = aDN.substring(1);
                } else if (thequote > 0) {
                    Quoted = true;
                    aDN = aDN.substring(0, thequote) +
                            aDN.substring(thequote + 1);
                } // End of Else.
            } while (aDN.indexOf("\042") != -1);

        } // End of If.

        // **********************************
        // Is this infact an X500DN?
        if ((!"".equals(aDN)) &&
                (aDN != null) &&
                (aDN.startsWith("/"))) {
            aDN = convertX500NameToLDAPName(aDN);
        } // End of X500DN Check.

        // **********************************
        // Now check again for invalid start
        // and ending characters.
        if ((!"".equals(aDN)) &&
                (aDN != null) &&
                (!aDN.startsWith(",")) &&
                (!aDN.startsWith("+")) &&
                (!aDN.startsWith("=")) &&
                (!aDN.startsWith(">")) &&
                (!aDN.startsWith("<")) &&
                (!aDN.startsWith(";")) &&
                (!aDN.endsWith(",")) &&
                (!aDN.endsWith("+")) &&
                (!aDN.endsWith("=")) &&
                (!aDN.startsWith(">")) &&
                (!aDN.startsWith("<")) &&
                (!aDN.startsWith(";"))) {
            // *********************************
            // Clean up the DN.
            aDN = cleanDN(aDN);

            // **********************************
            // Parse.
            int thecomma = aDN.indexOf(",");
            if (thecomma <= 0) {
                aName[1] = aDN.trim();
            } else {
                aName[1] = aDN.substring(0, thecomma);

                aName[2] = aDN.substring(thecomma + 1);
                aName[2] = aName[2].trim();
            } // End of Else.

            int theequal = aName[1].indexOf("=");
            if (theequal > 0) {
                aName[0] = aName[1].substring(0, theequal);
                aName[0] = aName[0].trim();

                aName[1] = aName[1].substring(theequal + 1);
                aName[1] = aName[1].trim();
            } // End of If.

            // **********************************
            // Check to see if the RDN contains
            // a multivalued name.
            // If it does clear it, we do not
            // Support this capability nat this time.
            //
            int theplus = aName[1].indexOf("+");
            if (theplus >= 0) {
                clear();
            }

            // **********************************
            // Now Set the Validity Indicator.
            setValid();

        } // End of If.

    } // End of parse Method.

    /**
     * Obtains full DN.
     *
     * @return String full DN contained within Object.
     *         If incoming DN was Quoted, we will re-Quote entire DN.
     */
    public String getDNwithQuotes() {
        if (("".equals(aName[0])) ||
                ("".equals(aName[1]))) {
            if (Quoted) {
                return ("\042" + aName[2] + "\042");
            } else {
                return (aName[2]);
            }
        }

        if ("".equals(aName[2])) {
            if (Quoted) {
                return ("\042" + aName[0] + "=" + aName[1] + "\042");
            } else {
                return (aName[0] + "=" + aName[1]);
            }
        } else {
            if (Quoted) {
                return ("\042" + aName[0] + "=" + aName[1] + "," + aName[2] + "\042");
            } else {
                return (aName[0] + "=" + aName[1] + "," + aName[2]);
            }
        }
    } // End of getDN Method.

    /**
     * Obtains full DN, without Quotes.
     *
     * @return String full DN contained within Object
     */
    public String getDN() {
        if (("".equals(aName[0])) ||
                ("".equals(aName[1]))) {
            return (aName[2]);
        }

        if ("".equals(aName[2])) {
            return (aName[0] + "=" + aName[1]);
        } else {
            return (aName[0] + "=" + aName[1] + "," + aName[2]);
        }
    } // End of getDN Method.

    /**
     * Returns Original DN.
     *
     * @return Original DN.
     */
    public String getOriginalDN() {
        return (aOriginalName);
    } // End of getDN Method.

    /**
     * Obtains RDN.
     *
     * @return String RDN contained within Object
     */
    public String getRDN() {
        if (("".equals(aName[0])) ||
                ("".equals(aName[1]))) {
            return ("");
        } else {
            return (aName[0] + "=" + aName[1]);
        }
    } // End of getRDN Method.

    /**
     * Obtains Parent DN or PDN.
     * The Parent DN could be blank,
     * if this is a top Level DN,
     * Such as "dc=com".
     *
     * @return String PDN contained within Object
     */
    public String getPDN() {
        return (aName[2]);
    } // End of getPDN Method.

    /**
     * Obtains the Domain of the DN.
     *
     * @return String of Domain DN contained within Object
     */
    public String getDomain() {
        String value = getDN();
        int thedomain = value.indexOf("dc=");
        if (thedomain == 0) {
            return (value);
        } else if (thedomain > 0) {
            return (value.substring(thedomain));
        } // End of Else.

        return ("");
    } // End of getDomain Method.

    /**
     * Obtains everything but the Domain of the DN.
     *
     * @return String of DN contained within Object less Domain part.
     */
    public String getDNLessDomain() {
        String value = getDN();
        int thedomain = value.indexOf("dc=");
        if (thedomain == 0) {
            return ("");
        } else if (thedomain > 0) {
            value = value.substring(0, thedomain);
            int thecomma = value.lastIndexOf(',');
            if (thecomma > 0) {
                return (value.substring(0, thecomma));
            }
        } // End of Else.

        return (value);
    } // End of getDNLessDomain Method.

    /**
     * Obtains everything but the Domain of the DN and RDN.
     *
     * @return String of DN contained within Object less Domain part.
     */
    public String getPDNLessDomain() {
        String value = getPDN();
        int thedomain = value.indexOf("dc=");
        if (thedomain == 0) {
            return ("");
        } else if (thedomain > 0) {
            value = value.substring(0, thedomain);
            int thecomma = value.lastIndexOf(',');
            if (thecomma > 0) {
                return (value.substring(0, thecomma));
            }
        } // End of Else.

        return (value);
    } // End of getDNLessDomain Method.

    /**
     * Obtains the Naming Attribute for RDN.
     *
     * @return String Naming Attribute.
     */
    public String getNamingValue() {
        return (aName[1]);
    } // End of getNamingValue Method.

    /**
     * Obtains the Naming Value of DN.
     *
     * @return String Naming Value of DN.
     */
    public String getNamingAttribute() {
        return (aName[0]);
    } // End of getNamingAttribute Method.

    /**
     * Indicates if DN was quoted or not.
     *
     * @return boolean indicator.
     */
    public boolean isQuoted() {
        return (Quoted);
    } // End of getPDN Method.

    /**
     * Indicates if DN was quoted or not.
     *
     * @return boolean indicator.
     */
    public boolean isValid() {
        return (Valid);
    } // End of getPDN Method.

    /**
     * Sets the Valid indicator.
     */
    private void setValid() {
        if ((getDN() == null) ||
                ("".equals(getDN()))) {
            Valid = false;
        } else {
            Valid = true;
        }
    } // End of setValid Method.

    /**
     * Provides the depth of the current DN.Valid indicator.
     *
     * @return int Depth of current RDN.
     *         <pre>
     *         (-1) Indicates invalid.
     *         (0)  Indicates at top of Directory Structure.
     *         (1 or greater)  Indicates level of Directory Structure.
     *         </pre>
     */
    public int depth() {

        int thedepth = 0;

        // ********************
        // Is current DN Valid
        if (!isValid()) {
            return (-1);
        }

        // ********************
        // Are we at the TOP?
        if ((getPDN() == null) ||
                ("".equals(getPDN()))) {
            return (thedepth);
        }

        // ********************
        // Loop to Count depth.
        DN XDN = new DN(this.getDN());
        do {
            if ((XDN.getPDN() == null) ||
                    ("".equals(XDN.getPDN()))) {
                break;
            }
            thedepth++;
            XDN = new DN(XDN.getPDN());
        } while (true);

        // ********************
        // Are we at the TOP?
        return (thedepth);

    } // End of depth Method.

    /**
     * Provides X500 DN Syntax from an LDAP DN.
     * TODO:: Check for Escaped Commas.
     *
     * @return String X500 DN.
     */
    public String getX500Name() {
        String X500dn = "";
        StringTokenizer st = new StringTokenizer(this.getDN(), ",");
        while (st.hasMoreTokens()) {
            String ep = st.nextToken();
            X500dn = "/" + ep + X500dn;
        } // End of While Loop.
        return (X500dn);
    } // End of getX500Name Method.

    /*
     * convertX500NameToLDAPName
     * @param String X500Name
     * @return String LDAPName
     **/
    public String convertX500NameToLDAPName(String _x500name) {

        // ***************************************
        // Initialize.
        String _ldapname = "";

        // ***************************************
        // Now Parse out the X500 Domains to
        // Create the GLuE Nodes.
        //
        StringTokenizer NODES = new StringTokenizer(_x500name, "/");
        while (NODES.hasMoreTokens()) {
            String node = (String) NODES.nextToken();
            if ((node == null) || (node.equals(""))) {
                continue;
            }

            // **********************************
            // Place the Node at the Begining of
            // the LDAP Name.
            if (_ldapname.equals("")) {
                _ldapname = node;
            } else {
                _ldapname = node + "," + _ldapname;
            }
        } // End of While.

        // ***************************************
        // Return the LDAP Name.
        return (_ldapname);
    } // End of convertX500NameToLDAPName.

    /**
     * Provides clean LDAP DN with removing white space and such between separators.
     * <p/>
     * Clean up numerous whitespace, but do not make a invalid dn into a correct dn.
     *
     * @param _dn Incoming DN.
     * @return String Newly Cleaned DN.
     *         <p/>
     *         TODO : Check for Escaped Commas.
     */
    private String cleanDN(final String _dn) {
        String newDN = "";
        StringTokenizer st = new StringTokenizer(_dn, ",");
        while (st.hasMoreTokens()) {
            String ep = st.nextToken();
            if (ep == null) {
                continue;
            }
            ep = ep.trim();
            if (ep == null) {
                continue;
            }
            if (newDN.equals("")) {
                newDN = ep;
            } else {
                newDN = newDN + "," + ep;
            }
        } // End of While Loop.
        return (newDN);
    } // End of cleanDN Method.

    /**
     * Main
     *
     * @param args Incoming Argument Array.
     * @see DN
     */
    public static void main(String[] args) {

        // ****************************************
        // Parse the incoming Arguments and
        // create objects for each entity.
        //
        for (int i = 0; i < args.length; i++) {
            dntest(args[i]);
        }

    } // End of Main Method.

    /**
     * dntest method to test a DN Parsing from the Command Line.
     */
    private static void dntest(String myDN) {
        // *****************************************
        // Perform some tests on the incoming DN.
        DN zpDN = new DN(myDN);

        // *****************************************
        // Break output.
        System.out.println("\n\n*****************");

        // *****************************************
        // Show Original String.
        System.out.println(
                "Original DN String:[" +
                        zpDN.getOriginalDN() +
                        "].");

        // *****************************************
        // Show DN.
        System.out.println(
                "DN:[" +
                        zpDN.getDN() +
                        "].");

        // *****************************************
        // Is Parsed DN a Valid DN?
        if (zpDN.isValid()) {
            System.out.println(
                    "DN:[" + zpDN.getDN() + "], is Valid.");
        } // End of If.
        else {
            System.out.println(
                    "DN:[" + zpDN.getDN() + "], is Not Valid.");
        } // End of If.

        // *****************************************
        // Show the DN Depth.
        System.out.println(
                "DN Depth:[" +
                        zpDN.depth() +
                        "].");

        // *****************************************
        // Show Naming Attribute Name.
        System.out.println(
                "Naming Attribute:[" +
                        zpDN.getNamingAttribute() +
                        "].");

        // *****************************************
        // Show Naming Attribute Value.
        System.out.println(
                "Naming Value:[" +
                        zpDN.getNamingValue() +
                        "].");

        // *****************************************
        // Show RDN.
        System.out.println(
                "RDN:[" +
                        zpDN.getRDN() +
                        "].");

        // *****************************************
        // Show PDN.
        System.out.println(
                "PDN:[" +
                        zpDN.getPDN() +
                        "].");

        // *****************************************
        // Show Domain.
        System.out.println(
                "Domain:[" +
                        zpDN.getDomain() +
                        "].");

        // *****************************************
        // Show DN with Quotes.
        System.out.println(
                "DN with Quotes:[" +
                        zpDN.getDNwithQuotes() +
                        "].");

        // *****************************************
        // Show DN Less Domain.
        System.out.println(
                "DN Less Domain:[" +
                        zpDN.getDNLessDomain() +
                        "].");

        // *****************************************
        // Show PDN Less Domain.
        System.out.println(
                "PDN Less Domain:[" +
                        zpDN.getPDNLessDomain() +
                        "].");

        // *****************************************
        // Show X500 DN
        System.out.println(
                "X500 DN:[" +
                        zpDN.getX500Name() +
                        "].");

    } // End of dntest method.
}
