package g419.corpus.structure;

import java.util.Comparator;

public class AnnotationExactMatchComparator implements Comparator<Annotation> {

    private final boolean sameChannel;

    public AnnotationExactMatchComparator(){
        this.sameChannel = false;
    }

    public AnnotationExactMatchComparator(boolean sameChannel){
        this.sameChannel = sameChannel;
    }

    @Override
    public int compare(Annotation ann1, Annotation ann2) {
//        System.out.println(ann1 + " " + ann2);
        boolean channelEquality = !sameChannel || (ann1.getType() != null && ann1.getType().equals(ann2.getType()));
        int sentenceAnn1 = ann1.getSentence().getDocument().getSentences().indexOf(ann1.getSentence());
        int sentenceAnn2 = ann2.getSentence().getDocument().getSentences().indexOf(ann2.getSentence());
        boolean sentenceEquality = (sentenceAnn1 == sentenceAnn2);
        boolean tokenEquality = ann1.getTokens().equals(ann2.getTokens());
        if(sentenceEquality && tokenEquality && channelEquality) return 0;
        return -1;
    }

}
