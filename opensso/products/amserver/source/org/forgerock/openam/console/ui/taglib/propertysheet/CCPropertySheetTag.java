/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.openam.console.ui.taglib.propertysheet;

import com.iplanet.am.util.SystemProperties;
import com.sun.identity.shared.Constants;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author Peter Major
 */
public class CCPropertySheetTag extends com.sun.web.ui.taglib.propertysheet.CCPropertySheetTag {

    private static final String TXT = ".txt";
    private static final String URI = ".uri";
    private static final String CONTEXT_ROOT =
            SystemProperties.get(Constants.AM_SERVICES_DEPLOYMENT_DESCRIPTOR);
    private static final String HELP_TEMPLATE =
            "<div id=\"help{0}\" class=\"helpPanel\">"
            + "<div><a href=\"#\" id=\"close{0}\" onclick=\"hideHelp({0}); event.cancelBubble = true;return false;\">"
            + "<img alt=\"Close help\" src=\"" + CONTEXT_ROOT + "/console/images/tasks/close.gif\" border=\"0\" /></a></div><p>"
            + "<span class=\"helpHeader\">{2}</span></p>{3}</div>"
            + "<a href=\"#\" onclick=\"showHelp({0}); event.cancelBubble = true; return false;\" "
            + "onmouseover=\"hoverHelp({0}); event.cancelBubble = true;\" "
            + "onmouseout=\"outHelp({0}); event.cancelBubble = true;\" "
            + "onfocus=\"hoverHelp({0}); event.cancelBubble = true;\" "
            + "onblur=\"outHelp({0}); event.cancelBubble = true;\" id=\"i{0}\">"
            + "<img alt=\"{1}\" src=\"" + CONTEXT_ROOT + "/console/images/help/info.gif\" "
            + "id=\"helpImg{0}\" /></a><div class=\"helpText\">{1}</div>";
    private static final String URL_TEMPLATE = "<a href=\"http://openam.forgerock.org/doc/{0}\" class=\"helpFooter\">Read more in the OpenAM online help</a>";
    private static volatile int uniqueID = 0;

    @Override
    protected String getMessage(String key) {
        String txtKey = key + TXT;
        String uriKey = key + URI;
        String helpTxt = super.getMessage(txtKey);
        if (helpTxt.equals(txtKey)) {
            //There is no such property, so let's just render the content as usual
            return super.getMessage(key);
        } else {
            String helpUri = super.getMessage(uriKey);
            if (helpUri.equals(uriKey)) {
                //There is no Help URL so only render the HelpText
                return MessageFormat.format(HELP_TEMPLATE, uniqueID++, super.getMessage(key), helpTxt, "");
            } else {
                //Let's render everything
                return MessageFormat.format(HELP_TEMPLATE, uniqueID++, super.getMessage(key), helpTxt, MessageFormat.format(URL_TEMPLATE, helpUri));
            }
        }
    }

    public static String getDynamicHelp(ResourceBundle bundle, String key) {
        String txtKey = key + TXT;
        String urlKey = key + URI;
        String helpTxt = getFromBundle(bundle, txtKey);
        if (helpTxt == null) {
            //There is no such property, so let's just render the content as usual
            return getFromBundle(bundle, key);
        } else {
            String helpUrl = getFromBundle(bundle, urlKey);
            if (helpUrl == null) {
                //There is no Help URL so only render the HelpText
                return MessageFormat.format(HELP_TEMPLATE, uniqueID++, getFromBundle(bundle, key), helpTxt, "");
            } else {
                //Let's render everything
                return MessageFormat.format(HELP_TEMPLATE, uniqueID++, getFromBundle(bundle, key), helpTxt, MessageFormat.format(URL_TEMPLATE, helpUrl));
            }
        }
    }

    private static String getFromBundle(ResourceBundle bundle, String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException mre) {
            return null;
        }
    }
}
