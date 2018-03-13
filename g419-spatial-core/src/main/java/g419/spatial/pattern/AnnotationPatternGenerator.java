package g419.spatial.pattern;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.spatial.tools.SentenceAnnotationIndexTypePos;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AnnotationPatternGenerator {

    //	private final String ngs = "(AdjG|NumG[rzbde]?|NGdata|NGadres|Ngg|Ngs|NG[agspbcnxk])";
//	private final Set<String> spejdTypesToReplace = Sets.newHashSet("PrepNG", "NG", "Verbfin", "Ppas", "Pact", "Inf", "Imps");

    //		this.annotationsPrep.add(Pattern.compile("^PrepNG.*"));
//		this.annotationsNg.add(Pattern.compile("^NG.*"));
//		this.annotationsNp.add(Pattern.compile("chunk_np"));

    public String generate(Sentence sentence, Collection<Annotation> annotations) throws Exception{
        StringBuilder sb = new StringBuilder();
        List<Token> tokens = sentence.getTokens();
        SentenceAnnotationIndexTypePos chunks = new SentenceAnnotationIndexTypePos(sentence);

        List<Annotation> outside = annotations.stream().filter(an->an.getSentence()!=sentence).collect(Collectors.toList());
        if (!outside.isEmpty()){
            throw new Exception(String.format("Sentence %s does not contain annotation(s): %s", sentence, outside));
        }

        Integer firstToken = annotations.stream().map(a -> a.getBegin()).min(Integer::compare).get();
        Integer lastToken = annotations.stream().map(a -> a.getEnd()).max(Integer::compare).get();

        int i=firstToken;
        while (i<=lastToken){
            sb.append(String.format("[pos=%s]", tokens.get(i).getDisambTag().getPos()));
            i++;
        }

        return sb.toString().trim();

    }


}
