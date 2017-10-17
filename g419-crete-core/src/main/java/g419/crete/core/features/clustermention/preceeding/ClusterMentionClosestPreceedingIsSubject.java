package g419.crete.core.features.clustermention.preceeding;

public class ClusterMentionClosestPreceedingIsSubject extends ClusterMentionClosestPreceedingMaltHasRelationFeature{

	public ClusterMentionClosestPreceedingIsSubject(String modelPath) {
		super(modelPath);
	}

	@Override
	public String getRelationName() {
		return "subj";
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_is_subject";
	}

}
