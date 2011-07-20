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

package org.forgerock.openam.amsessionstore.db.opendj;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.forgerock.openam.amsessionstore.common.Constants;
import org.forgerock.openam.amsessionstore.common.Log;
import org.forgerock.openam.amsessionstore.db.StoreException;
import org.forgerock.openam.amsessionstore.db.opendj.setup.SetupProgress;
import org.opends.messages.Message;
import org.opends.server.core.AddOperation;
import org.opends.server.core.DeleteOperation;
import org.opends.server.core.DirectoryServer;
import org.opends.server.core.ModifyOperation;
import org.opends.server.extensions.ConfigFileHandler;
import org.opends.server.protocols.internal.InternalClientConnection;
import org.opends.server.protocols.internal.InternalSearchOperation;
import org.opends.server.protocols.ldap.LDAPAttribute;
import org.opends.server.protocols.ldap.LDAPModification;
import org.opends.server.types.DirectoryEnvironmentConfig;
import org.opends.server.util.EmbeddedUtils;
import org.opends.server.util.ServerConstants;
import org.opends.server.tools.InstallDS;
import org.opends.server.tools.dsconfig.DSConfig;
import org.opends.server.tools.dsreplication.ReplicationCliMain;
import org.opends.server.types.Attribute;
import org.opends.server.types.AttributeValue;
import org.opends.server.types.DereferencePolicy;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.ModificationType;
import org.opends.server.types.RawAttribute;
import org.opends.server.types.RawModification;
import org.opends.server.types.ResultCode;
import org.opends.server.types.SearchResultEntry;
import org.opends.server.types.SearchScope;

/**
 *
 * @author steve
 */
public class EmbeddedOpenDJ {    
    private static boolean serverStarted = false;
    private static List<LDAPAttribute> objectClasses;
    private static final String replDN = 
            "cn=all-servers,cn=Server Groups,cn=admin data";
    private static LinkedHashSet<String> serverAttrs;
    
    static {
        initialize();
    }
    
    static void initialize() {
        List<String> valueList = new ArrayList<String>();
        valueList.add(Constants.TOP);
        valueList.add(Constants.FR_AMSESSIONDB);
        LDAPAttribute ldapAttr= new LDAPAttribute(Constants.OBJECTCLASS, valueList);
        objectClasses = new ArrayList<LDAPAttribute>();
        objectClasses.add(ldapAttr);
        
        serverAttrs = new LinkedHashSet<String>();
        serverAttrs.add("cn");
        serverAttrs.add("jmxPort");
        serverAttrs.add("adminPort");
        serverAttrs.add("ldapPort");
        serverAttrs.add("replPort");
    }
    
    /**
     * Returns <code>true</code> if the server has already been started.
     *
     * @return <code>true</code> if the server has already been started.
     */ 
    public static boolean isStarted() {
        return serverStarted;
    }
    
