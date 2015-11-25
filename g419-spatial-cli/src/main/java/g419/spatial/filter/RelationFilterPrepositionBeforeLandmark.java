package g419.spatial.filter;

import java.io.IOException;

import g419.spatial.structure.SpatialRelation;

/**
 * Odrzuca relacje, które posiadają określone tr lub lm.
 * @author czuk
 *
 */
public class RelationFilterPrepositionBeforeLandmark implements IRelationFilter {

	public RelationFilterPrepositionBeforeLandmark() throws IOException{
	}
		
	@Override
	public boolean pass(SpatialRelation relation) {
		return relation.getSpatialIndicator().getHead() < relation.getLandmark().getHead();
	}
	
}
