package g419.corpus.structure;

import org.junit.Test;

public class SentenceTest {

  @Test
  public void testSentence() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testAddChunk() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testAddAnnotations() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testAddToken() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testGetId() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testHasId() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testGetChunksAt() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testGetTokenClassLabel() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testGetChunks() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testGetAttributeIndexLength() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testGetAttributeIndex() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testGetTokenNumber() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testGetTokens() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testSetAttributeIndex() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testSetAnnotations() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testSetId() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testAnnotationsToString() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testRemoveAnnotations() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testGetAnnotationInChannel() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testToString() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testSetTokens() {
    //fail("Not yet implemented"); // TODO
  }

  @Test
  public void testClone() {
    //fail("Not yet implemented"); // TODO
  }

  /**
   * Tworzy przykładowe zdanie "Ala ma kota.".
   *
   * @return
   */
  public static Sentence getSampleSentence(TokenAttributeIndex index) {
    Sentence sentence = new Sentence(index);
    if (index.getIndex("orth") == -1) {
      index.addAttribute("orth");
    }
    sentence.addToken(new Token("Ala", new Tag("Ala", "subst:sg:nom:f", true), index));
    sentence.addToken(new Token("ma", new Tag("mieć", "fin:sg:ter:imperf", true), index));
    sentence.addToken(new Token("kota", new Tag("kot", "subst:sg:gen:m2", true), index));
    sentence.addToken(new Token(".", new Tag(".", "interp", true), index));
    return sentence;
  }

}
