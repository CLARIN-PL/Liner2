package g419.corpus.structure;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

public class AnnotationJunutTest {

  @Test
  public void testHashCode() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());

    final Annotation annotation1 = new Annotation(0, "", sentence);
    final Annotation annotation2 = new Annotation(0, "", sentence);

    Assert.assertEquals("Hashcode for the same object is diffrent.", annotation1.hashCode(), annotation1.hashCode());
    Assert.assertEquals("Hashcode for two equal objects is diffrent.", annotation1.hashCode(), annotation2.hashCode());
  }

  @Test
  public void testSetChannelIdx() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "";
    int channelIdx = 1;
    final Annotation annotation = new Annotation(begin, type, channelIdx, sentence);

    channelIdx = 2;
    annotation.setChannelIdx(channelIdx);
    Assert.assertEquals(annotation.getChannelIdx(), channelIdx);
  }

  @Test
  public void testGetChannelIdx() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "";
    int channelIdx = 1;
    final Annotation annotation = new Annotation(begin, type, channelIdx, sentence);

    Assert.assertEquals(annotation.getChannelIdx(), channelIdx);
    channelIdx = 2;
    annotation.setChannelIdx(channelIdx);
    Assert.assertEquals(annotation.getChannelIdx(), channelIdx);
  }

  @Test
  public void testHasHead() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "";
    final Annotation annotation = new Annotation(begin, type, sentence);

    Assert.assertTrue(annotation.hasHead());
    annotation.setHead(begin);
    Assert.assertTrue(annotation.hasHead());
  }

  @Test
  public void testGetHead() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "";
    final Annotation annotation = new Annotation(begin, type, sentence);

    final Integer head = 10;
    annotation.setHead(head);
    Assert.assertEquals("annotation.setHead did not caused annotation.getHead to return proper value", head, annotation.getHead());
  }

  @Test
  public void testSetHead() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "";
    final Annotation annotation = new Annotation(begin, type, sentence);

    annotation.setHead(begin);
    Assert.assertTrue("annotation.setHead did not caused annotation.hasHead to return true", annotation.hasHead());
  }

  @Test
  public void testAddToken() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "";
    final Annotation annotation = new Annotation(begin, type, sentence);

    final int token = 1;
    annotation.addToken(token);
    Assert.assertTrue("annotation.getTokens() does not contain prievously added token", annotation.getTokens().contains(token));
  }

  @Test
  public void testReplaceTokens() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "";
    final Annotation annotation = new Annotation(begin, type, sentence);

    final int tokensBegin = 10;
    final int tokensEnd = 30;
    annotation.replaceTokens(tokensBegin, tokensEnd);

    for (int token = tokensBegin; token <= tokensEnd; token++) {
      Assert.assertTrue("annotation.getTokens() does not contain prievously added token", annotation.getTokens().contains(token));
    }
  }

  @Test
  public void testGetId() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "";
    final Annotation annotation = new Annotation(begin, type, sentence);

    final String id = "test_id";
    annotation.setId(id);
    Assert.assertEquals("The id of annotation is diffrent than prievously set one", id, annotation.getId());
  }

  @Test
  public void testGetBegin() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "";
    final Annotation annotation = new Annotation(begin, type, sentence);

    Assert.assertEquals("The begin of annotation is diffrent than prievously set one", begin, annotation.getBegin());
  }

  @Test
  public void testGetEnd() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final int end = 1;
    final String type = "";
    final Annotation annotation = new Annotation(begin, end, type, sentence);

    Assert.assertEquals("The end of annotation is diffrent than prievously set one", end, annotation.getEnd());
  }

  @Test
  public void testGetTokens() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final int end = 10;
    final String type = "";
    final Annotation annotation = new Annotation(begin, end, type, sentence);

    for (int token = begin; token <= end; token++) {
      Assert.assertTrue("annotation.getTokens() does not contain prievously added token", annotation.getTokens().contains(token));
    }
  }

  @Test
  public void testGetSentence() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "";
    final Annotation annotation = new Annotation(begin, type, sentence);

    Assert.assertEquals("Sentence returned from annotation is diffrent than one used to construct this annotation", sentence, annotation.getSentence());
  }

  @Test
  public void testGetType() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "test_type";
    final Annotation annotation = new Annotation(begin, type, sentence);

    Assert.assertEquals("Type returned from annotation is diffrent than one used to construct this annotation", type, annotation.getType());
  }

  @Test
  public void testGetText() {
    final Sentence sentence1 = SentenceTest.getSampleSentence(new TokenAttributeIndex());

    final Annotation annotation1 = new Annotation(0, "", sentence1);
    final Annotation annotation2 = new Annotation(0, "", sentence1);

    try {
      final String text1 = annotation1.getText();
      final String text2 = annotation2.getText();

      Assert.assertEquals("Text for the same object is diffrent", text1, text1);
      Assert.assertEquals("Text for the same object is diffrent", text2, text2);
      Assert.assertEquals("Text for two same object is diffrent", text1, text2);

      // TODO Check if text is the same as it supposed to be

      // FIXME Tak zdanie musi mieć tokeny

    } catch (final IndexOutOfBoundsException e) {
      fail("Annotation expect that sentence have at least one token");
    }
  }

  @Test
  public void testSetId() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "";
    final Annotation annotation = new Annotation(begin, type, sentence);

    final String id = "test_id";
    annotation.setId(id);
    Assert.assertEquals("The id of annotation remain unchanged", id, annotation.getId());
  }

  @Test
  public void testSetType() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "test_type";
    final Annotation annotation = new Annotation(begin, type, sentence);

    final String newType = "new_test_type";
    annotation.setType(newType);
    Assert.assertEquals("Type returned from annotation is diffrent than prievously set one", newType, annotation.getType());
  }

  @Test
  public void testClone() {
    final Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final int begin = 0;
    final String type = "";
    final Annotation annotation = new Annotation(begin, type, sentence);

    final Annotation clonedAnnotation = annotation.clone();
    Assert.assertFalse("annotation and its clone are the same object (references are equal)", clonedAnnotation == annotation);
    Assert.assertEquals("annotation and its clone are not equal", annotation, clonedAnnotation);
  }

  @Test
  public void testEquals() {
    final Sentence sentence1 = SentenceTest.getSampleSentence(new TokenAttributeIndex());
    final Sentence sentence2 = SentenceTest.getSampleSentence(new TokenAttributeIndex());

    final Annotation annotation1 = new Annotation(0, "", sentence1);
    final Annotation annotation2 = new Annotation(0, "", sentence1);
    //Annotation annotation3 = new Annotation(0, "", sentence2);

    Assert.assertTrue("Annotation object is not equal to itself.", annotation1.equals(annotation1));
    Assert.assertTrue("Two same objects are not equal.", annotation1.equals(annotation2));
    //Assert.assertFalse("Two differnt objects are equal.", annotation1.equals(annotation3));
  }

}
