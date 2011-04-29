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
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import java.util.List;

/**
 *
 * @author steve
 */
public class PredefinedQueries {
    /**
     * Query by primary key, the OpenIDM identifier. This identifier is different from the OrientDB record id.
     * 
     * @param id the OpenIDM identifier for an object
     * @param database a handle to the OrientDB database object. No other thread must operate on this concurrently.
     * @return The ODocument if found, null if not found.
     * @throws IllegalArgumentException if the passed identifier or type are invalid
     */
    public ODocument getByID(final String id, ODatabaseDocumentTx database) 
    throws IllegalArgumentException {

        if (id == null) {
            throw new IllegalArgumentException("Query by id the passed id was null.");
        }
        
        // TODO: convert into a prepared statement
        OSQLSynchQuery<ODocument> query = 
                new OSQLSynchQuery<ODocument>("select * from " + DocumentUtil.CLASS_NAME + " where " + DocumentUtil.ORIENTDB_PRIMARY_KEY + " = '" + id + "'");
        List<ODocument> result = database.query(query);

        ODocument first = null;
        if (result.size() > 0) {
            first = result.get(0); // ID is of type unique index, there must only be one at most
        }

        return first;
    }
    
    public List<ODocument> getBySecID(final String id, ODatabaseDocumentTx database) 
    throws IllegalArgumentException {

        if (id == null) {
            throw new IllegalArgumentException("Query by id the passed id was null.");
        }
        
        // TODO: convert into a prepared statement
        OSQLSynchQuery<ODocument> query = 
                new OSQLSynchQuery<ODocument>("select * from " + DocumentUtil.CLASS_NAME + " where " + DocumentUtil.ORIENTDB_SECONDARY_KEY + " = '" + id + "'");
        List<ODocument> result = database.query(query);

        return result;
    }
    
    public List<ODocument> getAll(ODatabaseDocumentTx database) { 
        // TODO: convert into a prepared statement
        OSQLSynchQuery<ODocument> query = 
                new OSQLSynchQuery<ODocument>("select * from " + DocumentUtil.CLASS_NAME);
        List<ODocument> result = database.query(query);

        return result;
    }
}
