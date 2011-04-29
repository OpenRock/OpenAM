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

package org.forgerock.openam.amsessionstore.db;

import java.util.Map;
import java.util.Set;
import org.forgerock.openam.amsessionstore.common.AMRecord;

/**
 *
 * @author steve
 */
public interface PersistentStore {
    public void write(AMRecord record) throws StoreException;
    
    public AMRecord read(String id) throws StoreException, NotFoundException;
    
    public Set<String> readWithSecKey(String id) throws StoreException, NotFoundException;
    
    public void delete(String id) throws StoreException, NotFoundException;
    
    public void deleteExpired(long expDate) throws StoreException;
    
    public void shutdown();
    
    public Map<String, Long> getRecordCount(String id) throws StoreException;
    
    public DBStatistics getDBStatistics();
}
