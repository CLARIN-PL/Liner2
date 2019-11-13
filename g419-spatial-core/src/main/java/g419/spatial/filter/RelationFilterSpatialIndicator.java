package g419.spatial.filter;

import g419.spatial.structure.SpatialExpression;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;

public class RelationFilterSpatialIndicator implements IRelationFilter {

  Set<String> spatialIndicators = new HashSet<>();

  public RelationFilterSpatialIndicator() throws IOException {

    final String location = "/g419/spatial/resources/spatial_indicators.txt";
    final InputStream resource = getClass().getResourceAsStream(location);

    if (resource == null) {
      throw new MissingResourceException("Resource not found: " + location,
          getClass().getName(), location);
    }
    final BufferedReader tags = new BufferedReader(new InputStreamReader(resource));
    String line = null;
    while ((line = tags.readLine()) != null) {
      line = line.trim();
      if (line.length() > 0) {
        spatialIndicators.add(line);
      }
    }
  }

  @Override
  public boolean pass(final SpatialExpression relation) {
    final String si = relation.getSpatialIndicator().getText().toLowerCase();
    return spatialIndicators.contains(si);
  }

}
