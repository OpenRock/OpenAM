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
 * Query helper class used to search for specific entries in the database.
 * 
 * @author steve
 */
public class PredefinedQueries {
    protected static OSQLSynchQuery<ODocument> getByIDQuery = null;
    protected static OSQLSynchQuery<ODocument> getSecIdQuery = null;
    protected static OSQLSynchQuery<ODocument> getExpDateQuery = null;
    protected static OSQLSynchQuery<ODocument> getAllQuery = null;
    
    static {
        initialize();
    }
    
    private static void initialize() {
        getByIDQuery = 
                new OSQLSynchQuery<ODocument>("select * from " + DocumentUtil.CLASS_NAME + " where " + DocumentUtil.ORIENTDB_PRIMARY_KEY + " = ?");
        getSecIdQuery = 
                new OSQLSynchQuery<ODocument>("select * from " + DocumentUtil.CLASS_NAME + " where " + DocumentUtil.ORIENTDB_SECONDARY_KEY + " = ?");
        getExpDateQuery = 
                new OSQLSynchQuery<ODocument>("select * from " + DocumentUtil.CLASS_NAME + " where " + DocumentUtil.ORIENTDB_EXP_DATE + " <= ?");
        getAllQuery = new OSQLSynchQuery<ODocument>("select * from " + DocumentUtil.CLASS_NAME);
    }
    
    /**
     * Query by primary key.
     * 
     * @param id the primary key identifier for an object
     * @param database A handle to the OrientDB database object. No other thread must operate on this concurrently.
     * @return The ODocument if found, null if not found.
     * @throws IllegalArgumentException if the passed identifier or type are invalid
     */
    public ODocument getByID(final String id, ODatabaseDocumentTx database) 
    throws IllegalArgumentException {

        if (id == null) {
            throw new IllegalArgumentException("Query by id the passed id was null.");
        }
        
        List<ODocument> result = database.command(getByIDQuery).execute(id);

        ODocument first = null;
        if (result.size() > 0) {
            first = result.get(0); // ID is of type unique index, there must only be one at most
        }

        return first;
    }
    
    /**
     * Get by secondary id
     * 
     * @param id The secondary id, normally the uuid
     * @param database A handle to the OrientDB database object. No other thread must operate on this concurrently.
     * @return The list of documents found matching the secondary id
     * @throws IllegalArgumentException 
     */
    public List<ODocument> getBySecID(final String id, ODatabaseDocumentTx database) 
    throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("Query by id the passed id was null.");
        }
        
        List<ODocument> result = database.command(getSecIdQuery).execute(id);

        return result;
    }
    
    /**
     * Given a specific exp date, returns all record that have a exp date less than
     * or equal to that date
     * 
     * @param expDate The exp date for which to search
     * @param database A handle to the OrientDB database object. No other thread must operate on this concurrently.
     * @return The matching expired records, if any
     * @throws IllegalArgumentException 
     */
    public List<ODocument> getByExpDate(final long expDate, ODatabaseDocumentTx database) 
    throws IllegalArgumentException {
        if (expDate < 0) {
            throw new IllegalArgumentException("Query by exp date was passed negative exp date.");
        }
        
        List<ODocument> result = database.command(getExpDateQuery).execute(expDate);

        return result;
    }
    
    /**
     * Returns everything from the database.
     * 
     * TODO: Should probably remove, find a better way of doing this.
     * 
     * @param database
     * @return 
     */
    public List<ODocument> getAll(ODatabaseDocumentTx database) { 
        List<ODocument> result = database.query(getAllQuery);

        return result;
    }
}
