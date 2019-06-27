//package g419.corpus.structure;
//
//import org.junit.Test;
//
//import static org.junit.Assert.assertEquals;
//
//public class AnnotationPositionComparatorTest {
//
//  @Test
//  public void testCompare() {
//    final int lower = -1;
//    final int equal = 0;
//    final int greater = 1;
//
//    final Sentence sentence1 = SentenceTest.getSampleSentence(new TokenAttributeIndex());
//    sentence1.setId("sentence1");
//
//    final Sentence sentence2 = SentenceTest.getSampleSentence(new TokenAttributeIndex());
//    sentence2.setId("sentence2");
//
//    final int begin1 = 0;
//    final int end1 = 0;
//    final int begin2 = 1;
//    final int end2 = 2;
//    final String type = "";
//    final Annotation annotation1 = new Annotation(begin1, end1, type, sentence1);
//    final Annotation annotation2 = new Annotation(begin1, end1, type, sentence1);
//    final Annotation annotation3 = new Annotation(begin2, end2, type, sentence1);
//    final Annotation annotation4 = new Annotation(begin1, end1, type, sentence2);
//    final Annotation annotation5 = new Annotation(begin1, end2, type, sentence2);
//
//    final AnnotationPositionComparator annotationPositionComparator = new AnnotationPositionComparator();
//
//    assertEquals(
//        "Annotation is not equal to itself",
//        equal,
//        signum(annotationPositionComparator.compare(annotation1, annotation1))
//    );
//    assertEquals(
//        "Annotation is not equal to same annotation",
//        equal,
//        signum(annotationPositionComparator.compare(annotation1, annotation2))
//    );
//    assertEquals(
//        "Annotation is supposed to be lower if is earlier in senence",
//        lower,
//        signum(annotationPositionComparator.compare(annotation1, annotation3))
//    );
//    assertEquals(
//        "Annotation is supposed to be greater if is later in senence",
//        greater,
//        signum(annotationPositionComparator.compare(annotation3, annotation1))
//    );
//    assertEquals(
//        "Annotation is supposed to be lower if is set of tokens is smaller",
//        lower,
//        signum(annotationPositionComparator.compare(annotation4, annotation5))
//    );
//    assertEquals(
//        "Annotation is supposed to be greater if is set of tokens is bigger",
//        greater,
//        signum(annotationPositionComparator.compare(annotation5, annotation4))
//    );
//
//    //TODO What happens if two sentences are different?
//  }
//
//  static private int signum(final int x) {
//    return x == 0 ? 0 : (x < 0 ? -1 : 1);
//  }
//
//}
