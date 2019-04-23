package g419.spatial.filter;

import g419.spatial.structure.SpatialExpression;

import java.io.IOException;

/**
 * Odrzuca relacje, które posiadają określone tr lub lm.
 *
 * @author czuk
 */
public class RelationFilterDifferentObjects implements IRelationFilter {

  public RelationFilterDifferentObjects() throws IOException {
  }

  @Override
  public boolean pass(SpatialExpression relation) {
    return relation.getTrajector().getSpatialObject().getHead() != relation.getLandmark().getSpatialObject().getHead();
  }

}
