/*global define, screen */ 

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */

define(["org/forgerock/openam/extensions/authmodules/adaptivedeviceprint/AbstractDevicePrintInfoCollector",
		"org/forgerock/openam/extensions/authmodules/adaptivedeviceprint/EventManager",
        "org/forgerock/openam/extensions/authmodules/adaptivedeviceprint/Constants",
        "org/forgerock/libwrappers/EvercookieWrapper"],
        function(AbstractDevicePrintInfoCollector,
                 eventManager,
                 constants,
                 Evercookie) {

    var obj = new AbstractDevicePrintInfoCollector(), cookie; 

    obj.gatherInformation = function() { 
        var ret = {};
        
        if(obj.cookie) {
            ret.persistentCookie = obj.cookie;
  
            return ret;
        } else {
            console.debug("EvercookieCollector: cookie not readed"); 
            return null;  
        } 
    };
    
    obj.getEverCookie = function() { 
        var ec = new Evercookie(), newest = 0, d = 0; 
        
        ec.get(constants.adaptiveDevicePrintCookieName, function(bestCandidate, allCandidates) {            
            console.debug("Selecting best candidate");
        	
            //cookie is timestamp-uuid            
        	for(var i in allCandidates) {		
                if( allCandidates[i] != undefined ) {
                	var timestamp = parseInt(allCandidates[i].split("-")[0]);
            		
                    if( !isNaN(timestamp) && timestamp > newest ) {
                    	newest = timestamp;
                    	obj.cookie = allCandidates[i];
                    }
                	
                	d++;
                }
            }
            
        	console.debug("Cookie was saved in " + d + " different ways");
        	
        	var staticCookie = obj.getCookie(constants.adaptiveDevicePrintCookieName);        	
        	
            if(staticCookie != undefined ) {
            	var staticCookieTimestamp = parseInt(staticCookie.split("-")[0]);
            	
            	if(!isNaN(staticCookieTimestamp) && staticCookieTimestamp > newest ) {
	            	newest = staticCookieTimestamp;
	            	obj.cookie = staticCookie;
            	}
            } 
            
            console.debug("Evercookie best candidate: " + obj.cookie + " from " +  new Date(newest));
            
            eventManager.sendEvent(constants.EVENT_RECOLLECT_DATA); 
            
            if( newest != 0 ) {
	           	console.debug("Refreshing evercookie with " + obj.cookie);
	            	
	           	ec.set(constants.adaptiveDevicePrintCookieName, obj.cookie);
            }
        }, 1);
    };
    
    obj.setCookie = function(c_name, value, exdays) {
	    var exdate = new Date();
	    exdate.setDate(exdate.getDate() + exdays);
	    
	    var c_value = escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
	    
	    document.cookie = c_name + "=" + c_value;
    };
    
    obj.getCookie = function(c_name) {
	    var i, x, y, ARRcookies = document.cookie.split(";");
	    
	    for (i = 0; i < ARRcookies.length; i++) {
		    x = ARRcookies[i].substr(0, ARRcookies[i].indexOf("="));
		    y = ARRcookies[i].substr(ARRcookies[i].indexOf("=") + 1);
		    x = x.replace(/^\s+|\s+$/g,"");
		    
		    if (x == c_name) {
		    	return unescape(y);
		    }
	    }
    }
    
    obj.removeCookie = function(c_name) {
    	document.cookie = c_name + '=; expires=Thu, 01-Jan-70 00:00:01 GMT;'
    }
    
    obj.getEverCookie();

    return obj;
});
