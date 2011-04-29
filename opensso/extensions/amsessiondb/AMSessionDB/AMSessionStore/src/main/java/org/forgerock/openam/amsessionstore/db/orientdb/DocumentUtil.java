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

package org.forgerock.openam.amsessionstore.db.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import java.util.HashMap;
import java.util.Map;
import org.forgerock.openam.amsessionstore.common.AMRecord;
import org.forgerock.openam.amsessionstore.db.StoreException;

/**
 *
 * @author steve
 */
public class DocumentUtil {
    // Identifiers in the DB representation
    public final static String ORIENTDB_PRIMARY_KEY = "_amsessiondb_id";
    public final static String ORIENTDB_SECONDARY_KEY = "_amsessiondb_skey";
    public final static String ORIENTDB_SERVICE = "_amsessiondb_service";
    public final static String ORIENTDB_OPERATION = "_amsessiondb_operation";
    public final static String ORIENTDB_EXP_DATE = "_amsessiondb_expdate";
    public final static String ORIENTDB_STATE = "_amsessiondb_state";
    public final static String ORIENTDB_AUX_DATA = "_amsessiondb_auxdata";
    public final static String ORIENTDB_DATA = "_amsessiondb_data";
    public final static String ORIENTDB_EXTRA_STRING_ATTR = 
            "_amsessiondb_extrastringattrs";
    public final static String ORIENTDB_EXTRA_BYTE_ATTR = 
            "_amsessiondb_extrabyteattrs";
    
    public final static String CLASS_NAME = "amrecords";
    
    protected static ODocument toDocument(AMRecord record, ODatabaseDocumentTx db) 
    throws StoreException {
        ODocument result = null;
        
        if (record != null) {
            result = new ODocument(db, CLASS_NAME);
            
            String pKey = record.getPrimaryKey();
            
            if (pKey == null) {
                throw new StoreException("primary key cannot be null");
            }
            
            result.field(ORIENTDB_PRIMARY_KEY, pKey);

            String sKey = record.getSecondaryKey();
            
            if (sKey != null) {
                result.field(ORIENTDB_SECONDARY_KEY, sKey);
            }
            
            String service = record.getService();
            
            if (service != null) {
                result.field(ORIENTDB_SERVICE, service);
            }
            
            String operation = record.getOperation();
            
            if (operation != null) {
                result.field(ORIENTDB_OPERATION);
            }
            
            long expDate = record.getExpDate();
            
            if (expDate > 0) {
                result.field(ORIENTDB_EXP_DATE, Long.toString(expDate));
            }
            
            int state = record.getState();
            result.field(ORIENTDB_STATE, Integer.toString(state));
            
            String auxData = record.getAuxdata();
            
            if (auxData != null) {
                result.field(ORIENTDB_AUX_DATA, auxData);
            }
            
            String data = record.getData();
            
            if (data != null) {
                result.field(ORIENTDB_DATA, data);
            }
            
            Map<String, String> extraStringAttrs = record.getExtraStringAttributes();
            
            if (extraStringAttrs != null && !extraStringAttrs.isEmpty()) {
                ODocument innerMap = new ODocument();
                
                for (Map.Entry<String, String> entry : extraStringAttrs.entrySet()) {
                    innerMap.field(entry.getKey(), entry.getValue());
                }
                
                result.field(ORIENTDB_EXTRA_STRING_ATTR, innerMap, OType.EMBEDDED);
            }
            
            Map<String, String> extraByteAttrs = record.getExtraByteAttributes();
            
            if (extraByteAttrs != null && !extraByteAttrs.isEmpty()) {
                ODocument innerMap = new ODocument();
                
                for (Map.Entry<String, String> entry : extraByteAttrs.entrySet()) {
                    innerMap.field(entry.getKey(), entry.getValue());
                }
                
                result.field(ORIENTDB_EXTRA_BYTE_ATTR, innerMap, OType.EMBEDDED);
            }
            
        }

        return result;
    }
    
    protected static AMRecord toAMRecord(ODocument doc) {
        AMRecord record = new AMRecord();
        
        for (Map.Entry<String, Object> entry : doc) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key.equals(ORIENTDB_PRIMARY_KEY)) {
                record.setPrimaryKey((String) value);
            } else if (key.equals(ORIENTDB_SECONDARY_KEY)) {
                record.setSecondaryKey((String) value);
            } else if (key.equals(ORIENTDB_SERVICE)) {
                record.setService((String) value);
            } else if (key.equals(ORIENTDB_OPERATION)) {
                record.setOperation((String) value);
            } else if (key.equals(ORIENTDB_EXP_DATE)) {
                long expDate = Long.parseLong((String) value);
                record.setExpDate(expDate);
            } else if (key.equals(ORIENTDB_STATE)) {
                int state = Integer.parseInt((String) value);
                record.setState(state);
            } else if (key.equals(ORIENTDB_AUX_DATA)) {
                record.setAuxdata((String) value);
            } else if (key.equals(ORIENTDB_DATA)) {
                record.setData((String) value);
            } else if (key.equals(ORIENTDB_EXTRA_STRING_ATTR)) {
                ODocument innerDoc = (ODocument) value;
                Map<String, String> extraStringAttrs = new HashMap<String, String>();
                
                for (Map.Entry<String, Object> innerEntry : innerDoc) {
                    extraStringAttrs.put(innerEntry.getKey(), (String) innerEntry.getValue());
                }
                
                record.setExtraStringAttrs(extraStringAttrs);
            } else if (key.equals(ORIENTDB_EXTRA_BYTE_ATTR)) {
                ODocument innerDoc = (ODocument) value;
                
                for (Map.Entry<String, Object> innerEntry : innerDoc) {
                    record.setExtraByteAttrs(innerEntry.getKey(), (String) innerEntry.getValue());
                }
            } else {
                // handle unexpected entry in document
                System.out.println("Unexpected entry in map: " + key + " : " + value);
            }
        }
        
        return record;
    }
}
