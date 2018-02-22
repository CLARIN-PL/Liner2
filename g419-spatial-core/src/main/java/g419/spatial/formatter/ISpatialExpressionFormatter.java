package g419.spatial.formatter;

import g419.corpus.structure.Document;
import g419.spatial.structure.SpatialExpression;

import java.util.List;

public interface ISpatialExpressionFormatter{
    public List<String> getHeader();
    public List<String> format(Document document, List<SpatialExpression> spatialExpressions);
}
