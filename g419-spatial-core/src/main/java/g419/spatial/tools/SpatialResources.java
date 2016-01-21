package g419.spatial.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class SpatialResources {

	/**
	 * Wczytuje listę regionów w postci form bazowych z resources.
	 * Lokalizacja: /g419/spatial/resources/spatial_regions.txt
	 * @return zbiór form bazowych regiobów
	 */
	public static Set<String> getRegions(){
		String location = "/g419/spatial/resources/spatial_regions.txt";
		InputStream resource = SpatialResources.class.getResourceAsStream(location);
		BufferedReader regionReager = new BufferedReader(new InputStreamReader( resource ));
		Set<String> regions = new HashSet<String>();
		
		String line = null;
		try {
			while ( (line = regionReager.readLine()) != null ) {
				line = line.trim();
				if ( line.length() > 0 ){
					regions.add(line);
				}
			}
		} catch (IOException e) {
			Logger.getLogger(SpatialResources.class).error("Wystąpił problem z odczytem pliku z regionami: " + e.getMessage());
		}
		
		return regions;
	}
	
}
