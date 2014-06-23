package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.DictionaryChunker;
import g419.liner2.api.chunker.SerializableChunkerInterface;
import g419.liner2.api.tools.Logger;

import java.util.ArrayList;
import java.util.regex.Matcher;



public class ChunkerFactoryItemDictCompile extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictCompile() {
		super("dict-compile:dict=(.*):common=(.*):model=(.*?)(:types=(.*))?");
	}

	@Override
	public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
		
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()){
            Logger.log("--> Dictionary Chunker compile");

            String dictFile = matcher.group(1);
            String commonsFile = matcher.group(2);
            String modelFile = matcher.group(3);
            
            ArrayList<String> types = null;
            if (matcher.group(5) != null) {
            	types = new ArrayList<String>();
            	String[] typesArray = matcher.group(5).split(",");
            	for (int i = 0; i < typesArray.length; i++)
            		types.add(typesArray[i]);
            }
            
            DictionaryChunker chunker = new DictionaryChunker(types);
//            chunker.setModelFilename(modelFile);
            Logger.log("--> Compiling dictionary from file=" + dictFile);
            chunker.loadDictionary(dictFile, commonsFile);
            Logger.log("--> Saving chunker to file=" + modelFile);
			((SerializableChunkerInterface)chunker).serialize(modelFile);
            
            return chunker;		
		}
		else		
			return null;
	}

}
