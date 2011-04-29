/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.forgerock.openam.amsessionstore.db.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 *
 * @author steve
 */
public class DBServer {
    public static void main(String[] argv) {
        System.out.println("Start OServer");
        EmbeddedOServer.startEmbedded();
        ODatabaseDocumentPool pool = DBHelper.initPool("remote:localhost/amsessiondb", "admin", "admin", 1, 10);
        
        try {
            // OPEN THE DATABASE
            //OServerAdmin serverAdmin = new OServerAdmin("remote:mqtest2/amsessiondb").connect("root", "F8D560682EEF1C76C85655E4427AF5E8A6DAB7FCB946DE3692BB3FA913DFE5B2");
            //ODatabaseDocumentTx db = new ODatabaseDocumentTx ("remote:mqtest2/amsessiondb").open("admin", "admin");
            ODatabaseDocumentTx db = pool.acquire("remote:localhost/amsessiondb", "admin", "admin");
            
            // CREATE A NEW DOCUMENT AND FILL IT
            ODocument doc = new ODocument(db, "Person");
            doc.field( "name", "Luke" );
            doc.field( "surname", "Skywalker" );
            doc.field( "city", new ODocument(db, "City").field("name","Rome").field("country", "Italy") );

            // SAVE THE DOCUMENT
            doc.save();

            //db.close();
            pool.release(db);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
