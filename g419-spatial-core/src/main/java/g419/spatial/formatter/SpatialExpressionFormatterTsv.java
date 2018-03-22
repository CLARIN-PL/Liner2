package g419.spatial.formatter;

import com.google.common.collect.Lists;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.spatial.structure.SpatialExpression;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class SpatialExpressionFormatterTsv implements ISpatialExpressionFormatter{

    @Override
    public List<String> getHeader(){
        StringJoiner joiner = new StringJoiner("\t");
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
    public List<String> format(Document document, List<SpatialExpression> spatialExpressions){
        List<String> lines = Lists.newArrayList();
        int count=1;
        for (SpatialExpression se : spatialExpressions){
            StringJoiner fields = new StringJoiner("\t");
            fields.add(document.getName());
            fields.add(String.format("Expression #%d", count++));
            fields.add(getTextOrEmpty(se.getTrajector().getRegion()));
            fields.add(getTextOrEmpty(se.getTrajector().getSpatialObject()));
            fields.add(getTextOrEmpty(se.getSpatialIndicator()));
            fields.add(getTextOrEmpty(se.getLandmark().getRegion()));
            fields.add(getTextOrEmpty(se.getLandmark().getSpatialObject()));
            fields.add(getTextOrEmpty(se.getMotionIndicator()));
            fields.add(se.getDirections().stream().map(an -> getTextOrEmpty(an) ).collect(Collectors.joining(";")));
            fields.add(se.getDistances().stream().map(an -> getTextOrEmpty(an) ).collect(Collectors.joining(";")));
            fields.add(se.getPathsIndicators().toString());
            lines.add(fields.toString());
        }
        return lines;
    }

    private String getTextOrEmpty(Annotation an){
        return an==null?"":an.getText();
    }

}
