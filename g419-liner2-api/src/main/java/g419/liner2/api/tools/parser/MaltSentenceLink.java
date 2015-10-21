package g419.liner2.api.tools.parser;

public class MaltSentenceLink{
	
	int tokenIndex = 0;
	int parentIndex = 0;
	String relationType = null;
	
	public MaltSentenceLink(int tokenIndex, int parentIndex, String relationType){
		this.tokenIndex = tokenIndex;
		this.parentIndex = parentIndex;
		this.relationType = relationType;
	}

	public int getTokenIndex() {
		return this.tokenIndex;
	}

	public void setTokenIndex(int tokenIndex) {
		this.tokenIndex = tokenIndex;
	}

	public int getParentIndex() {
		return parentIndex;
	}

	public void setParentIndex(int parentIndex) {
		this.parentIndex = parentIndex;
	}

	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}    	
	
}

