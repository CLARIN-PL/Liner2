package g419.spatial.filter;

import g419.spatial.structure.SpatialExpression;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RelationFilterPronoun implements IRelationFilter {

  Set<String> pronouns = new HashSet<>();

  public RelationFilterPronoun() throws IOException {
    pronouns.add("ppron12");
    pronouns.add("ppron3");
    pronouns.add("siebie");
  }

  @Override
  public boolean pass(final SpatialExpression relation) {
    return !pronouns.contains(relation.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getPos())
        && !pronouns.contains(relation.getTrajector().getSpatialObject().getHeadToken().getDisambTag().getPos());
  }

}
