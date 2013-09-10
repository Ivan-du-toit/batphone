/**
 * Copyright (C) 2011 The Serval Project
 *
 * This file is part of Serval Software (http://www.servalproject.org)
 *
 * Serval Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package za.co.csir.walkiemesh.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class Configuration {

	public static final String DEVICE_NEXUSONE   = "nexusone";
	public static final String DEVICE_GALAXY1X   = "galaxy1x";
	public static final String DEVICE_GALAXY2X   = "galaxy2x";
	public static final String DEVICE_DROID      = "droid";
	public static final String DEVICE_LEGEND     = "legend";
	public static final String DEVICE_DREAM      = "dream";
	public static final String DEVICE_HERO1X     = "hero1x";
	public static final String DEVICE_HERO2X     = "hero2x";
	public static final String DEVICE_MOMENT     = "moment";
	public static final String DEVICE_CLIQ       = "cliq";
	public static final String DEVICE_LIQUID     = "liquid";
	public static final String DEVICE_IDEOS_U8150	= "ideos8150";
	public static final String DEVICE_AR6000_BASED = "Atheros 6000 based";
	public static final String DEVICE_UNKOWN     = "unknown";

	public static final String DRIVER_TIWLAN0    = "tiwlan0";
	public static final String DRIVER_WEXT       = "wext";
	public static final String DRIVER_SOFTAP_HTC = "softap_htc";
	public static final String DRIVER_SOFTAP_GOG = "softap_gog";

	/**
	 * Returns the device-type as string.
	 * A very ugly hack - checking for wifi-kernel-modules.
	 */



	/**
	 * Returns the wpa_supplicant-driver which should be used
	 * on wpa_supplicant-start
	 */
	public static String getWifiInterfaceDriver(String deviceType) {
		if (deviceType.equals(DEVICE_DREAM) || deviceType.equals(DEVICE_HERO1X) || deviceType.equals(DEVICE_HERO2X)) {
			return DRIVER_TIWLAN0;
		}
		/**
		 * Extremely ugly stuff here - we really need a better method to detect such stuff
		 */
		else if (deviceType.equals(DEVICE_NEXUSONE) && hasKernelFeature("CONFIG_BCM4329_SOFTAP")) {
			return DRIVER_SOFTAP_HTC;
		}
		//else if (deviceType.equals(DEVICE_NEXUSONE) && (Integer.parseInt(Build.VERSION.SDK)) > 7 && (new File("/etc/firmware/fw_bcm4329_apsta.bin")).exists()) {
		else if (deviceType.equals(DEVICE_NEXUSONE) && (new File("/etc/firmware/fw_bcm4329_apsta.bin")).exists()) {
			return DRIVER_SOFTAP_GOG;
		}
		return DRIVER_WEXT;
	}

	/**
	 * Returns the wpa_supplicant-driver which should be used
	 * on wpa_supplicant-start
	 */
	public static String getEncryptionAutoMethod(String deviceType) {
		if (deviceType.equals("legend") || deviceType.equals("nexusone")) {
			return "iwconfig";
		}
		return "wpa_supplicant";
	}


    public static boolean hasKernelFeature(String feature) {
    	try {
			File cfg = new File("/proc/config.gz");
			if (cfg.exists() == false) {
				return true;
			}
			FileInputStream fis = new FileInputStream(cfg);
			String line = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(fis)),256);
			while ((line = in.readLine()) != null) {
				   if (line.startsWith(feature)) {
					    in.close();
						return true;
					}
			}
		    in.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return false;
    }
}
