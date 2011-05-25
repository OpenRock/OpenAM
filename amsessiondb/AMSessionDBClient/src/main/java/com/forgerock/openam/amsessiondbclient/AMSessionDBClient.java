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

package com.forgerock.openam.amsessiondbclient;

import java.util.Map;
import java.util.Set;
import org.apache.commons.net.util.Base64;
import org.forgerock.openam.amsessionstore.common.AMRecord;
import org.forgerock.openam.amsessionstore.resources.DeleteByDateResource;
import org.forgerock.openam.amsessionstore.resources.DeleteResource;
import org.forgerock.openam.amsessionstore.resources.GetRecordCountResource;
import org.forgerock.openam.amsessionstore.resources.ReadResource;
import org.forgerock.openam.amsessionstore.resources.ReadWithSecKeyResource;
import org.forgerock.openam.amsessionstore.resources.WriteResource;
import org.restlet.resource.ClientResource;

/**
 * 
 *
 */
public class AMSessionDBClient {
    private final static String READ = "read";
    private final static String WRITE = "write";
    private final static String DELETE = "delete";
    private final static String DELETE_BY_DATE = "d_date";
    private final static String GET_RECORD_COUNT = "count";
    private final static String SHUTDOWN = "shutdown";
    private final static String READ_SEC_KEY = "r_skey";
    private final static String TEST = "test";
    
    public final static String SESSION = "session"; 
    
    private String resourceUrl = null;
    
    public static void main(String[] args) {
        System.out.println("AMSessionDBClient");

        if (args.length != 2) {
            System.err.println("Wrong number of arguments");
        }
        
        AMSessionDBClient client = new AMSessionDBClient(args[0]);
       
        if (args[1].equals(READ)) {
            client.read();
        } else if (args[1].equals(WRITE)) {
            client.write();
        } else if (args[1].equals(DELETE)) {
            client.delete();
        } else if (args[1].equals(DELETE_BY_DATE)) {
            client.deleteByDate();
        } else if (args[1].equals(GET_RECORD_COUNT)) {
            client.getRecordCount();
        } else if (args[1].equals(SHUTDOWN)) {
            client.shutdown();
        } else if (args[1].equals(READ_SEC_KEY)) {
            client.readSecKey();
        } else if (args[1].equals(TEST)) {
            client.test();
        } else {
            System.err.println("Invalid option");
        }
        
        System.exit(0);
    }
    
    public AMSessionDBClient(String url) {
        resourceUrl = url;
    }
    
    public void read() {
        System.out.println("Read");
        
        try {
            ClientResource resource = new ClientResource(resourceUrl + "/read");
            ReadResource readResource = resource.wrap(ReadResource.class);

            AMRecord record = readResource.read("43478392743");
            System.out.println(record);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        } 
    }
    
    public void write() {
        System.out.println("Write");
        
        long date = System.currentTimeMillis();
        byte[] blob = new byte[32];
        String data = Base64.encodeBase64String(blob);
        
        ClientResource resource = new ClientResource(resourceUrl + "/write");
        WriteResource writeResource = resource.wrap(WriteResource.class);
        
        AMRecord record1 = new AMRecord(SESSION, 
                                       AMRecord.WRITE, 
                                       "43478392743", 
                                       date,
                                       "id=steve,ou=user,dc=opensso,dc=java,dc=net", 
                                       1, 
                                       "AQIC5wM2LY4SfczmQf3Ao_kX9sxz5h21vebM9pNRcxCKBMg.*" +
                                       "AAJTSQACMDIAAlNLAAotMTIzMDI1NjY0AAJTMQACMDE.*", data);
        AMRecord record2 = new AMRecord(SESSION, 
                                       AMRecord.WRITE, 
                                       "47387438748", 
                                       date,
                                       "id=bob,ou=user,dc=opensso,dc=java,dc=net", 
                                       1, 
                                       "AQIC5wM2LY4SfczmQf3Ao_kX9sxz5h21vebM9pNRcxCKBMg.*" +
                                       "AAJTSQACfjdkljhgfkdzbvfjkdzu43jk43j4AJTMQACMDE.*", data);
        AMRecord record3 = new AMRecord(SESSION, 
                                       AMRecord.WRITE, 
                                       "123025664", 
                                       date,
                                       "id=sven,ou=user,dc=opensso,dc=java,dc=net", 
                                       1, 
                                       "AQIC5wM2LY4SfczmQf3Ao_kX9sxz5h21vebM9pNRcxCKBMg.*" +
                                       "djklfjdksljfkdsljfkldsjfkldjfkldjskfljdskfjds.*", data);
        try {
            writeResource.write(record1);
            writeResource.write(record2);
            writeResource.write(record3);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }
    
    public void delete() {
        System.out.println("Delete");
        
        ClientResource resource = new ClientResource(resourceUrl + "/delete");
        DeleteResource deleteResource = resource.wrap(DeleteResource.class);
        
        try {
            deleteResource.remove("123025664");
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    public void deleteByDate() {
        System.out.println("Delete by date");
        
        ClientResource resource = new ClientResource(resourceUrl + "/deletebydate");
        DeleteByDateResource purgeResource = resource.wrap(DeleteByDateResource.class);
        
        try {
            purgeResource.remove(System.currentTimeMillis() / 1000);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    public void getRecordCount() {
        System.out.println("Get Record Count");
        
        try {
            ClientResource resource = new ClientResource(resourceUrl + "/getrecordcount");
            GetRecordCountResource getRecordCountResource = resource.wrap(GetRecordCountResource.class);

            Map<String, Long> sessions = getRecordCountResource.getRecordCount("id=bob,ou=user,dc=opensso,dc=java,dc=net");

            System.out.println(sessions);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    public void shutdown() {
        System.out.println("Shutdown");
        
        ClientResource resource = new ClientResource(resourceUrl + "/shutdown");
    }
    
    public void readSecKey() {
        System.out.println("Get sec key");
        
        try {
            ClientResource resource = new ClientResource(resourceUrl + "/readwithseckey");
            ReadWithSecKeyResource secReadResource = resource.wrap(ReadWithSecKeyResource.class);

            Set<String> records = secReadResource.readWithSecKey("id=bob,ou=user,dc=opensso,dc=java,dc=net");
            System.out.println(records);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    public void test() {
        write();
        read();
        getRecordCount();
        readSecKey();
        delete();
        getRecordCount();
    }
}
