package liner2.features.tokens;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import liner2.structure.Token;
import liner2.structure.Sentence;
import liner2.tools.TrieDictNode;

public class DictFeature extends Feature{
	
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
		BufferedReader inFile = new BufferedReader(new FileReader(path));
		String entry = inFile.readLine();
		String[] words;
		while (entry != null){
			
			words = entry.split(" ");
			if (words.length == 1)
				this.dict.addChild(entry, true);
			else{
				int wordIdx = 0;
				TrieDictNode dictNode = this.dict;
				while ( wordIdx < words.length - 1){
					dictNode.addChild(words[wordIdx], false);
					dictNode = dictNode.getChild(words[wordIdx]);
					wordIdx++;
				}
				dictNode.addChild(words[wordIdx], true);
			}	
			entry = inFile.readLine();
		}	
		inFile.close();
	}

	
	public void generate(Sentence sentence, int thisFeatureIdx){
		ArrayList<Token> tokens = sentence.getTokens();
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
