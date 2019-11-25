package g419.corpus.structure;

import java.util.Comparator;

public class AnnotationTokenListComparator implements Comparator<Annotation> {

  private final boolean sameChannel;
  private Document refDocument;

  public AnnotationTokenListComparator() {
    sameChannel = false;
  }

  public AnnotationTokenListComparator(final boolean sameChannel) {
    this.sameChannel = sameChannel;
  }

  @Override
  public int compare(final Annotation ann1, final Annotation ann2) {
    final boolean channelEquality = !sameChannel || (ann1.getType() != null && ann1.getType().equals(ann2.getType()));
    // TODO: Refaktoring mapowania nazw własnych tei-ccl
    //ann1.getSentence().getOrd() == ann2.getSentence().getOrd() &&
    if (ann1.getTokens().equals(ann2.getTokens()) && channelEquality) {
      return 0;
    }
    return -1;
  }
}
