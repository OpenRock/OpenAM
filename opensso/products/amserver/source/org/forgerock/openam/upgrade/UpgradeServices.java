/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 ForgeRock AS. All Rights Reserved
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
package org.forgerock.openam.upgrade;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.common.configuration.ServerConfiguration;
import com.sun.identity.common.configuration.UnknownPropertyNameException;
import com.sun.identity.setup.ServicesDefaultValues;
import com.sun.identity.setup.SetupConstants;
import com.sun.identity.shared.StringUtils;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.xml.XMLUtils;
import com.sun.identity.sm.AttributeSchemaImpl;
import com.sun.identity.sm.SMSException;
import com.sun.identity.sm.ServiceSchemaModifications;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import org.w3c.dom.Document;

/**
 * This is the primary upgrade class that determines the how the services need
 * to be upgraded and performs the upgrade.
 * 
 * @author steve
 */
public class UpgradeServices {
    protected static Debug debug = Debug.getInstance("amUpgrade");
    protected String fileName = null;
    protected static final List<String> serviceNames = new ArrayList<String>();
    private static final String umEmbeddedDS;
        
    static {
        ResourceBundle rb = ResourceBundle.getBundle(
            SetupConstants.PROPERTY_FILENAME);
        String names = rb.getString(SetupConstants.SERVICE_NAMES);
        StringTokenizer st = new StringTokenizer(names);
        
        while (st.hasMoreTokens()) {
            serviceNames.add(st.nextToken());
        }
        
        umEmbeddedDS = rb.getString("umEmbeddedDS");
    }
    
    /**
     * Determines the services that will be upgraded.
     *
     * @param adminToken Administrator Single Sign On token.
     * @param bUseExtUMDS <code>true</code> to use external DS as
     *         user management datastore.
     * @throws IOException if file operation errors occur.
     * @throws SMSException if services cannot be registered.
     * @throws SSOException if single sign on token is not valid.
     */
    /*public void upgrade(SSOToken adminToken, boolean bUseExtUMDS)
    throws IOException, SMSException, SSOException {
        Map map = ServicesDefaultValues.getDefaultValues();
        String basedir = (String)map.get(SetupConstants.CONFIG_VAR_BASE_DIR);
        String dirXML = basedir + "/config/xml";
        
        File xmlDirs = new File(dirXML);
        
        if (!xmlDirs.exists() || !xmlDirs.isDirectory()) {
            xmlDirs.mkdirs();
        }

        for (String serviceFileName : serviceNames) {
            boolean tagswap = true;
            
            if (serviceFileName.startsWith("*")) {
                serviceFileName = serviceFileName.substring(1);
                tagswap = false;
            }

            Object[] params = { serviceFileName };
            SetupProgress.reportStart("emb.registerservice", params);
            String strXML = getResourceContent(serviceFileName);
            // This string 'content' is to avoid plain text password
            // in the files copied to the config/xml directory.
            String content = strXML;
            if (tagswap) {
                content = StringUtils.strReplaceAll(content,
                    "@UM_DS_DIRMGRPASSWD@", "********");
                content =
                    ServicesDefaultValues.tagSwap(content, true);
            }
            if (tagswap) {
                strXML = ServicesDefaultValues.tagSwap(strXML, true);
            }

            // Write to file without visible password values.
            AMSetupServlet.writeToFile(dirXML + "/" + serviceFileName,
                content);

            // Write to directory server with original password 
            // values.
            upgradeService(strXML, adminToken);
            SetupProgress.reportEnd("emb.success", null);
        }
        
        if (!bUseExtUMDS) {
            addSubConfigForEmbeddedDS(adminToken);
        }
    }*/
    
    /**
     * Kick off the upgrade process
     * 
     * @param adminToken A valid admin SSOToken
     * @throws UpgradeException 
     */
    public void upgrade(SSOToken adminToken) 
    throws UpgradeException {            
        if (debug.messageEnabled()) {
            debug.message("Upgrade startup.");
        }
                
        ServiceUpgradeWrapper serviceChanges = preUpgradeProcessing(adminToken);
        
        if (serviceChanges != null) {
            generateUpgradeReport(adminToken, serviceChanges);
            processUpgrade(adminToken, serviceChanges);
        }
        
        if (debug.messageEnabled()) {
            debug.message("Upgrade complete.");
        }
    }
            
