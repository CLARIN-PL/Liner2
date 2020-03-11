package g419.spatial.tools;

import com.google.common.collect.Sets;
import g419.corpus.schema.kpwr.KpwrSpatial;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Frame;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialRelationSchema;
import java.util.Set;

public class SpatialExpressionToFrame {

  public static Frame<Annotation> convert(final SpatialExpression se) {
    final Frame<Annotation> f = new Frame<>(KpwrSpatial.SPATIAL_FRAME_TYPE);
    f.set(KpwrSpatial.SPATIAL_INDICATOR, se.getSpatialIndicator());
    f.set(KpwrSpatial.SPATIAL_LANDMARK, se.getLandmark().getSpatialObject());
    f.set(KpwrSpatial.SPATIAL_TRAJECTOR, se.getTrajector().getSpatialObject());
    f.set(KpwrSpatial.SPATIAL_REGION, se.getLandmark().getRegion());

    f.setSlotAttribute(KpwrSpatial.SPATIAL_TRAJECTOR, "sumo",
        String.join(", ", se.getTrajectorConcepts()));
    f.setSlotAttribute(KpwrSpatial.SPATIAL_LANDMARK, "sumo",
        String.join(", ", se.getLandmarkConcepts()));
    f.setSlotAttribute("debug", "pattern", se.getType());

    final Set<String> schemas = Sets.newHashSet();
    for (final SpatialRelationSchema schema : se.getSchemas()) {
      schemas.add(schema.getName());
    }
    f.setSlotAttribute("debug", "schema", String.join("; ", schemas));

    return f;
  }

}
