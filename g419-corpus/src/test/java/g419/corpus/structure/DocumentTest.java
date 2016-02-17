package g419.corpus.structure;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

public class DocumentTest {

	@Test
	public void testDocumentStringTokenAttributeIndex() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testDocumentStringArrayListOfParagraphTokenAttributeIndex() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testDocumentStringArrayListOfParagraphTokenAttributeIndexRelationSet() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetName() {
		String name = "Test name";
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
		Document document = new Document(name, attributeIndex);

		assertEquals(name, document.getName());
	}

	@Test
	public void testGetRelations() {
		String name = "Test name";
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
		Document document = new Document(name, attributeIndex);

		Sentence sentence1 = SentenceTest.getSampleSentence();
		Sentence sentence2 = SentenceTest.getSampleSentence();
		int begin1 = 0;
		int begin2 = 2;
		String type1 = "type1";
		String type2 = "type2";

		Annotation annotation1 = new Annotation(begin1, type1, sentence1);
		Annotation annotation2 = new Annotation(begin1, type1, sentence1);
		Annotation annotation3 = new Annotation(begin2, type2, sentence1);
		Annotation annotation4 = new Annotation(begin2, type2, sentence2);

		Relation relation1 = new Relation(annotation1, annotation2, type1);
		Relation relation2 = new Relation(annotation2, annotation3, type1);
		Relation relation3 = new Relation(annotation3, annotation4, type2);
		Relation relation4 = new Relation(annotation4, annotation1, type2);

		document.addRelation(relation1);
		document.addRelation(relation2);
		document.addRelation(relation3);
		document.addRelation(relation4);

		RelationSet relations = document.getRelations();

		assertTrue(relations.getRelations().contains(relation1));
		assertTrue(relations.getRelations().contains(relation2));
		assertTrue(relations.getRelations().contains(relation3));
		assertTrue(relations.getRelations().contains(relation4));
	}

	@Test
	public void testSetRelations() {
		String name = "Test name";
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
		Document document = new Document(name, attributeIndex);

		Sentence sentence1 = SentenceTest.getSampleSentence();
		Sentence sentence2 = SentenceTest.getSampleSentence();
		int begin1 = 0;
		int begin2 = 2;
		String type1 = "type1";
		String type2 = "type2";

		Annotation annotation1 = new Annotation(begin1, type1, sentence1);
		Annotation annotation2 = new Annotation(begin1, type1, sentence1);
		Annotation annotation3 = new Annotation(begin2, type2, sentence1);
		Annotation annotation4 = new Annotation(begin2, type2, sentence2);

		Relation relation1 = new Relation(annotation1, annotation2, type1);
		Relation relation2 = new Relation(annotation2, annotation3, type1);
		Relation relation3 = new Relation(annotation3, annotation4, type2);
		Relation relation4 = new Relation(annotation4, annotation1, type2);

		RelationSet relations = new RelationSet();

		relations.addRelation(relation1);
		relations.addRelation(relation2);
		relations.addRelation(relation3);
		relations.addRelation(relation4);

		document.setRelations(relations);

		RelationSet relationsFromDocument = document.getRelations();

		assertTrue(relationsFromDocument.getRelations().contains(relation1));
		assertTrue(relationsFromDocument.getRelations().contains(relation2));
		assertTrue(relationsFromDocument.getRelations().contains(relation3));
		assertTrue(relationsFromDocument.getRelations().contains(relation4));
	}

	@Test
	public void testAddParagraph() {
		String name = "Test name";
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
		Document document = new Document(name, attributeIndex);

		String id1 = "id1";
		Paragraph paragraph1 = new Paragraph(id1);
		document.addParagraph(paragraph1);

		assertTrue(document.getParagraphs().contains(paragraph1));
	}

	@Test
	public void testGetAttributeIndex() {
		String name = "Test name";
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
		Document document = new Document(name, attributeIndex);

		assertEquals(attributeIndex, document.getAttributeIndex());
	}

