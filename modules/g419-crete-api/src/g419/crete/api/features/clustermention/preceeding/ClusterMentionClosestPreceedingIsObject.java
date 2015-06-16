package g419.crete.api.features.clustermention.preceeding;

public class ClusterMentionClosestPreceedingIsObject extends ClusterMentionClosestPreceedingMaltHasRelationFeature{
	
	public ClusterMentionClosestPreceedingIsObject(String modelPath) {
		super(modelPath);
	}

	@Override
	public String getRelationName() {
		return "obj";
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_is_object";
	}

}
