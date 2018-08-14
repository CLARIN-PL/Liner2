package g419.crete.core.features.factory;

import g419.corpus.structure.Annotation;
import g419.crete.core.CreteOptions;
import g419.crete.core.features.AbstractFeature;
import g419.crete.core.features.annotations.pair.AnnotationPairFeatureSemanticLinkAgP;
import g419.crete.core.features.factory.item.IFeatureFactoryItem;
import g419.toolbox.wordnet.Wordnet;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotationPairFeatureSemanticLinkAgPItem implements IFeatureFactoryItem<Pair<Annotation, Annotation>, Float> {

	private HashMap<String, HashMap<String, String[]>> mappings = new HashMap<>();
	
	private Wordnet getInitializedWordnet(String wordnetPath){
		 return Wordnet.getWordnet(wordnetPath);
	}
	
	private void initializeMapping(String mappingPath) throws FileNotFoundException{
		Pattern entryPattern = Pattern.compile("^(\\w+),\"(.*)?\"$");
		File mappingFile = new File(mappingPath);
		synchronized(mappingFile){
			if(mappings.get(mappingPath) == null){
				Scanner scanner = new Scanner(mappingFile);
				HashMap<String, String[]> mapping = new HashMap<>();
				while (scanner.hasNextLine())
		        {
					String line = scanner.nextLine();
		            Matcher entryMatcher = entryPattern.matcher(line);
		            if(entryMatcher.matches()){
		            	String neName = entryMatcher.group(1);
		            	String ssets = entryMatcher.group(2);
		            	String[] synsets = ssets.substring(0,ssets.lastIndexOf(",")).split(",");
		            	mapping.put(neName, synsets);
		            }
		        }
				mappings.put(mappingPath, mapping);
				scanner.close();
			}
		}
	}
	
	private HashMap<String, String[]> getInitializedMapping(String mappingPath){
		if(mappings.get(mappingPath) == null){
			try {
				initializeMapping(mappingPath);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return mappings.get(mappingPath);
	}
	
	@Override
	public AbstractFeature<Pair<Annotation, Annotation>, Float>  createFeature() {
		String wordnetPath = CreteOptions.getOptions().getProperties().getProperty("wordnet_path"); //TODO: fixme
		String wordnetMappingPath = CreteOptions.getOptions().getProperties().getProperty("wordnet_mapping"); //TODO: fixme
		return new AnnotationPairFeatureSemanticLinkAgP(wordnetPath == null ? null : getInitializedWordnet(wordnetPath), wordnetMappingPath == null ? null : getInitializedMapping(wordnetMappingPath));
	}


}
