package g419.spatial.formatter;

import com.google.common.collect.Lists;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialObjectPath;

import java.util.List;

public class SpatialExpressionFormatterTree implements ISpatialExpressionFormatter {

  @Override
  public List<String> getHeader() {
    return Lists.newArrayList();
  }

  @Override
  public List<String> format(Document document, List<SpatialExpression> spatialExpressions) {
    List<String> lines = Lists.newArrayList();

    lines.add("");
    lines.add("Document: " + document.getName());

    int count = 1;
    for (SpatialExpression se : spatialExpressions) {
      lines.add("");
      lines.add(String.format("Expression #%d", count++));
      lines.add(" ┝━TR━┯━RE: " + printAnnotation(se.getTrajector().getRegion()));
      lines.add(" │    ┕━SO: " + printAnnotation(se.getTrajector().getSpatialObject()));
      lines.add(" ┝━SI━━━━━: " + printAnnotation(se.getSpatialIndicator()));
      lines.add(" ┝━LM━┯━RE: " + printAnnotation(se.getLandmark().getRegion()));
      lines.add(" │    ┕━SO: " + printAnnotation(se.getLandmark().getSpatialObject()));
      lines.add(" ┕━MI━┯━━━: " + printAnnotation(se.getMotionIndicator()));
      lines.add("      ┝━DR: ");
      for (Annotation direction : se.getDirections()) {
        lines.add("      │  ┕: " + printAnnotation(direction));
      }
      lines.add("      ┝━DS ");
      for (Annotation distance : se.getDistances()) {
        lines.add("      │  ┕: " + printAnnotation(distance));
      }
      lines.add("      ┕━PA ");
      for (SpatialObjectPath path : se.getPathsIndicators()) {
        lines.add("         ┕━┯━PI: " + printAnnotation(path.getPathIndicator()));
        lines.add("           ┝━RE: " + printAnnotation(path.getSpatialObject().getRegion()));
        lines.add("           ┕━SO: " + printAnnotation(path.getSpatialObject().getSpatialObject()));
      }
    }
    return lines;
  }

  private String printAnnotation(Annotation an) {
    return an == null ? "" : String.format("%-20s %-10s %s", an.getText(), an.getSentence().getId(), an.getType());
  }

}
