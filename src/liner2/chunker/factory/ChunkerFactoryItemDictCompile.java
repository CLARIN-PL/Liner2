package liner2.chunker.factory;

import java.util.regex.Matcher;
import java.util.ArrayList;

import liner2.chunker.Chunker;
import liner2.chunker.DictionaryChunker;
import liner2.chunker.SerializableChunkerInterface;

import liner2.Main;

public class ChunkerFactoryItemDictCompile extends ChunkerFactoryItem {

	public ChunkerFactoryItemDictCompile() {
		super("dict-compile:dict=(.*):common=(.*):model=(.*?)(:types=(.*))?");
	}

	@Override
	public Chunker getChunker(String description) throws Exception {
		
		Matcher matcher = this.pattern.matcher(description);
		if (matcher.find()){
            Main.log("--> Dictionary Chunker compile");

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
            Main.log("--> Compiling dictionary from file=" + dictFile);
            chunker.loadDictionary(dictFile, commonsFile);
            Main.log("--> Saving chunker to file=" + modelFile);
			((SerializableChunkerInterface)chunker).serialize(modelFile);
            
            return chunker;		
		}
		else		
			return null;
	}

}
