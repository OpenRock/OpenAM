/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: LogRecWrite.java,v 1.6 2009/06/19 02:33:29 bigfatrat Exp $
 *
 * Portions Copyrighted 2011-2015 ForgeRock AS
 * Portions Copyrighted 2013 Nomura Research Institute, Ltd
 */
package com.sun.identity.log.service;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.forgerock.openam.audit.AuditConstants.*;
import static org.forgerock.openam.utils.CollectionUtils.getFirstItem;

import com.iplanet.dpro.parser.ParseOutput;
import com.iplanet.services.comm.share.Response;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdUtils;
import com.sun.identity.log.LogConstants;
import com.sun.identity.log.LogRecord;
import com.sun.identity.log.Logger;
import com.sun.identity.log.s1is.LogSSOTokenDetails;
import com.sun.identity.log.service.AgentLogParser.LogExtracts;
import com.sun.identity.log.spi.Debug;
import com.sun.identity.monitoring.Agent;
import com.sun.identity.monitoring.MonitoringUtil;
import com.sun.identity.monitoring.SsoServerLoggingHdlrEntryImpl;
import com.sun.identity.monitoring.SsoServerLoggingSvcImpl;
import org.forgerock.audit.events.AuditEvent;
import org.forgerock.openam.audit.AMAuditEventBuilderUtils;
import org.forgerock.openam.audit.AuditConstants;
import org.forgerock.openam.audit.AuditEventFactory;
import org.forgerock.openam.audit.AuditEventPublisher;
import org.forgerock.openam.audit.context.AuditRequestContext;
import org.forgerock.openam.utils.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

/**
 * This class implements <code>ParseOutput</code> interface and <code>
 * LogOperation</code> interface. It is parsing request and process the request.
 * log record. This class is registered with the SAX parser.
 */
public class LogRecWrite implements LogOperation, ParseOutput {

    private static final String EVALUATION_REALM = "org.forgerock.openam.agents.config.policy.evaluation.realm";
    
    String _logname;
    String _loggedBySid;
    Vector _records = new Vector();

    /**
     * Return result of the request processing in <code>Response</code>
     * @return result of the request processing in <code>Response</code>
     */
    public Response execute(AuditEventPublisher auditEventPublisher, AuditEventFactory auditEventFactory) {
        Response res = new Response("OK");
        SsoServerLoggingSvcImpl slsi = null;
        SsoServerLoggingHdlrEntryImpl slei = null;
        if (MonitoringUtil.isRunning()) {
            slsi = Agent.getLoggingSvcMBean();
            slei = slsi.getHandler(SsoServerLoggingSvcImpl.REMOTE_HANDLER_NAME);
        }
        
        Logger logger = (Logger)Logger.getLogger(_logname);
        if (Debug.messageEnabled()) {
            Debug.message("LogRecWrite: exec: logname = " + _logname);
        }
        
        Level level = 
            Level.parse(((com.sun.identity.log.service.LogRecord)_records.
        elementAt(0)).level);
        String msg = ((com.sun.identity.log.service.LogRecord)_records.
        elementAt(0)).msg;
        Map logInfoMap = ((com.sun.identity.log.service.LogRecord)_records.
        elementAt(0)).logInfoMap;
        Object [] parameters = 
            ((com.sun.identity.log.service.LogRecord)_records.
        elementAt(0)).parameters;
        
        try {
            msg = new String(com.sun.identity.shared.encode.Base64.decode(msg));
        } catch(RuntimeException ex){
            // if message is not base64 encoded just ignore & 
            // write msg as it is.
            if (Debug.messageEnabled()) {
                Debug.message("LogRecWrite: message is not base64 encoded");
            }
        }

        LogRecord rec = new LogRecord(level, msg);

        if (logInfoMap != null) {
            String loginIDSid =
                (String)logInfoMap.get(LogConstants.LOGIN_ID_SID);
            if (loginIDSid != null && loginIDSid.length() > 0) {
                SSOToken loginIDToken = null;
                try {
                    SSOTokenManager ssom = SSOTokenManager.getInstance();
                    loginIDToken = ssom.createSSOToken(loginIDSid);
                } catch (SSOException e) {
                    if (Debug.warningEnabled()) {
                        Debug.warning("LogService::process(): SSOException", e);
                    }
                    rec.setLogInfoMap(logInfoMap);
                }
                if (loginIDToken != null){
                    // here fill up logInfo into the newlr
                    rec = LogSSOTokenDetails.logSSOTokenInfo(rec, loginIDToken);

                    // now take one be one values from logInfoMap and overwrite 
                    // any populated value from sso token.
                    Set keySet = logInfoMap.keySet();
                    Iterator i = keySet.iterator();
                    String key = null;
                    String value = null;
                    while (i.hasNext()) {
                        key = (String)i.next();
                        value = (String)logInfoMap.get(key);
                        if(value != null && value.length() > 0) {
                            if (key.equalsIgnoreCase(LogConstants.DATA)) {
                                try {
                                    value = new String(
                                   com.sun.identity.shared.encode.Base64.decode(
                                        value));
                                } catch(RuntimeException ex){
                                    // if message is not base64 encoded just 
                                    // ignore & write msg as it is.
                                    if (Debug.messageEnabled()) {
                                        Debug.message(
                                            "LogRecWrite: data is not "
                                            + "base64 encoded");
                                    }
                                }
                            }
                            rec.addLogInfo(key, value);
                        }
                    }
                }
            } else {
                rec.setLogInfoMap(logInfoMap);
            }
        }
        rec.addLogInfo(LogConstants.LOG_LEVEL, rec.getLevel().toString());

        rec.setParameters(parameters);
        
        SSOToken loggedByToken = null;
        String realm = NO_REALM;
        try {
            SSOTokenManager ssom = SSOTokenManager.getInstance();
            loggedByToken = ssom.createSSOToken(_loggedBySid);
            Map<String, Set<String>> appAttributes = IdUtils.getIdentity(loggedByToken).getAttributes();
            realm = getFirstItem(appAttributes.get(EVALUATION_REALM), NO_REALM);
        } catch (IdRepoException | SSOException ssoe) {
            Debug.error("LogRecWrite: exec:SSOException: ", ssoe);
        }
        if (MonitoringUtil.isRunning()) {
            slei.incHandlerRequestCount(1);
        }
        auditAccessMessage(auditEventPublisher, auditEventFactory, rec, realm);
        logger.log(rec, loggedByToken);
        // Log file record write okay and return OK
        if (MonitoringUtil.isRunning()) {
            slei.incHandlerSuccessCount(1);
        }
        return res;
    }