	@Test
	public void testGetParagraphs() {
		String name = "Test name";
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
		Document document = new Document(name, attributeIndex);

		String id1 = "id1";
		String id2 = "id2";
		String id3 = "id3";
		String id4 = "id4";

		Paragraph paragraph1 = new Paragraph(id1);
		Paragraph paragraph2 = new Paragraph(id2);
		Paragraph paragraph3 = new Paragraph(id3);
		Paragraph paragraph4 = new Paragraph(id4);

		document.addParagraph(paragraph1);
		document.addParagraph(paragraph2);
		document.addParagraph(paragraph3);
		document.addParagraph(paragraph4);

		assertTrue(document.getParagraphs().contains(paragraph1));
		assertTrue(document.getParagraphs().contains(paragraph2));
		assertTrue(document.getParagraphs().contains(paragraph3));
		assertTrue(document.getParagraphs().contains(paragraph4));
	}

	@Test
	public void testSetAttributeIndex() {
		String name = "Test name";
		TokenAttributeIndex attributeIndex1 = new TokenAttributeIndex();
		Document document = new Document(name, attributeIndex1);
		
		TokenAttributeIndex attributeIndex2 = new TokenAttributeIndex();
		document.setAttributeIndex(attributeIndex2);

		assertEquals(attributeIndex2, document.getAttributeIndex());
	}

	@Test
	public void testGetChunkings() {
		String name = "Test name";
		TokenAttributeIndex attributeIndex1 = new TokenAttributeIndex();
		Document document = new Document(name, attributeIndex1);
		
		Sentence sentence1 = SentenceTest.getSampleSentence();
		Sentence sentence2 = SentenceTest.getSampleSentence();
		int begin1 = 0;
		int begin2 = 2;
		String type1 = "type1";
		String type2 = "type2";

		Annotation annotation1 = new Annotation(begin1, type1, sentence1);
		Annotation annotation2 = new Annotation(begin1, type1, sentence1);
		Annotation annotation3 = new Annotation(begin2, type2, sentence1);
		Annotation annotation4 = new Annotation(begin2, type2, sentence2);

		Relation relation1 = new Relation(annotation1, annotation2, type1);
		Relation relation2 = new Relation(annotation2, annotation3, type1);
		Relation relation3 = new Relation(annotation3, annotation4, type2);
		Relation relation4 = new Relation(annotation4, annotation1, type2);

		RelationSet relations = new RelationSet();

		relations.addRelation(relation1);
		relations.addRelation(relation2);
		relations.addRelation(relation3);
		relations.addRelation(relation4);

		document.setRelations(relations);
		
		HashMap<Sentence, AnnotationSet> chunkings = document.getChunkings();
		assertNotNull(chunkings);
		
		// TODO CHeck rest of cases
	}

	@Test
	public void testAddAnnotations() {
		String name = "Test name";
		TokenAttributeIndex attributeIndex1 = new TokenAttributeIndex();
		Document document = new Document(name, attributeIndex1);
		
		Sentence sentence1 = SentenceTest.getSampleSentence();
		Sentence sentence2 = SentenceTest.getSampleSentence();
		
		Paragraph p = new Paragraph("p1");
		p.addSentence(sentence1);
		p.addSentence(sentence2);
		document.addParagraph(p);
		
		int begin1 = 0;
		int begin2 = 2;
		String type1 = "type1";
		String type2 = "type2";

		Annotation annotation1 = new Annotation(begin1, type1, sentence1);
		Annotation annotation2 = new Annotation(begin1, type1, sentence1);
		Annotation annotation3 = new Annotation(begin2, type2, sentence1);
		Annotation annotation4 = new Annotation(begin2, type2, sentence2);
		
		AnnotationSet annotationSet1 = new AnnotationSet(sentence1);
		AnnotationSet annotationSet2 = new AnnotationSet(sentence2);
		
		annotationSet1.addChunk(annotation1);
		annotationSet1.addChunk(annotation2);
		annotationSet1.addChunk(annotation3);
		annotationSet2.addChunk(annotation4);
		
		HashMap<Sentence, AnnotationSet> chunkings1 = new HashMap<>();
		HashMap<Sentence, AnnotationSet> chunkings2 = new HashMap<>();
		
		chunkings1.put(annotationSet1.getSentence(), annotationSet1);
		chunkings2.put(annotationSet2.getSentence(), annotationSet2);
		
		document.addAnnotations(chunkings1);
		document.addAnnotations(chunkings2);
		
		HashMap<Sentence, AnnotationSet> chunkingsFromDocument = document.getChunkings();
		
		assertTrue(chunkingsFromDocument.containsKey(sentence1));
		assertTrue(chunkingsFromDocument.containsKey(sentence2));
		assertFalse(chunkingsFromDocument.containsKey(new Sentence()));
	}

