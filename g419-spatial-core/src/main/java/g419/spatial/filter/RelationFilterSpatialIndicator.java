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

  Set<String> spatialIndicators = new HashSet<String>();

  public RelationFilterSpatialIndicator() throws IOException {

    String location = "/g419/spatial/resources/spatial_indicators.txt";
    InputStream resource = this.getClass().getResourceAsStream(location);

    if (resource == null) {
      throw new MissingResourceException("Resource not found: " + location,
          this.getClass().getName(), location);
    }
    BufferedReader tags = new BufferedReader(new InputStreamReader(resource));
    String line = null;
    while ((line = tags.readLine()) != null) {
      line = line.trim();
      if (line.length() > 0) {
        this.spatialIndicators.add(line);
      }
    }
  }

  @Override
  public boolean pass(SpatialExpression relation) {
    String si = relation.getSpatialIndicator().getText().toLowerCase();
    return this.spatialIndicators.contains(si);
  }

}
