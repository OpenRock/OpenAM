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

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.forgerock.openam.amsessionstore.common.AMRecord;
import org.forgerock.openam.amsessionstore.resources.DeleteByDateResource;
import org.forgerock.openam.amsessionstore.resources.DeleteResource;
import org.forgerock.openam.amsessionstore.resources.GetRecordCountResource;
import org.forgerock.openam.amsessionstore.resources.ReadResource;
import org.forgerock.openam.amsessionstore.resources.ReadWithSecKeyResource;
import org.forgerock.openam.amsessionstore.resources.ShutdownResource;
import org.forgerock.openam.amsessionstore.resources.WriteResource;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Reference;
import org.restlet.engine.util.Base64;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * 
 *
 */
public class AMSessionDBClient {
    public final static String SESSION = "session"; 
    
    public final static String PRIMARY_KEY = "primaryKey";
    public final static String EXP_DATE = "expDate";
    public final static String SEC_KEY = "secKey";
    public final static String STATE = "state";
    public final static String TOKEN_ID = "token.id";
    public final static String DATA = "data";
    public final static String SLEEP_DELAY = "sleepDelay";
    
    public final static String RDN = "uid=";
    public final static String BASE_DN = ",ou=users,o=openam";
    public final static String ID1 = "@S101SI10SK";
    public final static String ID2 = "#";
    public final static String DEFAULT_SLEEP = "1000";
    public final static String BLOB = "AQIC5wM2LY4Sfcw45uksyOjqqd1Eo4yesbTiCsXmUASY3x4.*AAJTSQACMDMAAlNLAAk2ODU5MjA5NDgAAlMxAAIwMQ..*" +
            "|rO0ABXNyADBjb20uaXBsYW5ldC5kcHJvLnNlc3Npb24uc2VydmljZS5JbnRlcm5hbFNlc3Npb26BVEnUR/begwIAF0oADGNyZWF0aW9uVGltZVoACmlzSVN" +
            "TdG9yZWRKABBsYXRlc3RBY2Nlc3NUaW1lSgAObWF4Q2FjaGluZ1RpbWVKAAttYXhJZGxlVGltZUoADm1heFNlc3Npb25UaW1lWgAScmVzY2hlZHVsZVBvc3N" +
            "pYmxlSQAMc2Vzc2lvblN0YXRlSQALc2Vzc2lvblR5cGVKAAp0aW1lZE91dEF0SgAHdmVyc2lvbloADndpbGxFeHBpcmVGbGFnTAAMY2xpZW50RG9tYWludAA" +
            "STGphdmEvbGFuZy9TdHJpbmc7TAAIY2xpZW50SURxAH4AAUwACmNvb2tpZU1vZGV0ABNMamF2YS9sYW5nL0Jvb2xlYW47TAAJY29va2llU3RycQB+AAFMAB1" +
            "yZXN0cmljdGVkVG9rZW5zQnlSZXN0cmljdGlvbnQAD0xqYXZhL3V0aWwvTWFwO0wAFXJlc3RyaWN0ZWRUb2tlbnNCeVNpZHEAfgADTAAQc2Vzc2lvbkV2ZW5" + 
            "0VVJMc3EAfgADTAANc2Vzc2lvbkhhbmRsZXEAfgABTAAJc2Vzc2lvbklEdAAkTGNvbS9pcGxhbmV0L2Rwcm8vc2Vzc2lvbi9TZXNzaW9uSUQ7TAARc2Vzc2l" +
            "vblByb3BlcnRpZXN0ABZMamF2YS91dGlsL1Byb3BlcnRpZXM7TAAEdXVpZHEAfgABeHAAAAAATcG0BgEAAAAATcG0DgAAAAAAAAADAAAAAAAAAB4AAAAAAAA" +
            "AeAAAAAABAAAAAAAAAAAAAAAAAAAAAAAAAAABdAAZZGM9b3BlbnNzbyxkYz1qYXZhLGRjPW5ldHQAKmlkPXN0ZXZlLG91PXVzZXIsZGM9b3BlbnNzbyxkYz1" + 
            "qYXZhLGRjPW5ldHBwc3IAJWphdmEudXRpbC5Db2xsZWN0aW9ucyRTeW5jaHJvbml6ZWRNYXAbc/kJS0s5ewMAAkwAAW1xAH4AA0wABW11dGV4dAASTGphdmE" +
            "vbGFuZy9PYmplY3Q7eHBzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAMdwgAAAAQAAA" +
            "AAHhxAH4AC3hzcQB+AAlzcQB+AAw/QAAAAAAADHcIAAAAEAAAAAB4cQB+AA54c3EAfgAJc3EAfgAMP0AAAAAAAAx3CAAAABAAAAABdABFaHR0cDovL21xdGV" +
            "zdDIuaW50ZXJuYWwuc29sbGlja2VyLmNvbTo4MDgwL29wZW5hbS9ub3RpZmljYXRpb25zZXJ2aWNlc3IAImNvbS5pcGxhbmV0LmRwcm8uc2Vzc2lvbi5TZXN" +
            "zaW9uSUS2WWvWbPpcawIADVoADmNvbWluZ0Zyb21BdXRoWgAIaXNQYXJzZWRMAApjb29raWVNb2RlcQB+AAJMAA9lbmNyeXB0ZWRTdHJpbmdxAH4AAUwADWV" +
            "4dGVuc2lvblBhcnRxAH4AAUwACmV4dGVuc2lvbnNxAH4AA0wADXNlc3Npb25Eb21haW5xAH4AAUwADXNlc3Npb25TZXJ2ZXJxAH4AAUwAD3Nlc3Npb25TZXJ" + 
            "2ZXJJRHEAfgABTAARc2Vzc2lvblNlcnZlclBvcnRxAH4AAUwAFXNlc3Npb25TZXJ2ZXJQcm90b2NvbHEAfgABTAAQc2Vzc2lvblNlcnZlclVSSXEAfgABTAA" +
            "EdGFpbHEAfgABeHAAAHB0AF5BUUlDNXdNMkxZNFNmY3c0NXVrc3lPanFxZDFFbzR5ZXNiVGlDc1htVUFTWTN4NC4qQUFKVFNRQUNNRE1BQWxOTEFBazJPRFU" +
            "1TWpBNU5EZ0FBbE14QUFJd01RLi4qcHNxAH4ADD9AAAAAAAAMdwgAAAAQAAAAAHh0AABxAH4AF3EAfgAXcQB+ABdxAH4AF3EAfgAXcHhxAH4AEHh0AGZzaGF" +
            "uZGxlOkFRSUM1d00yTFk0U2ZjejZqckFMaXo0YVJacXB2VjBzUHp4V0V3MV9yTXRSV0tJLipBQUpUU1FBQ01ETUFBbE14QUFJd01RQUNVMHNBQ1RZNE5Ua3l" +
            "NRGswT0EuLipzcgBBY29tLmlwbGFuZXQuZHByby5zZXNzaW9uLnNlcnZpY2UuU2Vzc2lvblNlcnZpY2UkRXh0ZW5kZWRTZXNzaW9uSURkTxJRAJqzewIAAHh" +
            "xAH4AEwABcHEAfgAVdAAsQUFKVFNRQUNNRE1BQWxOTEFBazJPRFU1TWpBNU5EZ0FBbE14QUFJd01RPT1zcQB+AAw/QAAAAAAADHcIAAAAEAAAAAN0AAJTSXQ" +
            "AAjAzdAACUzF0AAIwMXQAAlNLdAAJNjg1OTIwOTQ4eHEAfgAHdAAha2l0Y2hlbm1hYy5pbnRlcm5hbC5zb2xsaWNrZXIuY29tcQB+AB50AAI4MHQABGh0dHB" +
            "0AAcvb3BlbmFtdAAAc3IAFGphdmEudXRpbC5Qcm9wZXJ0aWVzORLQenA2PpgCAAFMAAhkZWZhdWx0c3EAfgAFeHIAE2phdmEudXRpbC5IYXNodGFibGUTuw8" +
            "lIUrkuAMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAjdwgAAAAvAAAAGHQAB0NoYXJTZXR0AAVVVEYtOHQABlVzZXJJZHQABXN0ZXZldAA" +
            "MRnVsbExvZ2luVVJMdAAQL29wZW5hbS9VSS9Mb2dpbnQACnN1Y2Nlc3NVUkx0AA8vb3BlbmFtL2NvbnNvbGV0AA1jb29raWVTdXBwb3J0dAAEdHJ1ZXQACUF" +
            "1dGhMZXZlbHQAATB0AA1TZXNzaW9uSGFuZGxlcQB+ABh0AAlVc2VyVG9rZW50AAVzdGV2ZXQACGxvZ2luVVJMcQB+ADB0AApQcmluY2lwYWxzdAAFc3RldmV" +
            "0AAdTZXJ2aWNldAALbGRhcFNlcnZpY2V0ABpzdW4uYW0uVW5pdmVyc2FsSWRlbnRpZmllcnEAfgAIdAAKYW1sYmNvb2tpZXQAAjAxdAAMT3JnYW5pemF0aW9" +
            "ucQB+AAd0AAZMb2NhbGV0AAVlbl9HQnQACEhvc3ROYW1ldAANMTcyLjE2LjkwLjIyMXQAImNvbS1pcGxhbmV0LWFtLWNvbnNvbGUtbG9jYXRpb24tZG5xAH4" +
            "AB3QACEF1dGhUeXBldAAJRGF0YVN0b3JldAAESG9zdHEAfgBGdAALVXNlclByb2ZpbGV0AAhSZXF1aXJlZHQACmNsaWVudFR5cGV0AAtnZW5lcmljSFRNTHQ" +
            "AB0FNQ3R4SWR0ABJmOGM3YTAwNWZiNGQxMDUzMDF0AAthdXRoSW5zdGFudHQAFDIwMTEtMDUtMDRUMjA6MTY6MTRadAAJUHJpbmNpcGFscQB+AAh4cHEAfgAI";
    
