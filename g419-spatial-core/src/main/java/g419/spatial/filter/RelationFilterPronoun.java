package g419.spatial.filter;

import g419.spatial.structure.SpatialExpression;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RelationFilterPronoun implements IRelationFilter {

	Set<String> pronouns = new HashSet<String>();
		
	public RelationFilterPronoun() throws IOException{
		this.pronouns.add("ppron12");
		this.pronouns.add("ppron3");
		this.pronouns.add("siebie");
	}
		
	@Override
	public boolean pass(SpatialExpression relation) {
		return !this.pronouns.contains(relation.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getPos())
				&& !this.pronouns.contains(relation.getTrajector().getSpatialObject().getHeadToken().getDisambTag().getPos());
	}
	
}