	@Test
	public void testSetAnnotations() {
		String name = "Test name";
		TokenAttributeIndex attributeIndex1 = new TokenAttributeIndex();
		Document document = new Document(name, attributeIndex1);
		
		Sentence sentence1 = SentenceTest.getSampleSentence();
		Sentence sentence2 = SentenceTest.getSampleSentence();

		Paragraph p = new Paragraph("p1");
		p.addSentence(sentence1);
		p.addSentence(sentence2);
		document.addParagraph(p);

		int begin1 = 0;
		int begin2 = 2;
		String type1 = "type1";
		String type2 = "type2";

		Annotation annotation1 = new Annotation(begin1, type1, sentence1);
		Annotation annotation2 = new Annotation(begin1, type1, sentence1);
		Annotation annotation3 = new Annotation(begin2, type2, sentence1);
		Annotation annotation4 = new Annotation(begin2, type2, sentence2);
		
		AnnotationSet annotationSet1 = new AnnotationSet(sentence1);
		AnnotationSet annotationSet2 = new AnnotationSet(sentence2);
		
		annotationSet1.addChunk(annotation1);
		annotationSet1.addChunk(annotation2);
		annotationSet1.addChunk(annotation3);
		annotationSet2.addChunk(annotation4);
		
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<>(4);
		
		chunkings.put(annotationSet1.getSentence(), annotationSet1);
		chunkings.put(annotationSet2.getSentence(), annotationSet2);
		
		document.setAnnotations(chunkings);
		
		HashMap<Sentence, AnnotationSet> chunkingsFromDocument = document.getChunkings();
		
		assertTrue(chunkingsFromDocument.containsKey(sentence1));
		assertTrue(chunkingsFromDocument.containsKey(sentence2));
		//assertFalse(chunkingsFromDocument.containsKey(new Sentence()));
	}

	@Test
	public void testGetSentences() {
		String name = "Test name";
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
		Document document = new Document(name, attributeIndex);

		String id1 = "id1";

		Paragraph paragraph1 = new Paragraph(id1);
		Sentence sentence1 = new Sentence();
		Sentence sentence2 = new Sentence();
		Sentence sentence3 = new Sentence();
		Sentence sentence4 = new Sentence();
		paragraph1.addSentence(sentence1);
		paragraph1.addSentence(sentence2);
		paragraph1.addSentence(sentence3);
		paragraph1.addSentence(sentence4);

		document.addParagraph(paragraph1);

		assertTrue(document.getParagraphs().contains(paragraph1));
		
		ArrayList<Sentence> sentencesFromDocument = document.getSentences();
		
		assertTrue(sentencesFromDocument.contains(sentence1));
		assertTrue(sentencesFromDocument.contains(sentence2));
		assertTrue(sentencesFromDocument.contains(sentence3));
		assertTrue(sentencesFromDocument.contains(sentence4));
	}

	@Test
	public void testRemoveAnnotationsString() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testRemoveAnnotations() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetAnnotation() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testClone() {
		String name = "Test name";
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
		
		Document document1 = new Document(name, attributeIndex);		
		Document document2 = document1.clone();
		
		// TODO błędny test, referencja sklonowanego dokumentu jest inna niż oryginał
		//assertEquals(document1,document2);
	}

}