    protected void generateUpgradeReport(SSOToken adminToken, ServiceUpgradeWrapper serviceChanges) {
        StringBuilder buffer = new StringBuilder();
        
        if (serviceChanges.getServicesAdded() != null && 
                !serviceChanges.getServicesAdded().isEmpty()) {
            for (Map.Entry<String, Document> added : serviceChanges.getServicesAdded().entrySet()) {
                buffer.append("add service name: ").append(UpgradeUtils.getServiceName(added.getValue())).append("\n\n");
            }
        }

        if (serviceChanges.getServicesModified() != null &&
                !serviceChanges.getServicesModified().isEmpty()) {
            for (Map.Entry<String, Map<String, ServiceSchemaUpgradeWrapper>> mod : serviceChanges.getServicesModified().entrySet()) {
                buffer.append("mod service name:").append(mod.getKey()).append("\n");
                buffer.append("mod service details:\n");

                for (Map.Entry<String,ServiceSchemaUpgradeWrapper> serviceType : mod.getValue().entrySet()) {
                    buffer.append("service attr type: ").append(serviceType.getKey()).append("\n");

                    ServiceSchemaUpgradeWrapper sUpdate = serviceType.getValue(); 

                    if (sUpdate != null) {
                        if (sUpdate.getAttributesAdded() != null &&
                                sUpdate.getAttributesAdded().hasBeenModified()) {
                            buffer.append("attr added:\n");
                            buffer.append(calculateAttrModifications(sUpdate.getAttributesAdded()));

                        }

                        if (sUpdate.getAttributesModified() != null &&
                                sUpdate.getAttributesModified().hasBeenModified()) {
                            buffer.append("attr modified:\n");
                            buffer.append(calculateAttrModifications(sUpdate.getAttributesModified()));
                        }

                        if (sUpdate.getAttributesDeleted() != null &&
                                sUpdate.getAttributesDeleted().hasBeenModified()) {
                            buffer.append("attr deleted:\n");
                            buffer.append(calculateAttrModifications(sUpdate.getAttributesDeleted()));
                        }
                        
                        buffer.append("\n");
                    }
                }
            }
        }
        
        if (serviceChanges.getSubSchemasModified() != null &&
                !serviceChanges.getSubSchemasModified().isEmpty()) {
            for (Map.Entry<String, Map<String, SubSchemaUpgradeWrapper>> ssMod : serviceChanges.getSubSchemasModified().entrySet()) {
                buffer.append("mod service name: ").append(ssMod.getKey()).append("\n");
                buffer.append("new sub schema details:\n");
                
                for (Map.Entry<String,SubSchemaUpgradeWrapper> serviceType : ssMod.getValue().entrySet()) {
                    buffer.append("service attr type: ").append(serviceType.getKey()).append("\n");
                    
                    SubSchemaUpgradeWrapper ssUpdate = serviceType.getValue();
                    
                    if (ssUpdate != null) {
                        if (ssUpdate.getSubSchemasAdded() != null &&
                                ssUpdate.getSubSchemasAdded().subSchemaChanged()) {
                            buffer.append("sub schema added:\n");
                            buffer.append(calculateSubSchemaAdditions(ssUpdate.getSubSchemasAdded()));
                        }
                    }
                    
                    buffer.append("\n");
                }
            }
        }

        if (serviceChanges.getServicesDeleted() != null &&
                !serviceChanges.getServicesDeleted().isEmpty()) {
            buffer.append("services deleted:\n");

            for (String serviceName : serviceChanges.getServicesDeleted()) {
                buffer.append(serviceName).append("\n");
            }
        }
        
        try {
            buffer.append(ServerConfiguration.upgradeDefaults(adminToken, true));
        } catch (SSOException ssoe) {
            
        } catch (SMSException smse) {
            
        } catch (UnknownPropertyNameException upne) {
            
        }

        System.out.println(buffer.toString());
    }
    
    protected String calculateAttrModifications(ServiceSchemaModificationWrapper schemaMods) {
        StringBuilder buffer = new StringBuilder();
        
        if (!(schemaMods.getAttributes().isEmpty())) {
            for (AttributeSchemaImpl attrs : schemaMods.getAttributes()) {
                buffer.append(attrs.getName()).append("\n");
            }
        }
      
        if (schemaMods.hasSubSchema()) {
            for (Map.Entry<String, ServiceSchemaModificationWrapper> schema : schemaMods.getSubSchemas().entrySet()) {
                buffer.append("schema name: ").append(schema.getKey()).append("\n");

                if (!(schema.getValue().getAttributes().isEmpty())) {
                    for(AttributeSchemaImpl attrs : schema.getValue().getAttributes()) {
                        buffer.append(attrs.getName()).append("\n");
                    }
                }

                if (schema.getValue().hasSubSchema()) {
                    buffer.append(calculateAttrModifications(schema.getValue()));
                }            
            }
        }
        
        return buffer.toString();
    }
    
