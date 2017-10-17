package g419.liner2.core.features.tokens;

import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class KeyValueFeature extends TokenFeature{

	private int sourceFeatureIdx;
	static HashMap<String, String> loadedDicts = new HashMap<>();

	public KeyValueFeature(String name, String dict_path, int sourceFeatureIdx){
		super(name);
		this.sourceFeatureIdx = sourceFeatureIdx;
		try {
			createDictFromFile(dict_path);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	@Override
	public String generate(Token token, TokenAttributeIndex index){
		String key = token.getAttributeValue(this.sourceFeatureIdx);
		if (!loadedDicts.containsKey(key))
			return "16.139694";
		else
			return loadedDicts.get(key);
	}

	public void createDictFromFile(String dictPath) throws IOException{
		File dictFile = new File(dictPath);
		if (!dictFile.exists())
			throw new FileNotFoundException("Invalid dictionary directory for feature "+name+": "+dictPath);
		BufferedReader inFile = new BufferedReader(new FileReader(dictPath));
		String entry = null;

		while ( (entry = inFile.readLine()) != null){
			String[] data = entry.split("\t");
			loadedDicts.put(data[0], data[1]);
		}

	}

}
