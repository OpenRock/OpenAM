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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.forgerock.openam.amsessionstore.common.Constants;
import org.forgerock.openam.amsessionstore.common.Log;
import org.forgerock.openam.amsessionstore.db.StoreException;
import org.forgerock.openam.amsessionstore.db.opendj.setup.SetupProgress;
import org.opends.messages.Message;
import org.opends.server.core.DirectoryServer;
import org.opends.server.extensions.ConfigFileHandler;
import org.opends.server.types.DirectoryEnvironmentConfig;
import org.opends.server.util.EmbeddedUtils;
import org.opends.server.util.ServerConstants;
import org.opends.server.tools.InstallDS;

/**
 *
 * @author steve
 */
public class EmbeddedOpenDJ {    
    private static boolean serverStarted = false;
    
    /**
     * Returns <code>true</code> if the server has already been started.
     *
     * @return <code>true</code> if the server has already been started.
     */ 
    public static boolean isStarted() {
        return serverStarted;
    }
    
    /**
     * Returns true if OpenDJ is configured
     * 
     * @return <code>true</code> if the server has already been installed.
     */
    public static boolean isInstalled() {
        File dbDir = new File(OpenDJPersistentStore.getOdjRoot() + "/db/userRoot");
        
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
        // copy OpenDS jar file
        String[] opendjSchemaFiles = {
            "98_amsessiondb.ldif"
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
        EmbeddedOpenDJ.setupOpenDS(odjRoot + "/config/config.ldif", OpenDJPersistentStore.getOpenDJSetupMap());

        Object[] params = { odjRoot };
        EmbeddedOpenDJ.startServer(odjRoot);

        // Check: If adding a new server to a existing cluster

        if (OpenDJPersistentStore.getExistingServerUrl() == null) {
            // Default: single / first server.
            Log.logger.log(Level.FINE, "Configuring First Server");
            int ret = EmbeddedOpenDJ.loadLDIF(OpenDJPersistentStore.getOpenDJSetupMap(), odjRoot, "../ldif/amsessiondb_suffix.ldif");
            
            if (ret == 0) {
                Log.logger.log(Level.FINE, "amsessiondb suffix created successfully.");
            } else {
                Object[] error = { Integer.toString(ret) };
                Log.logger.log(Level.SEVERE, "EmbeddedOpenDJ.setupOpenDJ. Error loading amsessiondb suffix", error);
                throw new StoreException("Unable to create amsessiondb suffix");
            }
        }
        
        EmbeddedOpenDJ.shutdownServer();
    }
    
    private static String tagSwap(String inpStr) {
        inpStr = inpStr.replaceAll("@" + Constants.OPENDJ_SUFFIX_TAG + "@", OpenDJPersistentStore.getSessionDBSuffix());
        inpStr = inpStr.replaceAll("@" + Constants.OPENDJ_RDN_TAG + "@", calculateRDNValue(OpenDJPersistentStore.getSessionDBSuffix()));
        
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
            Log.logger.log(Level.FINE, "Loading amsessiondb ldif " + ldif);

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
                "EmbeddedOpenDS.setupOpenDS. Error setting up OpenDS", params);
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
        setupCmd[4] = OpenDJPersistentStore.getSessionDBSuffix();
        setupCmd[6] = map.get(Constants.OPENDJ_DS_MGR_DN);
        setupCmd[8] = map.get(Constants.OPENDJ_LDAP_PORT);
        setupCmd[13] = map.get(Constants.OPENDJ_JMX_PORT);
        setupCmd[16] = configFile;

        Object[] params = { concat(setupCmd) };
        Log.logger.log(Level.FINE, "Running OpenDJ Setup command", params);

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
        
        Log.logger.log(Level.INFO, "Start embedded OpenDJ server" + odjRoot);

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
