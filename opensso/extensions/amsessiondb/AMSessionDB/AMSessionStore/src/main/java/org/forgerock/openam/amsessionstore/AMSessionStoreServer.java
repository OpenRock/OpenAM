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
package org.forgerock.openam.amsessionstore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import org.forgerock.openam.amsessionstore.common.Constants;
import org.forgerock.openam.amsessionstore.common.Log;
import org.forgerock.openam.amsessionstore.common.SystemProperties;
import org.forgerock.openam.amsessionstore.db.DBStatistics;
import org.forgerock.openam.amsessionstore.db.PersistentStoreFactory;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.security.MapVerifier;
import org.restlet.ext.crypto.DigestAuthenticator;

/**
 * This is the main class of the amsessiondb server. Starts the RESTlet server
 * component and initialises the underlying persistent store implementation.
 * 
 * @author steve
 */

public class AMSessionStoreServer {
    private static final int DEFAULT_PORT = 8182;
    private static final String DEFAULT_SHUTDOWN_ADDR = "127.0.0.1";
    private static final int DEFAULT_SHUTDOWN_PORT = 8183;
    private final static String DEFAULT_URI = "/amsessiondb";
    private final static String DEFAULT_MIN_THREADS = "10";
    private final static String DEFAULT_MAX_THREADS = "50";
    
    private static Component component = null;
    
    public static void main( String[] args ) {
        // Create a new Component.  
        component = new Component();  
  
        int port = SystemProperties.getAsInt(Constants.PORT, DEFAULT_PORT);
        String shutdownAddr = SystemProperties.get(Constants.SHUTDOWN_ADDR, DEFAULT_SHUTDOWN_ADDR);
        int shutdownPort = SystemProperties.getAsInt(Constants.SHUTDOWN_PORT, DEFAULT_SHUTDOWN_PORT);
        String uri = SystemProperties.get(Constants.URI, DEFAULT_URI);
        Server server = component.getServers().add(Protocol.HTTP, port);  
        AmSessionDbApplication amsessiondbApp = new AmSessionDbApplication();
        
        String minThreads = 
                SystemProperties.get(Constants.MIN_THREADS, DEFAULT_MIN_THREADS);
        String maxThreads = 
                SystemProperties.get(Constants.MAX_THREADS, DEFAULT_MAX_THREADS);
        
        server.getContext().getParameters().add("minThreads", minThreads);
        server.getContext().getParameters().add("maxThreads", maxThreads);
        
        String username = SystemProperties.get(Constants.USERNAME);
        String password = SystemProperties.get(Constants.PASSWORD);
        
        if (username != null && password != null 
                && !username.isEmpty() && !password.isEmpty()) {

            DigestAuthenticator guard = new DigestAuthenticator(null, "amsessiondb", password);
            MapVerifier mapVerifier = new MapVerifier();
            mapVerifier.getLocalSecrets().put(username, password.toCharArray());
            guard.setWrappedVerifier(mapVerifier);
            guard.setNext(amsessiondbApp);
            component.getDefaultHost().attach(uri, guard);
            
            Log.logger.log(Level.FINE, "amsessiondb started with DIGEST authentication");
        } else {
            component.getDefaultHost().attach(uri, amsessiondbApp);
            Log.logger.log(Level.WARNING, "amsessiondb started without authentication");
        }
        
        // Start the component, persistents store and statistics framework.  
        try {
            component.start(); 
            PersistentStoreFactory.getPersistentStore();
            DBStatistics.getInstance();
        } catch (Exception ex) {
            Log.logger.log(Level.WARNING, "Unable to start amsessiondb", ex);
        }   
        
        // Start the shutdown listener
        try {
            ShutdownListener shutdownServer = new ShutdownListener(shutdownAddr, shutdownPort);
            Thread shutdownThread = new Thread(shutdownServer);
            shutdownThread.setDaemon(true);
            shutdownThread.start();
            
            Object[] params = { shutdownAddr, shutdownPort };
            Log.logger.log(Level.FINE, "Shutdown listener started on address {0} and port {1}", params);
        } catch (Exception ex) {
            Log.logger.log(Level.WARNING, "Unable to start shutdown listener", ex);
        }
            
        
        Object[] params = { port, maxThreads};
        Log.logger.log(Level.FINE, "amsessiondb started on port {0} with maximum threads {1}", params);
    }
    
    public static void stop() {
        try {
            component.stop();
        } catch (Exception ex) {
            Log.logger.log(Level.WARNING, "Unable to stop amsessiondb", ex);
        }
    }
    
    static class ShutdownListener implements Runnable {
        private ServerSocket socket = null;
        private final static String SHUTDOWN = "SHUTDOWN";
        
        public ShutdownListener(String addr, int port)
        throws IOException {
            socket = new ServerSocket(port, 8, InetAddress.getByName(addr));
        }
        
        @Override
        public void run() {
            while (true) {
                Socket clientSocket = null;
                BufferedReader in = null;
                
                try {
                    clientSocket = socket.accept();
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String command = in.readLine();
                    
                    if (command.equals(SHUTDOWN)) {
                        Log.logger.log(Level.FINE, "Shutdown amsessiondb called");
                        
                        try {
                            PersistentStoreFactory.getPersistentStore().shutdown();
                        } catch (Exception ex) {
                            Log.logger.log(Level.WARNING, "Unable to shutdown persistent store", ex);
                        }
                        
                        AMSessionStoreServer.stop();
                        break;
                    }
                } catch (IOException ioe) {
                    Log.logger.log(Level.WARNING, "Unable to receive socket connection", ioe);
                } finally {
                    try {
                        in.close();
                        clientSocket.close(); 
                    } catch (IOException ioe) {
                        Log.logger.log(Level.WARNING, "Unable to close socket resource", ioe);
                    }
                }
                
                try {
                    socket.close();
                } catch (IOException ioe) {
                    Log.logger.log(Level.WARNING, "Unable to close server socket", ioe);
                }
                
                Log.logger.log(Level.FINE, "Shutdown amsessiondb complete");
            }
        }    
    }
}