    protected String calculateSubSchemaAdditions(SubSchemaModificationWrapper subSchemaMods) {
        StringBuilder buffer = new StringBuilder();
        
        if (subSchemaMods.hasNewSubSchema()) {
            for (Map.Entry<String, NewSubSchemaWrapper> newSubSchema : subSchemaMods.entrySet()) {
                buffer.append("new subschema type: ").append(newSubSchema.getKey()).append("\n");
                buffer.append("new subschema name: ").append(newSubSchema.getValue().getSubSchemaName()).append("\n");
                buffer.append("new subschema value: ").append(XMLUtils.print(newSubSchema.getValue().getSubSchemaNode()));
            }
        }
        
        if (subSchemaMods.hasSubSchema()) {
            buffer.append(calculateSubSchemaAdditions(subSchemaMods.getSubSchema()));
        }
        
        return buffer.toString();
    }
    
    /*
     * Determine what needs to be upgraded in the configuration repository
     */
    protected ServiceUpgradeWrapper preUpgradeProcessing(SSOToken adminToken) 
    throws UpgradeException {
        Map map = ServicesDefaultValues.getDefaultValues();
        Map<String, Document> newServiceDefinitions = new HashMap<String, Document>();
        String basedir = (String)map.get(SetupConstants.CONFIG_VAR_BASE_DIR);
        String dirXML = basedir + "/config/xml";
        
        File xmlDirs = new File(dirXML);
        
        if (!xmlDirs.exists() || !xmlDirs.isDirectory()) {
            xmlDirs.mkdirs();
            
            if (debug.messageEnabled()) {
                debug.message("Created directory: " + xmlDirs);
            }
        }

        for (String serviceFileName : serviceNames) {
            boolean tagswap = true;
            
            if (serviceFileName.startsWith("*")) {
                serviceFileName = serviceFileName.substring(1);
                tagswap = false;
            }

            Object[] params = { serviceFileName };
            String strXML = null;
            
            try {
                strXML = getResourceContent(serviceFileName);
            } catch (IOException ioe) {
                debug.error("unable to load services file: " + serviceFileName, ioe);
                throw new UpgradeException(ioe);
            }

            // This string 'content' is to avoid plain text password
            // in the files copied to the config/xml directory.
            String content = strXML;
            
            if (tagswap) {
                content = StringUtils.strReplaceAll(content,
                    "@UM_DS_DIRMGRPASSWD@", "********");
                content =
                    ServicesDefaultValues.tagSwap(content, true);
            }
            
            if (tagswap) {
                strXML = ServicesDefaultValues.tagSwap(strXML, true);
            }
            
            Document serviceSchema = null;
            
            try {
                serviceSchema = fetchDocumentSchema(strXML, adminToken);
            } catch (IOException ioe) {
                debug.error("unable to load document schema", ioe);
                throw new UpgradeException(ioe);
            }
            
            newServiceDefinitions.put(UpgradeUtils.getServiceName(serviceSchema), serviceSchema);
        }
        
        return diffServiceVersions(newServiceDefinitions, adminToken);
    }
    
    protected void processUpgrade(SSOToken adminToken, ServiceUpgradeWrapper serviceChanges) 
    throws UpgradeException {
        if (serviceChanges.getServicesAdded() != null && 
            !serviceChanges.getServicesAdded().isEmpty()) {

            StringBuilder buffer = new StringBuilder();
            
            if (debug.messageEnabled()) {
                buffer.append("services to add: ");
            }
            
            for (Map.Entry<String, Document> serviceToAdd : serviceChanges.getServicesAdded().entrySet()) {
                StringBuilder serviceDefinition = new StringBuilder();
                serviceDefinition.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
                serviceDefinition.append("<!DOCTYPE ServicesConfiguration\n");
                serviceDefinition.append("PUBLIC \"=//iPlanet//Service Management Services (SMS) 1.0 DTD//EN\"\n");
                serviceDefinition.append("\"jar://com/sun/identity/sm/sms.dtd\">\n");
                serviceDefinition.append(XMLUtils.print(serviceToAdd.getValue()));
                UpgradeUtils.createService(serviceDefinition.toString(), adminToken);
                
                if (debug.messageEnabled()) {
                    buffer.append(serviceToAdd.getKey()).append(": ");
                }
            }
            
            if (debug.messageEnabled()) {
                debug.message("services to add: " + buffer.toString());
            }
        }
        
        if (serviceChanges.getServicesModified() != null &&
            !serviceChanges.getServicesModified().isEmpty()) {
            for (Map.Entry<String, Map<String, ServiceSchemaUpgradeWrapper>> serviceToModify : serviceChanges.getServicesModified().entrySet()) {
                UpgradeUtils.modifyService(serviceToModify.getKey(), serviceToModify.getValue(), adminToken);
                
                if (debug.messageEnabled()) {
                    debug.message("modified service: " + serviceToModify.getKey());
                }
            }
        }
        
        if (serviceChanges.getSubSchemasModified() != null &&
            !serviceChanges.getSubSchemasModified().isEmpty()) {
            for (Map.Entry<String, Map<String, SubSchemaUpgradeWrapper>> ssMod : serviceChanges.getSubSchemasModified().entrySet()) { 
                UpgradeUtils.addNewSubSchemas(ssMod.getKey(), ssMod.getValue(), adminToken);
                
                if (debug.messageEnabled()) {
                    debug.message("modified sub schema: " + ssMod.getKey());
                }
            }
        }
        
        if (serviceChanges.getServicesDeleted() != null &&
            !serviceChanges.getServicesDeleted().isEmpty()) {
            for (String serviceToDelete : serviceChanges.getServicesDeleted()) {
                UpgradeUtils.deleteService(serviceToDelete, adminToken);
                
                if (debug.messageEnabled()) {
                    debug.message("deleted service: " + serviceToDelete);
                }
            }
        }
        
        try {
            ServerConfiguration.upgradeDefaults(adminToken, false);
        } catch (SSOException ssoe) {
            debug.error("Unable to process service configuration upgrade", ssoe);
        } catch (SMSException smse) {
            debug.error("Unable to process service configuration upgrade", smse);
        } catch (UnknownPropertyNameException upne) {
            debug.error("Unable to process service configuration upgrade", upne);
        }
    }
    
