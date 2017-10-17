package g419.liner2.core.chunker;

import java.util.HashMap;

import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.core.converter.AnnotationMappingConverter;
import g419.liner2.core.converter.Converter;

public class MappingChunker extends Chunker {

    private Converter converter;
    HashMap<String, Document> testData;

    public MappingChunker(String mappingFile){
        converter = new AnnotationMappingConverter(mappingFile);
    }
    
    @Override
    public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
    	for ( Sentence sentence : ps.getSentences() ){
    		converter.apply(sentence);
    	}
    	return ps.getChunkings();
    }
}