    private String resourceUrl = null;
    private Map<String, String> config = null;
    private SecureRandom secureRandom = null;
    private static ThreadGroup threadPool = new ThreadGroup("TestGroup");
    private ChallengeResponse challengeResponse = null;
    
    public static void main(String[] args) {
        System.out.println("AMSessionDBClient");

        if (args.length != 3) {
            System.err.println("Wrong number of arguments");
            System.exit(1);
        }
        
        int threadCount = 0;
        
        try {
            threadCount = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.err.println("thread count not an int");
            System.exit(1);
        }
        
        int runCount = 0;
        
        try {
            runCount = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.err.println("run count not an int");
            System.exit(1);
        }
        
        AMSessionDBClient client = new AMSessionDBClient(args[0]);
        client.executeTest(threadCount, runCount);
    }
    
    public AMSessionDBClient(String url) {
        resourceUrl = url;
        config = new HashMap<String, String>();
        
        try { 
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        
        ClientResource resource = new ClientResource(url);
        resource.setChallengeResponse(ChallengeScheme.HTTP_DIGEST, "amsessiondb", "password");
        
        try {
            resource.get();
        } catch (ResourceException re) {
            System.err.println(re.getMessage());
        }

        if (resource.getStatus().getCode() == 401) {
            ChallengeRequest c1 = null;

            for (ChallengeRequest challengeRequest : resource.getChallengeRequests()) {
                if (ChallengeScheme.HTTP_DIGEST.equals(challengeRequest.getScheme())) {
                    c1 = challengeRequest;
                    break;
                }
            }

            challengeResponse = new ChallengeResponse(c1, resource.getResponse(),
                                                        "amsessiondb",
                                                        "password".toCharArray());
            System.out.println("Authentication setup");
        } else {
            System.out.println("Authentication not required");
        }
    }
    
    public void executeTest(int threadCount, int runCount) {
        for (int j = 0; j < runCount; j++) {
            for (int i = 0; i < threadCount; i++) {
                SessionTask stt = new SessionTask(generateConfig(i + "" + j), resourceUrl);
                //FailoverTask ft = new FailoverTask(generateConfig(i + "" + j), resourceUrl);
                //ReadSecKeyTask rskt = new ReadSecKeyTask(generateConfig(i + "" + j), resourceUrl);
                new Thread(threadPool, stt).start();
                //new Thread(threadPool, ft).start();
                //new Thread(threadPool, rskt).start();
            }
        }
    }
    
    protected Map<String, String> generateConfig(String id) {
        Map<String, String> configMap = new HashMap<String, String>();
        
        configMap.put(PRIMARY_KEY, generateStorageKey());
        //configMap.put(PRIMARY_KEY, "345601839");
        configMap.put(SEC_KEY, RDN + id + BASE_DN);
        configMap.put(EXP_DATE, Long.toString((System.currentTimeMillis() + 1800000) / 1000));
        configMap.put(STATE, "1");
        configMap.put(TOKEN_ID, generateTokenID());
        configMap.put(DATA, BLOB);
        configMap.put(SLEEP_DELAY, DEFAULT_SLEEP);
        
        return configMap;
    }
    
    protected String generateTokenID() {
        StringBuilder tokenId = new StringBuilder();
        tokenId.append(Base64.encode(Long.toHexString(secureRandom.nextLong()).getBytes(), false));
        tokenId.append(ID1);
        tokenId.append(Base64.encode(generateStorageKey().getBytes(), false));
        tokenId.append(ID2);
        
        return tokenId.toString();
    }
    
    protected String generateStorageKey() {
        return String.valueOf(secureRandom.nextInt());
    }
    
    class FailoverTask implements Runnable {
        protected Map<String, String> taskConfig = null;
        protected String resourceURL = null;
        
        public FailoverTask(Map<String, String> taskConfig, String resourceURL) {
            this.taskConfig = taskConfig;
            this.resourceURL = resourceURL;
        }
        
        public void run() {
            read();
        }
        
        protected void read() {
            ChallengeResponse c2 = null;

            ClientResource r1 = new ClientResource(resourceUrl + ReadResource.URI);
            r1.setChallengeResponse(ChallengeScheme.HTTP_DIGEST, "amsessiondb", "password");
            ReadResource readResource = r1.wrap(ReadResource.class);

            try {
                readResource.read(taskConfig.get(PRIMARY_KEY));
            } catch (Exception re) {
                System.err.println(re.getMessage());
            }

            if (r1.getStatus().getCode() == 401) {
                ChallengeRequest c1 = null;

                for (ChallengeRequest challengeRequest : r1.getChallengeRequests()) {
                    if (ChallengeScheme.HTTP_DIGEST.equals(challengeRequest.getScheme())) {
                        c1 = challengeRequest;
                        break;
                    }
                }

                c2 = new ChallengeResponse(c1, r1.getResponse(),
                                                            "amsessiondb",
                                                            "password".toCharArray());
                System.out.println("Authentication setup");
            } else {
                System.out.println("Authentication not required");
            }
            
            r1.setChallengeResponse(c2);
                
            AMRecord record = null;
            
            try {
                record = readResource.read(taskConfig.get(PRIMARY_KEY));
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            } 
            
            System.out.println(record);
        }
    }    

    

    class DeleteByDateTask implements Runnable {
        protected Map<String, String> taskConfig = null;
        protected String resourceURL = null;
        
        public DeleteByDateTask(Map<String, String> taskConfig, String resourceURL) {
            this.taskConfig = taskConfig;
            this.resourceURL = resourceURL;
        }
        
        public void run() {
            deleteByDate();
        }
        
        protected void deleteByDate() {
            ClientResource resource = new ClientResource(resourceUrl + DeleteByDateResource.URI);
            DeleteByDateResource purgeResource = resource.wrap(DeleteByDateResource.class);

            if (challengeResponse != null) {
                Reference ref = challengeResponse.getDigestRef();
                ref.setPath("/amsessiondb" + DeleteByDateResource.URI);
                challengeResponse.setDigestRef(ref);
                resource.setChallengeResponse(challengeResponse);
            }
            
            try {
                purgeResource.remove(Long.parseLong(taskConfig.get(EXP_DATE)));
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        } 
    }

    class ReadSecKeyTask implements Runnable {
        protected Map<String, String> taskConfig = null;
        protected String resourceURL = null;
        
        public ReadSecKeyTask(Map<String, String> taskConfig, String resourceURL) {
            this.taskConfig = taskConfig;
            this.resourceURL = resourceURL;
        }
        
        public void run() {
            readSecKey();
        }
        
        protected void readSecKey() {
            ChallengeResponse c2 = null;

            ClientResource r1 = new ClientResource(resourceUrl + ReadWithSecKeyResource.URI);
            r1.setChallengeResponse(ChallengeScheme.HTTP_DIGEST, "amsessiondb", "password");
            ReadWithSecKeyResource secReadResource = r1.wrap(ReadWithSecKeyResource.class);

            try {
                secReadResource.readWithSecKey(taskConfig.get(SEC_KEY));
            } catch (Exception re) {
                System.err.println(re.getMessage());
            }

            if (r1.getStatus().getCode() == 401) {
                ChallengeRequest c1 = null;

                for (ChallengeRequest challengeRequest : r1.getChallengeRequests()) {
                    if (ChallengeScheme.HTTP_DIGEST.equals(challengeRequest.getScheme())) {
                        c1 = challengeRequest;
                        break;
                    }
                }

                c2 = new ChallengeResponse(c1, r1.getResponse(),
                                                            "amsessiondb",
                                                            "password".toCharArray());
                System.out.println("Authentication setup");
            } else {
                System.out.println("Authentication not required");
            }

            r1.setChallengeResponse(c2);

            Set<String> records = null;
            
            try {
                records = secReadResource.readWithSecKey(taskConfig.get(SEC_KEY));
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
            
            System.out.println(records.size());
        }
    }
    
    class ShutdownTask implements Runnable {
        protected Map<String, String> taskConfig = null;
        protected String resourceURL = null;
        
        public ShutdownTask(Map<String, String> taskConfig, String resourceURL) {
            this.taskConfig = taskConfig;
            this.resourceURL = resourceURL;
        }
        
        public void run() {
            shutdown();
        }
        
        protected void shutdown() {
            ClientResource resource = new ClientResource(resourceUrl + ShutdownResource.URI);
            ShutdownResource shutdown = resource.wrap(ShutdownResource.class);
            
            if (challengeResponse != null) {
                resource.setChallengeResponse(challengeResponse);
            }
            
            shutdown.shutdown();
        }
    }
    
    class SessionTask implements Runnable {
        protected Map<String, String> taskConfig = null;
        protected String resourceURL = null;
        
        public SessionTask(Map<String, String> taskConfig, String resourceURL) {
            this.taskConfig = taskConfig;
            this.resourceURL = resourceURL;
        }
        
        public void run() {
            write();
            getRecordCount();
            sleep();
            delete();
        }
        
        protected void write() {
            ChallengeResponse c2 = null;
            
            ClientResource r1 = new ClientResource(resourceUrl + WriteResource.URI);
            r1.setChallengeResponse(ChallengeScheme.HTTP_DIGEST, "amsessiondb", "password");
            WriteResource writeResource = r1.wrap(WriteResource.class);

            try {
                //r1.get();
                writeResource.write(null);
            } catch (Exception re) {
                System.err.println(re.getMessage());
            }

            if (r1.getStatus().getCode() == 401) {
                ChallengeRequest c1 = null;

                for (ChallengeRequest challengeRequest : r1.getChallengeRequests()) {
                    if (ChallengeScheme.HTTP_DIGEST.equals(challengeRequest.getScheme())) {
                        c1 = challengeRequest;
                        break;
                    }
                }

                c2 = new ChallengeResponse(c1, r1.getResponse(),
                                                            "amsessiondb",
                                                            "password".toCharArray());
                System.out.println("Authentication setup");
            } else {
                System.out.println("Authentication not required");
            }
            
            r1.setChallengeResponse(c2);
            
            AMRecord record = new AMRecord(SESSION, 
                                           AMRecord.WRITE, 
                                           taskConfig.get(PRIMARY_KEY), 
                                           Long.parseLong(taskConfig.get(EXP_DATE)),
                                           taskConfig.get(SEC_KEY), 
                                           Integer.parseInt(taskConfig.get(STATE)), 
                                           taskConfig.get(TOKEN_ID), 
                                           taskConfig.get(DATA));
            // TODO Why does the break the REST interface
            /*record.setExtraByteAttrs("test1", "foo");
            record.setExtraByteAttrs("test2", "bba");
            Map<String, String> map = new HashMap<String, String>();
            map.put("apple", "foo");
            map.put("orange", "baa");
            record.setExtraStringAttrs(map);*/

            try {
                writeResource.write(record);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
        
        protected void getRecordCount() {
            ChallengeResponse c2 = null;
            
            ClientResource r1 = new ClientResource(resourceUrl + GetRecordCountResource.URI);
            r1.setChallengeResponse(ChallengeScheme.HTTP_DIGEST, "amsessiondb", "password");
            GetRecordCountResource getRecordCountResource = r1.wrap(GetRecordCountResource.class);

            try {
                getRecordCountResource.getRecordCount(taskConfig.get(SEC_KEY));
            } catch (Exception re) {
                System.err.println(re.getMessage());
            }

            if (r1.getStatus().getCode() == 401) {
                ChallengeRequest c1 = null;

                for (ChallengeRequest challengeRequest : r1.getChallengeRequests()) {
                    if (ChallengeScheme.HTTP_DIGEST.equals(challengeRequest.getScheme())) {
                        c1 = challengeRequest;
                        break;
                    }
                }

                c2 = new ChallengeResponse(c1, r1.getResponse(),
                                                            "amsessiondb",
                                                            "password".toCharArray());
                System.out.println("Authentication setup");
            } else {
                System.out.println("Authentication not required");
            }
            
            r1.setChallengeResponse(c2);
            
            try {
                Map<String, Long> sessions = getRecordCountResource.getRecordCount(taskConfig.get(SEC_KEY));
                System.out.println(sessions);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
        
        protected void sleep() {
            try {
                Thread.sleep(Long.parseLong(taskConfig.get(SLEEP_DELAY)));
            } catch (InterruptedException ie) {
                System.err.println(ie.getMessage());
            }
        }
        
        public void delete() {
            ChallengeResponse c2 = null;
            
            ClientResource r1 = new ClientResource(resourceUrl + DeleteResource.URI);
            r1.setChallengeResponse(ChallengeScheme.HTTP_DIGEST, "amsessiondb", "password");
            DeleteResource deleteResource = r1.wrap(DeleteResource.class);

            try {
                deleteResource.remove(taskConfig.get(PRIMARY_KEY));
            } catch (Exception re) {
                System.err.println(re.getMessage());
            }

            if (r1.getStatus().getCode() == 401) {
                ChallengeRequest c1 = null;

                for (ChallengeRequest challengeRequest : r1.getChallengeRequests()) {
                    if (ChallengeScheme.HTTP_DIGEST.equals(challengeRequest.getScheme())) {
                        c1 = challengeRequest;
                        break;
                    }
                }

                c2 = new ChallengeResponse(c1, r1.getResponse(),
                                                            "amsessiondb",
                                                            "password".toCharArray());
                System.out.println("Authentication setup");
            } else {
                System.out.println("Authentication not required");
            }
            
            r1.setChallengeResponse(c2);

            try {
                deleteResource.remove(taskConfig.get(PRIMARY_KEY));
                System.out.println("Deleted: " + taskConfig.get(PRIMARY_KEY));
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}
