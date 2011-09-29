package org.forgerock.openam.utils;

import com.maxmind.geoip.LookupService;

public class FR_GeoDB {

	static LookupService theInstance = null;
	
	private FR_GeoDB() {
		
	}

	public static LookupService getInstance(String dbLocation) {
		try {
			if (theInstance == null) {
				LookupService theInstance = new LookupService(dbLocation,LookupService.GEOIP_MEMORY_CACHE);
			}
		}catch (Exception e) {

		}
		return theInstance;
	}

}
