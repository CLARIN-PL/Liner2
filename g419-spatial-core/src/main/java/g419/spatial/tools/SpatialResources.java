package g419.spatial.tools;

import com.google.common.collect.Sets;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

public final class SpatialResources {

    public final static String RESOURCE_REGIONS = "/g419/spatial/resources/spatial_regions.txt";

	/**
	 * Wczytuje listę regionów w postci form bazowych z resources.
	 * Lokalizacja: /g419/spatial/resources/spatial_regions.txt
	 * @return zbiór form bazowych regiobów
	 */
	public static Set<String> getRegions() {
		InputStream resource = SpatialResources.class.getResourceAsStream(RESOURCE_REGIONS);
        Set<String> regions = Sets.newHashSet();
        try {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource))) {
                regions.addAll(br.lines().map(String::trim).filter(l -> l.length() > 0).collect(Collectors.toList()));
            }
        } catch (Exception ex){
            LoggerFactory.getLogger(SpatialResources.class).error("Failed to load the list of regions from resource '{}", RESOURCE_REGIONS, ex);
        }
		return regions;
	}
	
}
