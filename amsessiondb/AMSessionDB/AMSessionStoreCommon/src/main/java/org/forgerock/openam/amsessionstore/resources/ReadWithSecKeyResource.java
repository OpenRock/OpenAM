/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.forgerock.openam.amsessionstore.resources;

import java.util.Set;
import org.restlet.resource.Get;

/**
 *
 * @author steve
 */
public interface ReadWithSecKeyResource {
    public static final String URI = "/readwithseckey";
    
    @Get
    public Set<String> readWithSecKey(String id);
}
