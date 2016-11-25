package g419.liner2.api.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class TrieDictNode {

	private HashMap<String,TrieDictNode> children = new HashMap<String,TrieDictNode>();
	private boolean terminal;

	public TrieDictNode(){
		this.terminal = false;
	}

	public TrieDictNode(boolean terminal){
		this.terminal = terminal;
	}

	public void addChild(String value, boolean terminal){
		if (!hasChild(value)) 
			children.put(value, new TrieDictNode(terminal));
		else if (terminal == true){
			TrieDictNode existing = getChild(value);
			if (!existing.isTerminal()){
				existing.setTerminal(true);
				children.put(value, existing);
			}
		}
	}
	
	public boolean hasChild(String value){
		return children.containsKey(value);
	}
	
	public TrieDictNode getChild(String value){
		return children.get(value);
	}
	
	public boolean isTerminal() {
		return terminal;
	}

	public void setTerminal(boolean terminal) {
		this.terminal = terminal;
	}
	
	
	/**
	 * 
	 * @param phrase
	 */
	public void addPhrase(String[] phrase){
        if (phrase.length == 1){
            this.addChild(phrase[0], true);
        }
        else{
            int wordIdx = 0;
            TrieDictNode dictNode = this;
            while ( wordIdx < phrase.length - 1){
                dictNode.addChild(phrase[wordIdx], false);
                dictNode = dictNode.getChild(phrase[wordIdx]);
                wordIdx++;
            }
            dictNode.addChild(phrase[wordIdx], true);
        }
		
	}
	
	/**
	 * Wczytuje słownik z pliku tekstowego, w którym każda linia zwiera jedną frazę. Słowa oddzielone są spacjami.
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static TrieDictNode loadPlain(String path) throws IOException{
		TrieDictNode node = new TrieDictNode(false);
		
        File dictFile = new File(path);
		if (!dictFile.exists()){
			throw new FileNotFoundException("File "+path+" does not exist");
		}
		
        BufferedReader inFile = new BufferedReader(new FileReader(dictFile));
        String entry = null;
        
        while ( (entry = inFile.readLine()) != null){
            node.addPhrase(entry.split(" "));
        }
        inFile.close();
        
        return node;
	}
	
}
