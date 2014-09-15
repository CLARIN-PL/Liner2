package g419.liner2.api.chunker;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.api.converter.AnnotationMappingConverter;
import g419.liner2.api.converter.Converter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by michal on 9/12/14.
 */
public class MappingChunker extends Chunker {

    private Converter converter;
    HashMap<String, Document> testData;

    public MappingChunker(String mappingFile, ArrayList<Document> testData){
        converter = new AnnotationMappingConverter(mappingFile);
        this.testData = new HashMap<String, Document>();
        for(Document doc: testData){
            this.testData.put(doc.getName(), doc);

        }
    }
    @Override
    public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
        HashMap<Sentence, AnnotationSet> chunking = new HashMap<Sentence, AnnotationSet>();
//        //ToDo: rozwiązanie na szybko
        Document testDoc = testData.get(ps.getName());
        for(int i=0 ; i<ps.getSentences().size(); i++){
            Sentence testSent = testDoc.getSentences().get(i);
            converter.apply(testSent);
            ps.getSentences().get(i).setAnnotations(new AnnotationSet(ps.getSentences().get(i), testSent.getChunks()));
            chunking.put(ps.getSentences().get(i), new AnnotationSet(ps.getSentences().get(i), testSent.getChunks()));
        }
        return chunking;
    }
}
