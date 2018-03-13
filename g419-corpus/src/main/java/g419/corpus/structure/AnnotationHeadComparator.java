package g419.corpus.structure;

import java.util.Comparator;

public class AnnotationHeadComparator implements Comparator<Annotation> {

    private final boolean sameChannel;

    public AnnotationHeadComparator() {
        this.sameChannel = false;
    }

    public AnnotationHeadComparator(boolean sameChannel) {
        this.sameChannel = sameChannel;
    }


    @Override
    public int compare(Annotation ann1, Annotation ann2) {
        boolean channelEquality = !sameChannel || (ann1.getType() != null && ann1.getType().equals(ann2.getType()));
        if (ann1.getSentence().getId().equals(ann2.getSentence().getId()) && channelEquality) {
            return (ann1.getHead()).compareTo(ann2.getHead());
        } else {
            return -1;
        }
    }

}
