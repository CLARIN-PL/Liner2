package g419.liner2.core.features.tokens;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.liner2.core.tools.TrieDictNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class DictFeature extends TokenInSentenceFeature{
    static HashMap<String, TrieDictNode> loadedDicts = new HashMap<String, TrieDictNode>();
	
	private TrieDictNode dict = new TrieDictNode(false);
	private int sourceFeatureIdx;
	
	public DictFeature(String name, String dict_path, int sourceFeatureIdx){
		super(name);
		this.sourceFeatureIdx = sourceFeatureIdx;
		try {
			createDictFromFile(dict_path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createDictFromFile(String path) throws IOException{
        File dictFile = new File(path);
		if (!dictFile.exists())
			throw new FileNotFoundException("Invalid dictionary directory for feature "+name+": "+path);
        if(loadedDicts.containsKey(dictFile.getName())){
            this.dict = loadedDicts.get(dictFile.getName());
        }
        else{
        	this.dict = TrieDictNode.loadPlain(path);        	
            loadedDicts.put(dictFile.getName(), this.dict);
        }
	}

	@Override
	public void generate(Sentence sentence){
		int thisFeatureIdx = sentence.getAttributeIndex().getIndex(this.getName());
		List<Token> tokens = sentence.getTokens();
		int tokenIdx = 0;
		String sourceFeatureValue = null;
		while (tokenIdx < sentence.getTokenNumber()){
			sourceFeatureValue = tokens.get(tokenIdx).getAttributeValue(sourceFeatureIdx);
			if (inDict(sourceFeatureValue)){
				TrieDictNode current = this.dict.getChild(sourceFeatureValue);
				int terminalToken = current.isTerminal() ? tokenIdx : -1;
				for(int currTok = tokenIdx+1; currTok < sentence.getTokenNumber(); currTok++){
					sourceFeatureValue = tokens.get(currTok).getAttributeValue(sourceFeatureIdx);
					if(current.hasChild(sourceFeatureValue)){
						current = current.getChild(sourceFeatureValue);
						if(current.isTerminal()){
							terminalToken = currTok;
						}
					}
					else
						break;
				}
				if(terminalToken == -1)
					tokens.get(tokenIdx).setAttributeValue(thisFeatureIdx, "O");
				else{
					tokens.get(tokenIdx).setAttributeValue(thisFeatureIdx, "B");
					for(int idx = tokenIdx+1; idx <= terminalToken; idx++)
						tokens.get(idx).setAttributeValue(thisFeatureIdx, "I");
					tokenIdx = terminalToken;
				}
			}
			else
				tokens.get(tokenIdx).setAttributeValue(thisFeatureIdx, "O");
			tokenIdx++;
		}
	}
	
	public boolean inDict(String value){
		return this.dict.hasChild(value);
	}
}
