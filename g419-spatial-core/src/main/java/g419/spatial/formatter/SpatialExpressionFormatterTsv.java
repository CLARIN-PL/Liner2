package g419.spatial.formatter;

import com.google.common.collect.Lists;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialObjectRegion;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class SpatialExpressionFormatterTsv implements ISpatialExpressionFormatter {

  @Override
  public List<String> getHeader() {
    final StringJoiner joiner = new StringJoiner("\t");
    joiner.add("Filename");
    joiner.add("Expression id");
    joiner.add("Trajector_region");
    joiner.add("Trajector_object");
    joiner.add("Spatial_indicator");
    joiner.add("Landmark_region");
    joiner.add("Landmark_object");
    joiner.add("Motion_indicator");
    joiner.add("Directions");
    joiner.add("Distances");
    joiner.add("Paths");
    return Lists.newArrayList(joiner.toString());
  }

  @Override
  public List<String> format(final Document document, final List<SpatialExpression> spatialExpressions) {
    final List<String> lines = Lists.newArrayList();
    for (final SpatialExpression se : spatialExpressions) {
      final StringJoiner fields = new StringJoiner("\t");
      fields.add(document.getName());
      fields.add(se.getKey());
      appendSpatialObjectRegion(fields, se.getTrajector());
      fields.add(getTextOrEmpty(se.getSpatialIndicator()));
      appendSpatialObjectRegion(fields, se.getLandmark());
      fields.add(getTextOrEmpty(se.getMotionIndicator()));
      fields.add(se.getDirections().stream().map(an -> getTextOrEmpty(an)).collect(Collectors.joining(";")));
      fields.add(se.getDistances().stream().map(an -> getTextOrEmpty(an)).collect(Collectors.joining(";")));
      fields.add(se.getPathsIndicators().toString());
      lines.add(fields.toString());
    }
    return lines;
  }

  private void appendSpatialObjectRegion(final StringJoiner fields, final SpatialObjectRegion ob) {
    fields.add(getTextOrEmpty(ob.getRegion()));
    fields.add(getTextOrEmpty(ob.getSpatialObject()));
    fields.add(getPosOrEmpty(ob.getSpatialObject()));
  }

  private String getTextOrEmpty(final Annotation an) {
    return an == null ? "" : an.getText();
  }

  private String getPosOrEmpty(final Annotation an) {
    return an == null ? "" : an.getHeadToken().getDisambTag().getPos();
  }

}