    private void auditAccessMessage(AuditEventPublisher auditEventPublisher, AuditEventFactory auditEventFactory,
            LogRecord record, String realm) {

        if (!auditEventPublisher.isAuditing(realm, AuditConstants.ACCESS_TOPIC)) {
            return;
        }

        AgentLogParser logParser = new AgentLogParser();
        LogExtracts logExtracts = logParser.tryParse(record.getMessage());

        if (logExtracts == null) {
            // A message type of no interest
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> info = record.getLogInfoMap();
        String clientIp = info.get(LogConstants.IP_ADDR);

        if (StringUtils.isEmpty(clientIp)) {
            clientIp = info.get(LogConstants.HOST_NAME);
        }

        String contextId = info.get(LogConstants.CONTEXT_ID);
        String clientId = info.get(LogConstants.LOGIN_ID);

        String resourceUrl = logExtracts.getResourceUrl();
        int queryStringIndex = resourceUrl.indexOf('?');
        String queryString = queryStringIndex > -1 ? resourceUrl.substring(queryStringIndex) : "";
        String path = resourceUrl.replace(queryString, "");
        Map<String, List<String>> queryParameters = AMAuditEventBuilderUtils.getQueryParametersAsMap(queryString);

        AuditEvent auditEvent = auditEventFactory.accessEvent(realm)
                .transactionId(AuditRequestContext.getTransactionIdValue())
                .eventName(EventName.AM_ACCESS_ATTEMPT)
                .component(Component.POLICY_AGENT)
                .userId(clientId)
                .httpRequest(hasSecureScheme(resourceUrl), "UNKNOWN", path, queryParameters,
                        Collections.<String, List<String>>emptyMap())
                .request("HTTP", "UNKNOWN")
                .client(clientIp)
                .trackingId(contextId)
                .response(null, logExtracts.getStatus(), -1, MILLISECONDS)
                .toEvent();

        auditEventPublisher.tryPublish(AuditConstants.ACCESS_TOPIC, auditEvent);
    }

    private boolean hasSecureScheme(String resourceUrl) {
        URI resourceURI;
        try {
            resourceURI = new URI(resourceUrl);
            String scheme = resourceURI.getScheme();
            if (StringUtils.isNotEmpty(scheme) && "https".equals(scheme.toLowerCase())) {
                return true;
            }
        } catch (URISyntaxException e) {
            //Fall through...
        }
        return false;
    }

    /**
     * The method that implements the ParseOutput interface. This is called
     * by the SAX parser.
     * @param name name of request
     * @param elems vaector has parsing elements
     * @param atts parsing attributes
     * @param pcdata given data to be parsed.
     */
    public void process(String name, Vector elems, Hashtable atts,
    String pcdata) { 
        
        _logname = ((Log) elems.elementAt(0))._logname;
        _loggedBySid = ((Log) elems.elementAt(0))._loggedBySid;
        
        for (int i = 1; i < elems.size(); i++) {
            com.sun.identity.log.service.LogRecord lr = 
                (com.sun.identity.log.service.LogRecord)elems.elementAt(i);
            _records.addElement(lr);
        }
    }
} //end of LogRecWrite
