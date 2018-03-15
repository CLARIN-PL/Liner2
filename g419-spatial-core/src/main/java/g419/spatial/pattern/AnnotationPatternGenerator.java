package g419.spatial.pattern;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.spatial.tools.SentenceAnnotationIndexTypePos;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class AnnotationPatternGenerator {

    public String generate(Sentence sentence, Collection<Annotation> annotations) throws Exception{
        assertAnnotationsBelongsToSentence(sentence, annotations);
        List<Token> tokens = sentence.getTokens();
        SentenceAnnotationIndexTypePos annIndex = new SentenceAnnotationIndexTypePos(sentence);
        annotations.stream().map(a->a.toString()).forEach(LoggerFactory.getLogger(getClass())::debug);

        Integer firstToken = annotations.stream().map(a -> a.getBegin()).min(Integer::compare).get();
        Integer lastToken = annotations.stream().map(a -> a.getEnd()).max(Integer::compare).get();

        StringJoiner sj = new StringJoiner(" ");
        int i=firstToken;
        Annotation nextMention = annIndex.getFirstInRangeFromSet(i, lastToken, annotations);
        while (i<=lastToken && nextMention!=null){
            while (i<nextMention.getBegin()){
                Annotation an = annIndex.getLongestAtPosFromSet(i, annotations);
                an = an == null ? annIndex.getLongestAtPos(i) : null;
                if ( an == null ) {
                    sj.add(String.format("[pos=%s]", tokens.get(i).getDisambTag().getPos()));
                    i++;
                } else {
                    sj.add(formatAnnotation(an));
                    i += an.length();
                }
            }
            sj.add(formatAnnotation(nextMention));
            i=nextMention.getEnd()+1;
            nextMention = annIndex.getFirstInRangeFromSet(i, lastToken, annotations);
        }

        return sj.toString();
    }

    private void assertAnnotationsBelongsToSentence(Sentence sentence, Collection<Annotation> annotations) throws Exception {
        List<Annotation> outside = annotations.stream().filter(an->an.getSentence()!=sentence).collect(Collectors.toList());
        if (!outside.isEmpty()){
            throw new Exception(String.format("Sentence %s does not contain annotation(s): %s", sentence, outside));
        }
    }

    private String formatAnnotation(Annotation annotation){
        return String.format("[%s]", annotation.getType());
    }


}
