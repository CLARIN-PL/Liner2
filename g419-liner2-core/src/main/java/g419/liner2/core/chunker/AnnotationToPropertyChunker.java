package g419.liner2.core.chunker;

import g419.corpus.structure.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Jan Kocoń
 * 
 */

public class AnnotationToPropertyChunker extends Chunker {

    private Pattern propertyPattern = null;
    private Pattern annotationPattern = null;


    public AnnotationToPropertyChunker(String propertyPattern, String annotationPattern) {
//        this.propertyPattern = Pattern.compile("^generality\\_.*|^polarity\\_.*");
//        this.annotationPattern = Pattern.compile("^action$|^state$|^i\\_action$^i\\_state$|^reporting$|^perception$|^light\\_predicate$|^aspectual$");
        this.propertyPattern = Pattern.compile(propertyPattern);
        this.annotationPattern = Pattern.compile(annotationPattern);

	}


    @Override
    public Map<Sentence, AnnotationSet> chunk(Document ps) {
        HashMap<Sentence, AnnotationSet> out = new HashMap<>();
        return out;
    }

    @Override
    public void chunkInPlace(Document ps){
        for ( Paragraph paragraph : ps.getParagraphs() )
            for (Sentence sentence : paragraph.getSentences()) {
                LinkedHashSet<Annotation> annotations = sentence.getAnnotations(this.annotationPattern);
                LinkedHashSet<Annotation> properties = sentence.getAnnotations(this.propertyPattern);

                for (Annotation p : properties)
                    for (Annotation a : annotations)
                        if (p.getBegin() == a.getBegin() && p.getEnd() == a.getEnd()) {
                            String[] metadataKeyValue = p.getType().split("_");
                            a.setMetadata(metadataKeyValue[0], metadataKeyValue[1]);
                        }
                sentence.getChunks().removeAll(properties);
            }
    }

}
