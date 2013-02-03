/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.forgerock.identity.fedlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.identity.saml2.assertion.*;
import com.sun.identity.saml2.common.*;
import com.sun.identity.fedlet.ag.*;


/**
 * Servlet to handle the REST interface
 *
 * Based on apache felix org/apache/felix/http/base/internal/service/ResourceServlet.java
 *
 * Changes and additions by
 * @author laszlo
 * @author aegloff
 */
@Component(name = "org.forgerock.identity.fedlet",
        immediate = true,
        policy = ConfigurationPolicy.IGNORE)
public final class FedletServlet
        extends HttpServlet {
    final static Logger logger = LoggerFactory.getLogger(FedletServlet.class);

    //TODO Decide where to put the web and the java resources. Now both are in root
    //This requires to create new HttpContext from single bundle with that path,
    private final String path = "/sf";

    ServiceRegistration<HttpServlet> serviceRegistration = null;

    @Activate
    protected void activate(ComponentContext context) throws ServletException, NamespaceException {

        //context.getProperties().get("openam.fedlet.alias");
        String alias = path;
        // TODO Read these from configuraton
        Dictionary<String,Object> properties = new Hashtable<String,Object>();
        properties.put("alias", alias);
        properties.put("httpContext.id", "openidm");
        properties.put("servletNames", "OpenIDM Fedlet");

        // All WebApplication elements must be registered with the same
        // BundleContext
        serviceRegistration = context.getBundleContext()
                        .registerService(HttpServlet.class, this, properties);
        logger.debug("Registered fedlet servlet at {}", alias);
    }

    @Deactivate
    protected synchronized void deactivate(ComponentContext context) {
        if (null != serviceRegistration) {
            serviceRegistration.unregister();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        logger.debug("GET call on {}", req);
        AssertionGen ag = new AssertionGen();

        String[] attrs = {"ATTR_UID"};
        String[] vals =  {"VALUE"};

        vals[0] = req.getParameter("ATTR_UID");

        String encodedResMsg = SAML2Utils.encodeForPOST(ag.getResponse(attrs,vals));
        MetaDataParser lparser = new MetaDataParser();
        String relayState = null;
        String acsURL = lparser.getSPbaseUrl();

        SAML2Utils.postToTarget(res, "SAMLResponse",
                encodedResMsg, "RelayState", relayState, acsURL);

    }

}
