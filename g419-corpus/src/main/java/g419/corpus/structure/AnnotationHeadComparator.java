package g419.corpus.structure;

import java.util.Comparator;

public class AnnotationHeadComparator implements Comparator<Annotation> {

  private final boolean sameChannel;

  public AnnotationHeadComparator() {
    sameChannel = false;
  }

  public AnnotationHeadComparator(final boolean sameChannel) {
    this.sameChannel = sameChannel;
  }


  @Override
  public int compare(final Annotation ann1, final Annotation ann2) {
    final boolean channelEquality = !sameChannel || (ann1.getType() != null && ann1.getType().equals(ann2.getType()));
    if(!ann1.getSentence().hasId() || !ann1.getSentence().hasId())
      return -1;
    if (ann1.getSentence().getId().equals(ann2.getSentence().getId()) && channelEquality) {
      return (ann1.getHead()).compareTo(ann2.getHead());
    } else {
      return -1;
    }
  }
}
