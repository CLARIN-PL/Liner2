package g419.liner2.api.tools.parser;

public class MaltSentenceLink{
	
	int sourceIndex = 0;
	int targetIndex = 0;
	String relationType = null;
	
	/**
	 * Reprezentuje relację
	 * <code>
	 * sourceIndex --(relationType)--> targetIndex
	 * </code> 
	 * @param sourceIndex
	 * @param targetIndex
	 * @param relationType
	 */
	public MaltSentenceLink(int sourceIndex, int targetIndex, String relationType){
		this.sourceIndex = sourceIndex;
		this.targetIndex = targetIndex;
		this.relationType = relationType;
	}

	public int getSourceIndex() {
		return this.sourceIndex;
	}

	public void setSourceIndex(int tokenIndex) {
		this.sourceIndex = tokenIndex;
	}

	public int getTargetIndex() {
		return targetIndex;
	}

	public void setTargetIndex(int targetIndex) {
		this.targetIndex = targetIndex;
	}

	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}    	
	
}

