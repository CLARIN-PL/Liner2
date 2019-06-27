//package g419.corpus.structure;
//
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import static org.junit.Assert.*;
//
//public class DocumentTest {
//
//  @Test
//  public void testDocumentStringTokenAttributeIndex() {
//    //fail("Not yet implemented"); // TODO
//  }
//
//  @Test
//  public void testDocumentStringArrayListOfParagraphTokenAttributeIndex() {
//    //fail("Not yet implemented"); // TODO
//  }
//
//  @Test
//  public void testDocumentStringArrayListOfParagraphTokenAttributeIndexRelationSet() {
//    //fail("Not yet implemented"); // TODO
//  }
//
//  @Test
//  public void testGetName() {
//    final String name = "Test name";
//    final TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
//    final Document document = new Document(name, attributeIndex);
//
//    assertEquals(name, document.getName());
//  }
//
//  @Test
//  public void testGetRelations() {
//    final String name = "Test name";
//    final TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
//    final Document document = new Document(name, attributeIndex);
//
//    final Sentence sentence1 = SentenceTest.getSampleSentence(attributeIndex);
//    final Sentence sentence2 = SentenceTest.getSampleSentence(attributeIndex);
//    final int begin1 = 0;
//    final int begin2 = 2;
//    final String type1 = "type1";
//    final String type2 = "type2";
//
//    final Annotation annotation1 = new Annotation(begin1, type1, sentence1);
//    final Annotation annotation2 = new Annotation(begin1, type1, sentence1);
//    final Annotation annotation3 = new Annotation(begin2, type2, sentence1);
//    final Annotation annotation4 = new Annotation(begin2, type2, sentence2);
//
//    final Relation relation1 = new Relation(annotation1, annotation2, type1);
//    final Relation relation2 = new Relation(annotation2, annotation3, type1);
//    final Relation relation3 = new Relation(annotation3, annotation4, type2);
//    final Relation relation4 = new Relation(annotation4, annotation1, type2);
//
//    document.addRelation(relation1);
//    document.addRelation(relation2);
//    document.addRelation(relation3);
//    document.addRelation(relation4);
//
//    final RelationSet relations = document.getRelations();
//
//    assertTrue(relations.getRelations().contains(relation1));
//    assertTrue(relations.getRelations().contains(relation2));
//    assertTrue(relations.getRelations().contains(relation3));
//    assertTrue(relations.getRelations().contains(relation4));
//  }
//
//  @Test
//  public void testSetRelations() {
//    final String name = "Test name";
//    final TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
//    final Document document = new Document(name, attributeIndex);
//
//    final Sentence sentence1 = SentenceTest.getSampleSentence(attributeIndex);
//    final Sentence sentence2 = SentenceTest.getSampleSentence(attributeIndex);
//    final int begin1 = 0;
//    final int begin2 = 2;
//    final String type1 = "type1";
//    final String type2 = "type2";
//
//    final Annotation annotation1 = new Annotation(begin1, type1, sentence1);
//    final Annotation annotation2 = new Annotation(begin1, type1, sentence1);
//    final Annotation annotation3 = new Annotation(begin2, type2, sentence1);
//    final Annotation annotation4 = new Annotation(begin2, type2, sentence2);
//
//    final Relation relation1 = new Relation(annotation1, annotation2, type1);
//    final Relation relation2 = new Relation(annotation2, annotation3, type1);
//    final Relation relation3 = new Relation(annotation3, annotation4, type2);
//    final Relation relation4 = new Relation(annotation4, annotation1, type2);
//
//    final RelationSet relations = new RelationSet();
//
//    relations.addRelation(relation1);
//    relations.addRelation(relation2);
//    relations.addRelation(relation3);
//    relations.addRelation(relation4);
//
//    document.setRelations(relations);
//
//    final RelationSet relationsFromDocument = document.getRelations();
//
//    assertTrue(relationsFromDocument.getRelations().contains(relation1));
//    assertTrue(relationsFromDocument.getRelations().contains(relation2));
//    assertTrue(relationsFromDocument.getRelations().contains(relation3));
//    assertTrue(relationsFromDocument.getRelations().contains(relation4));
//  }
//
//  @Test
//  public void testAddParagraph() {
//    final String name = "Test name";
//    final TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
//    final Document document = new Document(name, attributeIndex);
//
//    final String id1 = "id1";
//    final Paragraph paragraph1 = new Paragraph(id1);
//    document.addParagraph(paragraph1);
//
//    assertTrue(document.getParagraphs().contains(paragraph1));
//  }
//
//  @Test
//  public void testGetAttributeIndex() {
//    final String name = "Test name";
//    final TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
//    final Document document = new Document(name, attributeIndex);
//
//    assertEquals(attributeIndex, document.getAttributeIndex());
//  }
//
//  @Test
//  public void testGetParagraphs() {
//    final String name = "Test name";
//    final TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
//    final Document document = new Document(name, attributeIndex);
//
//    final String id1 = "id1";
//    final String id2 = "id2";
//    final String id3 = "id3";
//    final String id4 = "id4";
//
//    final Paragraph paragraph1 = new Paragraph(id1);
//    final Paragraph paragraph2 = new Paragraph(id2);
//    final Paragraph paragraph3 = new Paragraph(id3);
//    final Paragraph paragraph4 = new Paragraph(id4);
//
//    document.addParagraph(paragraph1);
//    document.addParagraph(paragraph2);
//    document.addParagraph(paragraph3);
//    document.addParagraph(paragraph4);
//
//    assertTrue(document.getParagraphs().contains(paragraph1));
//    assertTrue(document.getParagraphs().contains(paragraph2));
//    assertTrue(document.getParagraphs().contains(paragraph3));
//    assertTrue(document.getParagraphs().contains(paragraph4));
//  }
//
//  @Test
//  public void testSetAttributeIndex() {
//    final String name = "Test name";
//    final TokenAttributeIndex attributeIndex1 = new TokenAttributeIndex();
//    final Document document = new Document(name, attributeIndex1);
//
//    final TokenAttributeIndex attributeIndex2 = new TokenAttributeIndex();
//    document.setAttributeIndex(attributeIndex2);
//
//    assertEquals(attributeIndex2, document.getAttributeIndex());
//  }
//
//  @Test
//  public void testGetChunkings() {
//    final String name = "Test name";
//    final TokenAttributeIndex attributeIndex1 = new TokenAttributeIndex();
//    final Document document = new Document(name, attributeIndex1);
//
//    final Sentence sentence1 = SentenceTest.getSampleSentence(attributeIndex1);
//    final Sentence sentence2 = SentenceTest.getSampleSentence(attributeIndex1);
//    final int begin1 = 0;
//    final int begin2 = 2;
//    final String type1 = "type1";
//    final String type2 = "type2";
//
//    final Annotation annotation1 = new Annotation(begin1, type1, sentence1);
//    final Annotation annotation2 = new Annotation(begin1, type1, sentence1);
//    final Annotation annotation3 = new Annotation(begin2, type2, sentence1);
//    final Annotation annotation4 = new Annotation(begin2, type2, sentence2);
//
//    final Relation relation1 = new Relation(annotation1, annotation2, type1);
//    final Relation relation2 = new Relation(annotation2, annotation3, type1);
//    final Relation relation3 = new Relation(annotation3, annotation4, type2);
//    final Relation relation4 = new Relation(annotation4, annotation1, type2);
//
//    final RelationSet relations = new RelationSet();
//
//    relations.addRelation(relation1);
//    relations.addRelation(relation2);
//    relations.addRelation(relation3);
//    relations.addRelation(relation4);
//
//    document.setRelations(relations);
//
//    final HashMap<Sentence, AnnotationSet> chunkings = document.getChunkings();
//    assertNotNull(chunkings);
//
//    // TODO CHeck rest of cases
//  }
//
//  @Test
//  public void testAddAnnotations() {
//    final String name = "Test name";
//
//    final TokenAttributeIndex index = new TokenAttributeIndex();
//
//    final Sentence sentence1 = SentenceTest.getSampleSentence(index);
//    final Sentence sentence2 = SentenceTest.getSampleSentence(index);
//
//    final Document document = new Document(name, index);
//
//    final Paragraph p = new Paragraph("p1");
//    p.addSentence(sentence1);
//    p.addSentence(sentence2);
//    document.addParagraph(p);
//
//    final int begin1 = 0;
//    final int begin2 = 2;
//    final String type1 = "type1";
//    final String type2 = "type2";
//
//    final Annotation annotation1 = new Annotation(begin1, type1, sentence1);
//    final Annotation annotation2 = new Annotation(begin1, type1, sentence1);
//    final Annotation annotation3 = new Annotation(begin2, type2, sentence1);
//    final Annotation annotation4 = new Annotation(begin2, type2, sentence2);
//
//    final AnnotationSet annotationSet1 = new AnnotationSet(sentence1);
//    final AnnotationSet annotationSet2 = new AnnotationSet(sentence2);
//
//    annotationSet1.addChunk(annotation1);
//    annotationSet1.addChunk(annotation2);
//    annotationSet1.addChunk(annotation3);
//    annotationSet2.addChunk(annotation4);
//
//    final HashMap<Sentence, AnnotationSet> chunkings1 = new HashMap<>();
//    final HashMap<Sentence, AnnotationSet> chunkings2 = new HashMap<>();
//
//    chunkings1.put(annotationSet1.getSentence(), annotationSet1);
//    chunkings2.put(annotationSet2.getSentence(), annotationSet2);
//
//    document.addAnnotations(chunkings1);
//    document.addAnnotations(chunkings2);
//
//    final HashMap<Sentence, AnnotationSet> chunkingsFromDocument = document.getChunkings();
//
//    assertTrue(chunkingsFromDocument.containsKey(sentence1));
//    assertTrue(chunkingsFromDocument.containsKey(sentence2));
//    assertFalse(chunkingsFromDocument.containsKey(new Sentence()));
//  }
//
//  @Test
//  public void testSetAnnotations() {
//    final String name = "Test name";
//    final TokenAttributeIndex attributeIndex1 = new TokenAttributeIndex();
//    final Document document = new Document(name, attributeIndex1);
//
//    final Sentence sentence1 = SentenceTest.getSampleSentence(attributeIndex1);
//    final Sentence sentence2 = SentenceTest.getSampleSentence(attributeIndex1);
//
//    final Paragraph p = new Paragraph("p1");
//    p.addSentence(sentence1);
//    p.addSentence(sentence2);
//    document.addParagraph(p);
//
//    final int begin1 = 0;
//    final int begin2 = 2;
//    final String type1 = "type1";
//    final String type2 = "type2";
//
//    final Annotation annotation1 = new Annotation(begin1, type1, sentence1);
//    final Annotation annotation2 = new Annotation(begin1, type1, sentence1);
//    final Annotation annotation3 = new Annotation(begin2, type2, sentence1);
//    final Annotation annotation4 = new Annotation(begin2, type2, sentence2);
//
//    final AnnotationSet annotationSet1 = new AnnotationSet(sentence1);
//    final AnnotationSet annotationSet2 = new AnnotationSet(sentence2);
//
//    annotationSet1.addChunk(annotation1);
//    annotationSet1.addChunk(annotation2);
//    annotationSet1.addChunk(annotation3);
//    annotationSet2.addChunk(annotation4);
//
//    final HashMap<Sentence, AnnotationSet> chunkings = new HashMap<>(4);
//
//    chunkings.put(annotationSet1.getSentence(), annotationSet1);
//    chunkings.put(annotationSet2.getSentence(), annotationSet2);
//
//    document.setAnnotations(chunkings);
//
//    final HashMap<Sentence, AnnotationSet> chunkingsFromDocument = document.getChunkings();
//
//    assertTrue(chunkingsFromDocument.containsKey(sentence1));
//    assertTrue(chunkingsFromDocument.containsKey(sentence2));
//    //assertFalse(chunkingsFromDocument.containsKey(new Sentence()));
//  }
//
//  @Test
//  public void testGetSentences() {
//    final String name = "Test name";
//    final TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
//    final Document document = new Document(name, attributeIndex);
//
//    final String id1 = "id1";
//
//    final Paragraph paragraph1 = new Paragraph(id1);
//    final Sentence sentence1 = new Sentence();
//    final Sentence sentence2 = new Sentence();
//    final Sentence sentence3 = new Sentence();
//    final Sentence sentence4 = new Sentence();
//    paragraph1.addSentence(sentence1);
//    paragraph1.addSentence(sentence2);
//    paragraph1.addSentence(sentence3);
//    paragraph1.addSentence(sentence4);
//
//    document.addParagraph(paragraph1);
//
//    assertTrue(document.getParagraphs().contains(paragraph1));
//
//    final ArrayList<Sentence> sentencesFromDocument = document.getSentences();
//
//    assertTrue(sentencesFromDocument.contains(sentence1));
//    assertTrue(sentencesFromDocument.contains(sentence2));
//    assertTrue(sentencesFromDocument.contains(sentence3));
//    assertTrue(sentencesFromDocument.contains(sentence4));
//  }
//
//  @Test
//  public void testRemoveAnnotationsString() {
//    //fail("Not yet implemented"); // TODO
//  }
//
//  @Test
//  public void testRemoveAnnotations() {
//    //fail("Not yet implemented"); // TODO
//  }
//
//  @Test
//  public void testGetAnnotation() {
//    //fail("Not yet implemented"); // TODO
//  }
//
//  @Test
//  public void testClone() {
//    final String name = "Test name";
//    final TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
//
//    final Document document1 = new Document(name, attributeIndex);
//    final Document document2 = document1.clone();
//
//    // TODO błędny test, referencja sklonowanego dokumentu jest inna niż oryginał
//    //assertEquals(document1,document2);
//  }
//
//}