    protected ServiceUpgradeWrapper diffServiceVersions(Map<String, Document> serviceDefinitions, SSOToken adminToken) 
    throws UpgradeException {
        ServiceSchemaModifications modifications = null;
        Set<String> newServiceNames = 
                listNewServices(serviceDefinitions.keySet(), UpgradeUtils.getExistingServiceNames(adminToken));
        
        Map<String, Document> sAdd = new HashMap<String, Document>();
        
        for (String newServiceName : newServiceNames) {
            sAdd.put(newServiceName, serviceDefinitions.get(newServiceName));
            
            if (debug.messageEnabled()) {
                debug.message("found new service: " + newServiceName);
            }
        }
                
        Map<String, Map<String, ServiceSchemaUpgradeWrapper>> sMod = 
                new HashMap<String, Map<String, ServiceSchemaUpgradeWrapper>>();
        Map<String, Map<String, SubSchemaUpgradeWrapper>> ssMod =
                new HashMap<String, Map<String, SubSchemaUpgradeWrapper>>();
        Set<String> deletedServices = 
                listDeletedServices(serviceDefinitions.keySet(), UpgradeUtils.getExistingServiceNames(adminToken));; 
        
        for (Map.Entry<String, Document> service : serviceDefinitions.entrySet()) { 
            // service is new, skip modification check
            if (newServiceNames.contains(service.getKey())) {
                continue;
            }
            
            // service has been removed, skip modification check
            if (deletedServices.contains(service.getKey())) {
                continue;
            }
            
            modifications = new ServiceSchemaModifications(service.getKey(), service.getValue(), adminToken);
            
            if (modifications.isServiceModified()) {
                sMod.put(service.getKey(), modifications.getServiceModifications());
            }
            
            if (modifications.hasSubSchemaChanges()) {
                ssMod.put(service.getKey(), modifications.getSubSchemaChanges());
            }
        }
        
        ServiceUpgradeWrapper serviceWrapper = new ServiceUpgradeWrapper(sAdd, sMod, ssMod, deletedServices);
        return serviceWrapper;
    }    
    
    protected Set<String> listNewServices(Set<String> serviceNames, Set<String> existingServices) {
        Set<String> newServiceNames = new HashSet<String>(serviceNames);
        
        if (newServiceNames.removeAll(existingServices)) {
            return newServiceNames;
        }
        
        return Collections.EMPTY_SET;
    }
    
    protected Set<String> listDeletedServices(Set<String> newServiceNames, Set<String> existingServiceNames) {
        if (existingServiceNames.removeAll(newServiceNames)) {
            return existingServiceNames;
        }
        
        return Collections.EMPTY_SET;
    }
    
    protected Document fetchDocumentSchema(String xmlContent, SSOToken adminToken) 
    throws UpgradeException, IOException {
        InputStream serviceStream = null;
        Document doc = null;
        
        try {
            serviceStream = (InputStream) new ByteArrayInputStream(xmlContent.getBytes());
            doc = UpgradeUtils.parseServiceFile(serviceStream, adminToken);
        } finally {
            if (serviceStream != null) {
                serviceStream.close();
            }
        }
        
        return doc;
    }
    
    protected String getResourceContent(String resName) 
    throws IOException {
        BufferedReader rawReader = null;
        String content = null;

        try {
            rawReader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(resName)));
            StringBuilder buff = new StringBuilder();
            String line = null;

            while ((line = rawReader.readLine()) != null) {
                buff.append(line).append("\n");
            }

            rawReader.close();
            rawReader = null;
            content = buff.toString();
        } finally {
            if (rawReader != null) {
                rawReader.close();
            }
        }
        
        return content;
    }
}