    /**
     * Are we setting a secondary node that needs to replicate with the first?
     * 
     * @return True if we have an existing server id
     */
    public static boolean isMultiNode() {
        String replFlag = OpenDJConfig.getOpenDJSetupMap().get(Constants.EXISTING_SERVER_URL);
        
        if (replFlag != null) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns true if OpenDJ is configured
     * 
     * @return <code>true</code> if the server has already been installed.
     */
    public static boolean isInstalled() {
        File dbDir = new File(OpenDJConfig.getOdjRoot() + "/db/userRoot");
        
        return dbDir.exists() && dbDir.isDirectory();
    }
    
    /**
     * Sets up embedded OpenDJ during initial installation :
     * <ul>
     * <li>lays out the filesystem directory structure needed by OpenDJ
     * <li>sets up port numbers for ldap and replication
     * <li>invokes <code>EmbeddedUtils</code> to start the embedded server.
     * </ul>
     *
     *  @param map Map of properties collected by the configurator.
     *  @param servletCtx Servlet Context to read deployed war contents.
     *  @throws Exception on encountering errors.
     */
    public static void setup(String odjRoot)
    throws Exception {
        new File(odjRoot).mkdir();

        Log.logger.log(Level.FINE, "Starting OpenDJ Setup");
        ZipFile opendjZip = new ZipFile("../install/opendj.zip");
        Enumeration files = opendjZip.entries();

        while (files.hasMoreElements()) {
            ZipEntry file = (ZipEntry) files.nextElement();
            File f = new File(odjRoot + "/" + file.getName());

            if (file.isDirectory()) {
                f.mkdir();
                continue;
            }

            BufferedInputStream is =
                    new BufferedInputStream(opendjZip.getInputStream(file), 10000);
            BufferedOutputStream fos =
                    new BufferedOutputStream(new java.io.FileOutputStream(f), 10000);

            try {
                while (is.available() > 0) {
                    fos.write(is.read());
                }
            } catch (IOException ioe) {
                Log.logger.log(Level.SEVERE, "Error copying zip contents", ioe);
                throw ioe;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception ex) {
                        //No handling requried
                    }
                }

                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception ex) {
                        //No handling requried
                    }
                }
            }

            if (file.getName().endsWith("sh") || file.getName().startsWith("bin")) {
                f.setExecutable(true);
            }
        }
        
        // create tag swapped files
        String[] tagSwapFiles = {
            "../ldif/amsessiondb_suffix.ldif.template"
        };

        for (int i = 0 ; i < tagSwapFiles.length; i++) {
            String fileIn = tagSwapFiles[i];
            FileReader fin = new FileReader(fileIn);

            StringBuilder sbuf = new StringBuilder();
            char[] cbuf = new char[1024];
            int len;
            
            while ((len = fin.read(cbuf)) > 0) {
                sbuf.append(cbuf, 0, len);
            }
            
            FileWriter fout = null;

            try {
                fout = new FileWriter(odjRoot + "/" +
                        tagSwapFiles[i].substring(0, tagSwapFiles[i].indexOf(".template")));
                String inpStr = sbuf.toString();
                fout.write(tagSwap(inpStr));
            } catch (IOException ioe) {
                Log.logger.log(Level.SEVERE, "EmbeddedOpenDJ.setup(): Error tag swapping files", ioe);
                throw ioe;
            } finally {
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (Exception ex) {
                        //No handling requried
                    }
                }
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (Exception ex) {
                        //No handling requried
                    }
                }
            }
        }
        
        // copy schema files
        String[] opendjSchemaFiles = {
            "98-amsessiondb.ldif"
        };

        for (int i = 0 ; i < opendjSchemaFiles.length; i++) {
            String jarFileName = "../ldif/" + opendjSchemaFiles[i];
            ReadableByteChannel inChannel =
                    Channels.newChannel(new FileInputStream(jarFileName));
            FileChannel outChannel = new FileOutputStream(odjRoot + "/config/schema/" + opendjSchemaFiles[i]).getChannel();

            try {
                channelCopy(inChannel, outChannel);
            } catch (IOException ioe) {
                Log.logger.log(Level.SEVERE, "EmbeddedOpenDS.setup(): Error copying schema file(s)", ioe);
                throw ioe;
            } finally {
                if (inChannel != null) {
                    try {
                        inChannel.close();
                    } catch (Exception ex) {
                        //No handling requried
                    }
                }

                if (outChannel != null) {
                    try {
                        outChannel.close();
                    } catch (Exception ex) {
                        //No handling requried
                    }
                }
            }
        }

        // now setup OpenDJ
        System.setProperty("org.opends.quicksetup.Root", odjRoot);
        System.setProperty(ServerConstants.PROPERTY_SERVER_ROOT, odjRoot);
        System.setProperty(ServerConstants.PROPERTY_INSTANCE_ROOT, odjRoot);
        EmbeddedOpenDJ.setupOpenDS(odjRoot + "/config/config.ldif", OpenDJConfig.getOpenDJSetupMap());

        Object[] params = { odjRoot };
        EmbeddedOpenDJ.startServer(odjRoot);

        // Check: If adding a new server to a existing cluster

        if (OpenDJConfig.getExistingServerUrl() == null) {
            // Default: single / first server.
            Log.logger.log(Level.FINE, "Configuring First Server");
            int ret = EmbeddedOpenDJ.loadLDIF(OpenDJConfig.getOpenDJSetupMap(), odjRoot, "../ldif/amsessiondb_suffix.ldif");
            
            if (ret == 0) {
                Log.logger.log(Level.FINE, "amsessiondb suffix created successfully.");
            } else {
                Object[] error = { Integer.toString(ret) };
                Log.logger.log(Level.SEVERE, "EmbeddedOpenDJ.setupOpenDJ. Error loading amsessiondb suffix", error);
                throw new StoreException("Unable to create amsessiondb suffix");
            }
            
            registerServer(OpenDJConfig.getHostUrl());
        }
    }
    
    private static String tagSwap(String inpStr) {
        inpStr = inpStr.replaceAll("@" + Constants.OPENDJ_SUFFIX_TAG + "@", OpenDJConfig.getSessionDBSuffix());
        inpStr = inpStr.replaceAll("@" + Constants.OPENDJ_RDN_TAG + "@", calculateRDNValue(OpenDJConfig.getSessionDBSuffix()));
        
        return inpStr;
    }
    
    private static String calculateRDNValue(String dn) {
        if (dn.indexOf(',') == -1) {
            if (dn.indexOf('=') == -1) {
                return dn;
            } else {
                return dn.substring(dn.indexOf('=') + 1);
            }
        }
        
        if (dn.indexOf('=') == -1) {
            return dn.substring(0, dn.indexOf(','));
        } else {
            return dn.substring(dn.indexOf('=') + 1, dn.indexOf(','));
        }
        
    }
    
    protected static void channelCopy(ReadableByteChannel from, WritableByteChannel to)
    throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

        while (from.read(buffer) != -1) {
            buffer.flip();
            to.write(buffer);
            buffer.compact();
        }

        buffer.flip();

        while (buffer.hasRemaining()) {
            to.write(buffer);
        }
    }
    
    /**
     *  Utility function to preload data in the embedded instance.
     *  Must be called when the directory instance is shutdown.
     *
     *  @param odsRoot Local directory where <code>OpenDS</code> is installed.
     *  @param ldif Full path of the ldif file to be loaded.
     *
     */
    public static int loadLDIF(Map map, String odjRoot, String ldif) {
        int ret = 0;

        try {
            Object[] error = { ldif };
            Log.logger.log(Level.FINE, "Loading amsessiondb ldif ", error);

            String[] args1 = 
            { 
                "-C",                                               // 0
                "org.opends.server.extensions.ConfigFileHandler",   // 1
                "-f",                                               // 2
                odjRoot + "/config/config.ldif",                    // 3
                "-n",                                               // 4
                "userRoot",                                         // 5
                "-l",                                               // 6
                ldif,                                               // 7
                "-Q",                                               // 8
                "--trustAll",                                       // 9
                "-D",                                               // 10
                "cn=Directory Manager",                             // 11
                "-w",                                               // 12
                "password"                                          // 13
            };
            args1[11] = (String) map.get(Constants.OPENDJ_DS_MGR_DN);
            args1[13] = (String) map.get(Constants.OPENDJ_DS_MGR_PASSWD);
            ret = org.opends.server.tools.ImportLDIF.mainImportLDIF(args1, false,
                    SetupProgress.getOutputStream(), SetupProgress.getOutputStream());

            Log.logger.log(Level.FINE, "amsessiondb ldif loading, success");
        } catch (Exception ex) {
            Log.logger.log(Level.SEVERE,"amsessiondb ldif loading failed", ex);
        }

        return ret;
    }
    
    /**
     * Runs the OpenDJ setup command to create our instance
     *
     * @param configFile path to config.ldif
     * @param map The map of configuration options
     * @throws Exception upon encountering errors.
     */
    public static void setupOpenDS(String configFile, Map<String, String> map)
    throws Exception {
        Log.logger.log(Level.FINE, "Starting OpenDJ setup");

        int ret = runOpenDSSetup(map, configFile);

        if (ret == 0) {
            Log.logger.log(Level.FINE, "OpenDJ setup complete");
        } else {
            Object[] params = {Integer.toString(ret)};
            Log.logger.log(Level.FINE, 
                "EmbeddedOpenDS.setupOpenDS. Error setting up OpenDS {0}", params);
            throw new StoreException("Unable to setup OpenDJ");
        }
    }
    
     /**
      * Runs the OpenDJ setup command like this:
      * $ ./setup --cli --adminConnectorPort 4444
      * --baseDN dc=opensso,dc=java,dc=net --rootUserDN "cn=directory manager"
      * --doNotStart --ldapPort 50389 --skipPortCheck --rootUserPassword xxxxxxx
      * --jmxPort 1689 --no-prompt
      *
      *  @param map Map of properties collected by the configurator.
      *  @return status : 0 == success, !0 == failure
      */
    public static int runOpenDSSetup(Map<String, String> map, String configFile) {
        String[] setupCmd= {
            "--cli",                        // 0
            "--adminConnectorPort",         // 1
            "5444",                         // 2
            "--baseDN",                     // 3
            "o=amsessiondb",                // 4
            "--rootUserDN",                 // 5
            "cn=Directory Manager",         // 6
            "--ldapPort",                   // 7
            "60389",                        // 8
            "--skipPortCheck",              // 9
            "--rootUserPassword",           // 10
            "xxxxxxx",                      // 11
            "--jmxPort",                    // 12
            "2689",                         // 13
            "--no-prompt",                  // 14
            "--configFile",                 // 15
            "/path/to/config.ldif",         // 16
            "--doNotStart"                  // 17
        };

        setupCmd[2] = map.get(Constants.OPENDJ_ADMIN_PORT);
        setupCmd[4] = OpenDJConfig.getSessionDBSuffix();
        setupCmd[6] = map.get(Constants.OPENDJ_DS_MGR_DN);
        setupCmd[8] = map.get(Constants.OPENDJ_LDAP_PORT);
        setupCmd[13] = map.get(Constants.OPENDJ_JMX_PORT);
        setupCmd[16] = configFile;

        Object[] params = { concat(setupCmd) };
        Log.logger.log(Level.FINE, "Running OpenDJ Setup command {0}", params);

        setupCmd[11] = map.get(Constants.OPENDJ_DS_MGR_PASSWD);

        int ret = InstallDS.mainCLI(
            setupCmd, true,
            SetupProgress.getOutputStream(),
            SetupProgress.getOutputStream(),
            null);

        if (ret == 0) {
            Log.logger.log(Level.FINE, "OpenDJ setup successfully.");
        } else {
            Log.logger.log(Level.WARNING, "OpenDJ setup failed.");
        }

        return ret;
    }
    
    public static void registerServer(String serverUrl) 
    throws StoreException {
        InternalClientConnection icConn = InternalClientConnection.getRootConnection();
        
        List<RawAttribute> attrList = createLocalServerEntry();
        StringBuilder dn = new StringBuilder();
        dn.append(Constants.HOST_NAMING_ATTR).append(Constants.EQUALS).append(serverUrl);
        dn.append(Constants.COMMA).append(Constants.HOSTS_BASE_DN);
        dn.append(Constants.COMMA).append(OpenDJConfig.getSessionDBSuffix());
        
        attrList.addAll(objectClasses);
        AddOperation ao = icConn.processAdd(dn.toString(), attrList);
        ResultCode resultCode = ao.getResultCode();
        
        if (resultCode == ResultCode.SUCCESS) {
            Object[] params = { dn };
            Log.logger.log(Level.FINE, "Successfully created" +" entry: {0}", params);
        } else if (resultCode == ResultCode.ENTRY_ALREADY_EXISTS) {
            Object[] params = { dn };
            Log.logger.log(Level.WARNING, " unable to create: Entry " +
                        "Already Exists Error for DN {0}", params);
        } else {
            Object[] params = { dn, resultCode };
            Log.logger.log(Level.WARNING, "Error creating entry: {0}, error code = {1}", params);
            throw new StoreException("Unable to create entry: " + dn);
        }
    }
    
    public static Set<AMSessionDBOpenDJServer> getServers() 
    throws StoreException {
        InternalClientConnection icConn = InternalClientConnection.getRootConnection();
        Set<AMSessionDBOpenDJServer> serverList = new HashSet<AMSessionDBOpenDJServer>();
        StringBuilder baseDn = new StringBuilder();
        baseDn.append(Constants.HOSTS_BASE_DN);
        baseDn.append(Constants.COMMA).append(OpenDJConfig.getSessionDBSuffix());
        
        try {
            InternalSearchOperation iso = icConn.processSearch(baseDn.toString(),
                SearchScope.SINGLE_LEVEL, DereferencePolicy.NEVER_DEREF_ALIASES,
                0, 0, false, "objectclass=*" , serverAttrs);
            ResultCode resultCode = iso.getResultCode();

            if (resultCode == ResultCode.SUCCESS) {
                LinkedList<SearchResultEntry> searchResult = iso.getSearchEntries();
                
                if (!searchResult.isEmpty()) {
                    for (SearchResultEntry entry : searchResult) {
                        List<Attribute> attributes = entry.getAttributes();
                        AMSessionDBOpenDJServer server = new AMSessionDBOpenDJServer();
                        
                        for (Attribute attribute : attributes) {
                            if (attribute.getName().equals("cn")) {
                                server.setHostName(getFQDN(attribute.iterator().next().getValue().toString()));
                            } else if (attribute.getName().equals("adminPort")) {
                                server.setAdminPort(attribute.iterator().next().getValue().toString());
                            } else if (attribute.getName().equals("jmxPort")) {
                                server.setJmxPort(attribute.iterator().next().getValue().toString());
                            } else if (attribute.getName().equals("ldapPort")) {
                                server.setLdapPort(attribute.iterator().next().getValue().toString());
                            } else if (attribute.getName().equals("replPort")) {
                                server.setReplPort(attribute.iterator().next().getValue().toString());
                            } else {
                                Object[] error = { attribute.getName() };
                                Log.logger.log(Level.WARNING, "Unknown attribute: {0}", error);
                            }
                        }
                        
                        serverList.add(server);
                    }
                } 
            } else if (resultCode == ResultCode.NO_SUCH_OBJECT) {
                Object[] error = { baseDn };
                Log.logger.log(Level.FINE,"Entry not present: {0}", error);
                
                return null;
            } else {
                Object[] error = { baseDn, resultCode };
                Log.logger.log(Level.WARNING, "Error in accessing entry DN: {0}, error code = {1}", error);
                throw new StoreException("Unable to access entry DN" + baseDn);
            }
        } catch (DirectoryException dex) {
            Object[] error = { baseDn };
            Log.logger.log(Level.WARNING, "Error in accessing entry DN: {0}", error);
            throw new StoreException("Unable to read record from store", dex);
        }
        
        return serverList;
    }
    
    private static String getFQDN(String urlHost) {
        URL url = null;
        
        try {
            url = new URL(urlHost);
        } catch (MalformedURLException mue) {
            Object[] error = { urlHost };
            Log.logger.log(Level.WARNING, "Malformed URL: {0}", error);
            return urlHost;
        }
        
        return url.getHost();
    }
    
    private static List<RawAttribute> createLocalServerEntry() {
        Map<String, String> odjSetupMap = OpenDJConfig.getOpenDJSetupMap();
        
        List<RawAttribute> attrList = new ArrayList<RawAttribute>(odjSetupMap.size());
        
        for (Map.Entry<String, String> entry : odjSetupMap.entrySet()) {
            if (entry.getKey().equals(Constants.OPENDJ_ADMIN_PORT) ||
                entry.getKey().equals(Constants.OPENDJ_LDAP_PORT) ||
                entry.getKey().equals(Constants.OPENDJ_JMX_PORT) ||
                entry.getKey().equals(Constants.OPENDJ_REPL_PORT)) {
                List<String> valueList = new ArrayList<String>();
                valueList.add(entry.getValue());
                attrList.add(new LDAPAttribute(getAttrName(entry.getKey()), valueList));
            }
        }
        
        return attrList;
    }
    
    private static String getAttrName(String key) {
        if (key.equals(Constants.OPENDJ_ADMIN_PORT)) {
            return OpenDJConfig.AmSessionDbAttr.ADMIN_PORT.toString();
        } else if (key.equals(Constants.OPENDJ_LDAP_PORT)) {
            return OpenDJConfig.AmSessionDbAttr.LDAP_PORT.toString();
        } else if (key.equals(Constants.OPENDJ_JMX_PORT)) {
            return OpenDJConfig.AmSessionDbAttr.JMX_PORT.toString();
        } else if (key.equals(Constants.OPENDJ_REPL_PORT)) {
            return OpenDJConfig.AmSessionDbAttr.REPL_PORT.toString();
        } else {
            Log.logger.log(Level.WARNING, "Invalid map attr: {0}", key);
            return "";
        }
    }
    
    public static void unregisterServer(String serverUrl) {
        InternalClientConnection icConn = InternalClientConnection.getRootConnection();
        
        StringBuilder dn = new StringBuilder();
        dn.append(Constants.HOST_NAMING_ATTR).append(Constants.EQUALS).append(serverUrl);
        dn.append(Constants.COMMA).append(Constants.HOSTS_BASE_DN);
        dn.append(Constants.COMMA).append(OpenDJConfig.getSessionDBSuffix());
        
        DeleteOperation dop = icConn.processDelete(dn.toString());
        ResultCode resultCode = dop.getResultCode();
        
        if (resultCode != ResultCode.SUCCESS) {
            Log.logger.log(Level.WARNING, "Unable to delete entry: {0}", dn);
        }
    }
    
    public static void setupReplication(Map<String, String> localMap, Map<String, String> remoteMap)
    throws Exception {
        int ret = setupReplicationEnable(localMap, remoteMap);
        
        if (ret == 0) {
            ret = setupReplicationInitialize(localMap, remoteMap);
            Log.logger.log(Level.FINE, "replication setup succeeded.");
        } else {
            Log.logger.log(Level.FINE, "Error setting up replication: {0}", ret);
            throw new StoreException("Unable to setup replication: error=" + ret);
        }
    }
    
   /**
      * Setup replication between two OpenDJ amsessiondb stores.
      * $ dsreplication enable
      *    --no-prompt
      *    --host1 host1 --port1 1389 --bindDN1 "cn=Directory Manager"
      *    --bindPassword1 password --replicationPort1 8989
      *    --host2 host2 --port2 2389 --bindDN2 "cn=Directory Manager"
      *    --bindPassword2 password --replicationPort2 8990 
      *    --adminUID admin --adminPassword password 
      *    --baseDN "dc=amsessiondb,dc=com"
      *
      *
      *  @param map Map of configuration properties 
      *  @return status : 0 == success, !0 == failure
      */
    public static int setupReplicationEnable(Map<String, String> localMap, Map<String, String> remoteMap) {
        String[] enableCmd= {
            "enable",                // 0
            "--no-prompt",           // 1
            "--host1",               // 2
            "host1val",              // 3
            "--port1",               // 4
            "port1ival",             // 5
            "--bindDN1",             // 6
            "cn=Directory Manager",  // 7
            "--bindPassword1",       // 8
            "xxxxxxxx",              // 9
            "--replicationPort1",    // 10
            "8989",                  // 11
            "--host2",               // 12
            "host2val",              // 13
            "--port2",               // 14
            "port2ival",             // 15
            "--bindDN2",             // 16
            "cn=Directory Manager",  // 17
            "--bindPassword2",       // 18
            "xxxxxxxx",              // 19
            "--replicationPort2",    // 20
            "8989",                  // 21
            "--adminUID",            // 22
            "admin",                 // 23
            "--adminPassword",       // 24
            "xxxxxxxx",              // 25 
            "--baseDN",              // 26
            "dc=example,dc=com",     // 27
            "--trustAll",            // 28
            "--configFile",          // 29
            "path/to/config.ldif"    // 30
        };
        enableCmd[3] = remoteMap.get(Constants.HOST_FQDN);
        enableCmd[5] = remoteMap.get(Constants.OPENDJ_ADMIN_PORT);
        enableCmd[11] = remoteMap.get(Constants.OPENDJ_REPL_PORT);
        enableCmd[13] = localMap.get(Constants.HOST_FQDN);
        enableCmd[15] = localMap.get(Constants.OPENDJ_ADMIN_PORT);
        enableCmd[21] = localMap.get(Constants.OPENDJ_REPL_PORT);
        enableCmd[27] = localMap.get(Constants.OPENDJ_SUFFIX);
        enableCmd[9] = localMap.get(Constants.OPENDJ_DS_MGR_PASSWD);
        enableCmd[19] = localMap.get(Constants.OPENDJ_DS_MGR_PASSWD);
        enableCmd[25] = localMap.get(Constants.OPENDJ_DS_MGR_PASSWD);
        enableCmd[30] = localMap.get(Constants.OPENDJ_ROOT) + "/config/config.ldif";

        Object[] params = { enableCmd[3] };
        Log.logger.log(Level.FINE, "Host 1 {0}", params);
        Object[] params2 = { enableCmd[13] };
        Log.logger.log(Level.FINE, "Host 2 {0}", params2);
        Object[] params3 = { enableCmd[5] };
        Log.logger.log(Level.FINE, "Port 1 {0}", params3);
        Object[] params4 = { enableCmd[15] };
        Log.logger.log(Level.FINE, "Port 2 {0}", params4);
        
        int ret = ReplicationCliMain.mainCLI(
            enableCmd, false, 
            SetupProgress.getOutputStream(), 
            SetupProgress.getOutputStream(), 
            null);         

        if (ret == 0) {
            SetupProgress.reportEnd("OpenDJ replication setup successfully.", null);
        } else {
            SetupProgress.reportEnd("OpenDJ replication setup failed.", null);
        }
        
        return ret;
    }
    
    /**
      * Syncs replication data between two OpenDJ amsessiondb stores.
      * $ dsreplication initialize 
      *     --baseDN "dc=amsessiondb,dc=com" --adminUID admin --adminPassword pass
      *     --hostSource host1 --portSource 1389
      *     --hostDestination host2 --portDestination 2389
      *     --trustAll
      *
      *  @param map Map of configuration properties 
      *  @return status : 0 == success, !0 == failure
      */
    public static int setupReplicationInitialize(Map<String, String> localMap, Map<String, String> remoteMap) {
        String[] initializeCmd= {
            "initialize",                 // 0
            "--no-prompt",                // 1
            "--baseDN",                   // 2
            "dc=amsessiondb,dc=net",      // 3
            "--adminUID",                 // 4
            "admin",                      // 5
            "--adminPassword",            // 6
            "xxxxxxxx",                   // 7
            "--hostSource",               // 8
            "localhost",                  // 9
            "--portSource",               // 10
            "50389",                      // 11
            "--hostDestination",          // 12
            "localhost",                  // 13
            "--portDestination",          // 14
            "51389",                      // 15
            "--trustAll",                 // 16
            "--configFile",               // 17
            "path/to/config.ldif"         // 18
        };
        initializeCmd[3] = localMap.get(Constants.OPENDJ_SUFFIX);
        initializeCmd[9] = remoteMap.get(Constants.HOST_FQDN);
        initializeCmd[11] = remoteMap.get(Constants.OPENDJ_ADMIN_PORT);
        initializeCmd[13] = localMap.get(Constants.HOST_FQDN);
        initializeCmd[15] = localMap.get(Constants.OPENDJ_ADMIN_PORT);
        initializeCmd[18] = localMap.get(Constants.OPENDJ_ROOT) + "/config/config.ldif";

        initializeCmd[7] = localMap.get(Constants.OPENDJ_DS_MGR_PASSWD);
        int ret = ReplicationCliMain.mainCLI(initializeCmd, false,
            SetupProgress.getOutputStream(), SetupProgress.getOutputStream(),
            null); 

        if (ret == 0) {
            Log.logger.log(Level.FINE, "Replication Setup Success.");
        } else {
            Log.logger.log(Level.SEVERE, "Replication Setup Failed.");
        }
        
        return ret;
    }

    /**
     * Returns Replication Status by invoking OpenDJ <code>dsreplication</code>
     * CLI
     * @param port LDAP port number of embedded OpenDJ
     * @param passwd Directory Manager password
     * @param oo Standard output
     * @param err : Standard error
     * @return <code>dsreplication</code> CLI exit code.
     */
    public static int getReplicationStatus(String port, 
                                           String passwd,
                                           OutputStream oo, 
                                           OutputStream err) {
        String[] statusCmd= {
                              "status","--no-prompt",
                              "-h",  "localhost",
                              "-p",  port,
                              "--adminUID", "admin",
                              "--adminPassword",  passwd,
                              "-s"
                            };

        String dbgcmd = concat(statusCmd).replaceAll(passwd, "****");
        Object[] params = { dbgcmd };
        Log.logger.log(Level.FINE, "exec dsreplication : {0}", params);
        
        int ret = ReplicationCliMain.mainCLI(statusCmd, false, oo, err, null);
        Object[] params2 = { ret }; 
        Log.logger.log(Level.FINE, "dsreplication ret: {0}", params2);
        return ret;
    }
    
    /**
     * Synchronizes replication domain info with current list of opensso servers. 
     */
    public static boolean syncReplicatedDomains(
                          Set<String> currServerSet, String port, String passwd) {
        Log.logger.log(Level.FINE, "Domains:started");
        
        String[] args = {
            "-p", port,      // 1 : ds port num
            "-h", "localhost", 
            "-D",  "cn=directory manager",
            "-w", passwd,    // 7 : password
            "list-replication-domains",
            "--provider-name", "Multimaster Synchronization",
            "--property", "replication-server",
            "--no-prompt",
            "--trustAll"
        };

        String dbgcmd = concat(args).replaceAll(passwd, "****");
        Object[] params = { dbgcmd };
        Log.logger.log(Level.FINE, "exec dsconfig: {0}", params);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayOutputStream boe = new ByteArrayOutputStream();
        DSConfig.main(args, false, bos, boe);
        String str = bos.toString();
        String stre = boe.toString();
        
        if (stre.length() != 0) {
            Object[] params2 = { stre };
            Log.logger.log(Level.WARNING, "EmbeddedOpenDS:syncReplication:stderr: {0}", params2);
        }
        
        BufferedReader brd = new BufferedReader(new StringReader(str));
        String line = null;
        
        try {
            line = brd.readLine(); // 1st line
            line = brd.readLine(); // 2nd line
            
            while ((line = brd.readLine()) != null) {
                try {
                    int dcolon = line.indexOf(':');
                    String domainname = line.substring(0, dcolon).trim();
                    int stcolon = line.indexOf(':', dcolon+1);
                    String replservers = line.substring(stcolon+1);
                    
                    Object[] params3 = { domainname, replservers };
                    Log.logger.log(Level.FINE, "domain={0} replservers={1}", params3);
                    
                    StringTokenizer stok = new StringTokenizer(replservers, ",");
                    
                    // Check if this server is part of server list
                    List<String> cmdlist = new ArrayList<String>();
                    cmdlist.add("-p"); 
                    cmdlist.add(port);
                    cmdlist.add("-h"); 
                    cmdlist.add("localhost"); 
                    cmdlist.add("-D");  
                    cmdlist.add("cn=directory manager");
                    cmdlist.add("-w"); 
                    cmdlist.add(passwd);
                    cmdlist.add("--no-prompt");
                    cmdlist.add("--trustAll");
                    cmdlist.add("set-replication-domain-prop");
                    cmdlist.add("--provider-name"); 
                    cmdlist.add("Multimaster Synchronization");
                    cmdlist.add("--domain-name"); 
                    cmdlist.add(domainname);
    
                    int numremoved = 0;
                    while (stok.hasMoreTokens()) {
                        String tok = stok.nextToken().trim();
                        if (!currServerSet.contains(tok)) {
                            cmdlist.add("--remove"); 
                            cmdlist.add("replication-server:"+tok);
                            numremoved++;
                        }
                    }
                    
                    if (numremoved > 0) {
                        String[] args1 = cmdlist.toArray(new String[cmdlist.size()]);

                        String dbgcmd1 = concat(args1).replaceAll(passwd, "****");
                        Object[] error = { dbgcmd1 };
                        Log.logger.log(Level.FINE, " Execute: {0}", error);
                        
                        bos = new ByteArrayOutputStream();
                        boe = new ByteArrayOutputStream();
                        DSConfig.main(args1, false, bos, boe);
                        str = bos.toString();
                        stre = boe.toString();
                        
                        if (stre.length() != 0) {
                            Object[] params2 = { stre };
                            Log.logger.log(Level.WARNING, "EmbeddedOpenDS:syncRepl:stderr={0}", params2);
                        }
                        
                        Object[] params4 = { str };
                        Log.logger.log(Level.FINE, "syncReplication:Result: {0}", params4);
                    } 
                } catch (Exception ex) {
                    Log.logger.log(Level.SEVERE, "EmbeddedOpenDS:syncReplication:Failed:", ex);
                    return false;
                } 
            }
        } catch (Exception ex) {
            Log.logger.log(Level.SEVERE, "EmbeddedOpenDS:syncReplication:Failed:", ex);
            return false;
        } 
        return true;
    }
    
    /**
     * Synchronizes replication domain info with current list of opensso servers. 
     */
    public static boolean syncReplicatedServerList(
                          Set<String> currServerSet, String port, String passwd) {
        InternalClientConnection icConn = InternalClientConnection.getRootConnection();
        
        try {
            Set<String> dsServers = getServerSet(icConn);

            if (dsServers == null) {
                return false;
            }
            
            for (String tok : dsServers) {
                if (!currServerSet.contains(tok)) {
                    delOpenDJServer(icConn, tok);
                }
            }
        } catch (Exception ex) {
            Log.logger.log(Level.SEVERE, "Unable to delete OpenDJ replication instance.", ex);
            return false;
        }
        
        return true;
    }
    
    /**
     *  Removes host:port from OpenDJ replication
     */
    public static void delOpenDJServer(InternalClientConnection icConn, String delServer) 
    throws StoreException {
        String replServerDN = "cn=" + delServer + ",cn=Servers,cn=admin data";
        LinkedHashSet<String> returnAttrs = new LinkedHashSet<String>();
        returnAttrs.add("ds-cfg-key-id");
 
        if (icConn == null) {
            Object[] params = { replServerDN };
            Log.logger.log(Level.SEVERE, "Could not connect to local opends instance. {0}", params);
            return;
        }
        
        String trustKey = null;
        
        try {
            InternalSearchOperation iso = icConn.processSearch(replDN,
                SearchScope.BASE_OBJECT, DereferencePolicy.NEVER_DEREF_ALIASES,
                0, 0, false, "objectclass=*" , returnAttrs);
            ResultCode resultCode = iso.getResultCode();

            if (resultCode == ResultCode.SUCCESS) {
                LinkedList<SearchResultEntry> searchResult = iso.getSearchEntries();
                
                if (!searchResult.isEmpty()) {
                    SearchResultEntry entry = searchResult.get(0);
                    List<Attribute> attributes = entry.getAttributes();
                    
                    for (Attribute attribute : attributes) {
                        trustKey = attribute.toString();
                    }
                }
            } else if (resultCode == ResultCode.NO_SUCH_OBJECT) {
                Object[] error = { replDN };
                Log.logger.log(Level.FINE, "Entry not present: {0}", error);
            } else {
                Object[] error = { replDN, resultCode };
                Log.logger.log(Level.WARNING, "Error in accessing entry DN: {0} , error code = ", error);
                throw new StoreException("Unable to access entry DN" + replDN);
            }
        } catch (DirectoryException dex) {
            Log.logger.log(Level.WARNING, "Error in accessing entry DN: " + replDN, dex);
            throw new StoreException("Unable to read record from store", dex);
        }
        
        if (trustKey != null) {
            String keyDN = "ds-cfg-key-id=" + trustKey + ",cn=instance keys,cn=admin data";
            DeleteOperation dop = icConn.processDelete(keyDN);
            ResultCode resultCode = dop.getResultCode();
        
            if (resultCode != ResultCode.SUCCESS) {
                Object[] error = { keyDN };
                Log.logger.log(Level.WARNING, "Unable to delete entry: {0}", error);
            }
        }
        
        DeleteOperation dop = icConn.processDelete(replServerDN);
        ResultCode resultCode = dop.getResultCode();
        
        if (resultCode != ResultCode.SUCCESS) {
            Object[] error = { replServerDN };
            Log.logger.log(Level.WARNING, "Unable to delete entry: {0}", error);
        }
        
        RawAttribute toDelete = new LDAPAttribute("uniqueMember", "cn="+ delServer);
        RawModification mod = new LDAPModification(ModificationType.DELETE, toDelete);
        List<RawModification> mods = new ArrayList<RawModification>();
        mods.add(mod);
        ModifyOperation mop = icConn.processModify(replDN,  mods);
        resultCode = mop.getResultCode();
        
        if (resultCode != ResultCode.SUCCESS) {
            Object[] error = { replDN };
            Log.logger.log(Level.WARNING, "Unable to modify entry: {0}", replDN);
        }
    }
    
    /**
     *  Gets list of replicated servers from local OpenDJ directory.
     */
    public static Set<String> getServerSet(InternalClientConnection icConn) 
    throws StoreException {
        LinkedHashSet<String> returnAttrs = new LinkedHashSet<String>();
        returnAttrs.add("uniqueMember");
        
        if (icConn != null) {
            try {
                InternalSearchOperation iso = icConn.processSearch(replDN,
                    SearchScope.BASE_OBJECT, DereferencePolicy.NEVER_DEREF_ALIASES,
                0, 0, false, "objectclass=*" , returnAttrs);
                ResultCode resultCode = iso.getResultCode();

                if (resultCode == ResultCode.SUCCESS) {
                    LinkedList<SearchResultEntry> searchResult = iso.getSearchEntries();
                
                    if (!searchResult.isEmpty()) {
                        Set<String> hostSet = new HashSet<String>();
                    
                        for (SearchResultEntry entry : searchResult) {
                            Map<String, Set<String>> resultsMap = 
                                    EmbeddedSearchResultIterator.convertLDAPAttributeSetToMap(entry.getAttributes());
                            
                            for (Map.Entry<String, Set<String>> attribute : resultsMap.entrySet()) {
                                Set<String> values = attribute.getValue();
                                
                                for (String value : values) {
                                    hostSet.add(value.substring(3, value.length()));
                                }
                            }
                        }
                    
                        return hostSet;
                    } else {
                        return null;
                    }
                } else if (resultCode == ResultCode.NO_SUCH_OBJECT) {
                    Object[] param = { replDN };
                    Log.logger.log(Level.FINE, "Entry not present: {0}", param);

                    return null;
                } else {
                    Object[] error = { replDN, resultCode };
                    Log.logger.log(Level.WARNING, "Error in accessing entry DN: {0} error code = {1}", error);
                    throw new StoreException("Unable to access entry DN" + replDN);
                }
            } catch (DirectoryException dex) {
                Log.logger.log(Level.WARNING, "Error in accessing entry DN: " + replDN, dex);
                throw new StoreException("Unable to read record from store", dex);
            }
        }
        
        return Collections.EMPTY_SET;
    }
    
      
    /**
     * Synchronizes replication server info with current list of amsessiondb servers. 
     */
    public static boolean syncReplicatedServers(
                          Set<String> currServerSet, String port, String passwd) {
        Log.logger.log(Level.FINE, "syncReplication:start processing.");
        
        String[] args = {
            "-p", port,      // 1 : ds port num
            "-h", "localhost", 
            "-D",  "cn=directory manager",
            "-w", passwd,    // 7 : password
            "list-replication-server",
            "--provider-name", "Multimaster Synchronization",
            "--property", "replication-server",
            "--property", "replication-port","--no-prompt", "--trustAll"
        };
        
        String dbgcmd = concat(args).replaceAll(passwd, "****");
        Object[] params = { dbgcmd };
        Log.logger.log(Level.FINE, "exec dsconfig: {0}", params);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayOutputStream boe = new ByteArrayOutputStream();
        DSConfig.main(args, false, bos, boe);
        String str = bos.toString();
        String stre = boe.toString();
        
        if (stre.length() > 0 &&
                !stre.contains("Unable to continue since there are no Replication Server currently")) {
            Object[] params2 = { stre };
            Log.logger.log(Level.SEVERE, "syncReplication: stderr is not empty: {0}", params2);
            return false;
        } else {
            Object[] params3 = { stre };
            Log.logger.log(Level.FINE, "syncReplication: stderr is not empty: {0}", params3);
        }

        BufferedReader brd = new BufferedReader(new StringReader(str));
        String line = null;
        
        try {
            line = brd.readLine(); // 1st line
            line = brd.readLine(); // 2nd line
            line = brd.readLine(); // 3rd line
        } catch (Exception ex) {
            Log.logger.log(Level.SEVERE, "syncReplication:Failed:", ex);
        } 
        
        if (line == null)  {
            Object[] params2 = { str };
            Log.logger.log(Level.SEVERE, "syncReplication:cmd failed {0}", params2);
            return false;
        }
        
        try {
            int lastcolon= line.lastIndexOf(':');
            int stcolon = line.indexOf(':', line.indexOf(':')+1);
            String replservers = line.substring(stcolon+1, lastcolon);

            StringTokenizer stok = new StringTokenizer(replservers, ",");
            // Check if this server is part of server list
            List<String> cmdlist = new ArrayList<String>();
            cmdlist.add("-p"); 
            cmdlist.add(port);
            cmdlist.add("-h"); 
            cmdlist.add("localhost"); 
            cmdlist.add("-D");  
            cmdlist.add("cn=directory manager");
            cmdlist.add("-w"); 
            cmdlist.add(passwd);
            cmdlist.add("--no-prompt");
            cmdlist.add("--trustAll");
            cmdlist.add("set-replication-server-prop");
            cmdlist.add("--provider-name"); 
            cmdlist.add("Multimaster Synchronization");

            int numremoved = 0;
            while (stok.hasMoreTokens()) {
                String tok = stok.nextToken().trim();
                if (!currServerSet.contains(tok)) {
                    cmdlist.add("--remove"); 
                    cmdlist.add("replication-server:" + tok);
                    numremoved++;
                }
            }
            
            if (numremoved > 0) {
                String[] args1 = cmdlist.toArray(new String[cmdlist.size()]);
                String dbgcmd1 = concat(args1).replaceAll(passwd, "****");
                Object[] params2 = { dbgcmd1 };
                Log.logger.log(Level.SEVERE, "EmbeddedOpenDS:syncReplication:Execute: {0}", params2);
                
                bos = new ByteArrayOutputStream();
                boe = new ByteArrayOutputStream();
                DSConfig.main(args1, false, bos, boe);
                str = bos.toString();
                stre = boe.toString();

                Object[] params3 = { str };
                Log.logger.log(Level.SEVERE, "syncReplication:Result: {0}", params3);
                
                if (stre.length() != 0) {
                    Object[] params4 = { stre };
                    Log.logger.log(Level.SEVERE, "syncReplication:cmd stderr: {0}", params4);
                }
            }
        } catch (Exception ex) {
            Log.logger.log(Level.SEVERE, "EmbeddedOpenDS:syncReplication:Failed:", ex);
            return false;
        } 
        
        return true;
    }

    /**
     *  Starts the embedded <code>OpenDJ</code> instance.
     *
     *  @param odsRoot File system directory where <code>OpenDJ</code> 
     *                 is installed.
     *
     *  @throws Exception upon encountering errors.
     */
    public static void startServer(String odjRoot)
    throws Exception {
        if (isStarted()) {
            return;
        }
        
        Object[] params = { odjRoot };
        Log.logger.log(Level.INFO, "Start embedded OpenDJ server {0}", params);

        DirectoryEnvironmentConfig config = new DirectoryEnvironmentConfig();
        config.setServerRoot(new File(odjRoot));
        config.setForceDaemonThreads(true);
        config.setConfigClass(ConfigFileHandler.class);
        config.setConfigFile(new File(odjRoot + "/config", "config.ldif"));
        Log.logger.log(Level.FINE, "EmbeddedOpenDJ.startServer:starting DS Server...");
        EmbeddedUtils.startServer(config);
        Log.logger.log(Level.FINE, "...EmbeddedOpenDJ.startServer:DS Server started.");

        int sleepcount = 0;
        
        while (!EmbeddedUtils.isRunning() && (sleepcount < 60)) {
            sleepcount++;
            Thread.sleep(1000);
        }
        
        if (EmbeddedUtils.isRunning()) {
            Log.logger.log(Level.INFO, "...EmbeddedOpenDJ.startServer:DS Server started.");
        } else {
            Log.logger.log(Level.SEVERE, "...EmbeddedOpenDJ.startServer:DS Server not started.");
        }

        serverStarted = true;
    }
    

    /**
     *  Gracefully shuts down the embedded OpenDJ instance.
     *
     *  @throws Exception on encountering errors.
     */
    public static void shutdownServer() 
    throws Exception {
        if (isStarted()) {
            Log.logger.log(Level.FINE, "EmbeddedOpenDJ.shutdown server...");
            DirectoryServer.shutDown(
                "org.forgerock.openam.amsessionstore.db.opendj.EmbeddedOpenDJ",
                Message.EMPTY);
            int sleepcount = 0;
            
            while (DirectoryServer.isRunning() && (sleepcount < 60)) {
                sleepcount++;
                Thread.sleep(1000);
            }
            
            serverStarted = false;
            Log.logger.log(Level.FINE, "EmbeddedOpenDJ shutdown server success.");
        }
    }
    
    private static String concat(String[] args) {
        String ret = "";
        
        for (int i = 0; i < args.length; i++) {
           ret += args[i] + " ";        
        }
        
        return ret;
    }
}
