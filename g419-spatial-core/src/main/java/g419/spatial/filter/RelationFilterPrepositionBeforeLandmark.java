package g419.spatial.filter;

import g419.corpus.HasLogger;
import g419.spatial.structure.SpatialExpression;

import java.io.IOException;

/**
 * Odrzuca relacje, które posiadają określone tr lub lm.
 *
 * @author czuk
 */
public class RelationFilterPrepositionBeforeLandmark implements IRelationFilter, HasLogger {

  public RelationFilterPrepositionBeforeLandmark() throws IOException {
  }

  @Override
  public boolean pass(final SpatialExpression relation) {
    return relation.getSpatialIndicator().getHead() < relation.getLandmark().getSpatialObject().getHead();
  }

}
