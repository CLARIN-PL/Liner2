package g419.liner2.core.chunker;

import fasttext.Pair;
import g419.corpus.structure.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import fasttext.FastText;


/**
 * @author Jan Kocoń
 * 
 */

public class FastTextRelationChunker extends Chunker {

    private FastText fasttext = null;
    private Pattern annotationPattern = null;
    private boolean content = false;


    public FastTextRelationChunker(String modelPath, String annotationPattern, boolean content) {
        this.annotationPattern = Pattern.compile(annotationPattern);
        this.content = content;
        this.fasttext = new FastText();
        try {
            fasttext.loadModel(modelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<Sentence, AnnotationSet> chunk(Document ps) {
        HashMap<Sentence, AnnotationSet> out = new HashMap<>();
        return out;
    }

    public static String getRepresentation(Annotation annotationFrom, Annotation annotationTo, boolean content) throws IllegalArgumentException {
        Sentence s = annotationFrom.getSentence();
        if (!annotationTo.getSentence().equals(s))
            throw new IllegalArgumentException("Annotations " + annotationFrom + " and " + annotationTo + " are not from the same sentence!");
        boolean reverseRelation = annotationFrom.getBegin() > annotationTo.getBegin();
        int firstToken = reverseRelation ? annotationTo.getBegin() : annotationFrom.getBegin();
        int lastToken = reverseRelation ? annotationFrom.getEnd() : annotationTo.getEnd();
        //if (lastToken - firstToken > 7)
        //    return null;
        List<Token> sentenceTokens = s.getTokens();
        List<String> representation = new LinkedList<>();
        representation.add(reverseRelation ? "1" : "0");
        Set<Token> tokensFrom = new HashSet<>(annotationFrom.getTokenTokens());
        Set<Token> tokensTo = new HashSet<>(annotationTo.getTokenTokens());
        for (int i = firstToken; i <= lastToken; i++){
            Token tok = sentenceTokens.get(i);
            Tag tag = tok.getDisambTag();
            String tokenType = null;
            if (tokensFrom.contains(tok))
                tokenType = annotationFrom.getType();
            else if (tokensTo.contains(tok))
                tokenType = annotationTo.getType();
            if (tokenType != null)
                representation.add(tokenType);
            if (content)
                representation.add(tag.getBase().toLowerCase());
            //representation.add(tok.getPos());
        }
        return String.join(" ", representation);
    }


    @Override
    public void chunkInPlace(Document ps){
        for ( Paragraph paragraph : ps.getParagraphs() )
            for (Sentence sentence : paragraph.getSentences()) {
                LinkedHashSet<Annotation> annotations = sentence.getAnnotations(this.annotationPattern);
                for (Annotation annotationFrom : annotations)
                    for (Annotation annotationTo : annotations)
                        if (annotationFrom != annotationTo) {
                            String representation = getRepresentation(annotationFrom, annotationTo, this.content);
                            if (representation != null) {
                                List<Pair<Float, String>> prediction = fasttext.predict(representation.split(" "), 1);
                                String predictedClass = prediction.get(0).getValue().replaceFirst("__label__", "");
                                if (!predictedClass.equals("null"))
                                    ps.addRelation(new Relation(annotationFrom, annotationTo, predictedClass));
                            }
                        }
            }
    }

}
