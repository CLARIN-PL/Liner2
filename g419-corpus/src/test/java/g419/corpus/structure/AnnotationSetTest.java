package g419.corpus.structure;

import org.junit.Test;

import static org.junit.Assert.*;

public class AnnotationSetTest {

	@Test
	public void testAnnotationSetSentenceLinkedHashSetOfAnnotation() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testAnnotationSetSentence() {
		Sentence sentence = new Sentence();
		AnnotationSet annotationSet = new AnnotationSet(sentence);

		assertNotNull(annotationSet);
		assertEquals(sentence, annotationSet.getSentence());
	}

	@Test
	public void testAddChunk() {
		Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		Annotation annotation = new Annotation(1, "", sentence);
		AnnotationSet annotationSet = new AnnotationSet(sentence);

		annotationSet.addChunk(annotation);
		assertTrue(annotationSet.chunkSet().contains(annotation));
		assertTrue(annotationSet.contains(annotation));
	}

	@Test
	public void testRemoveChunk() {
		Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		Annotation annotation = new Annotation(1, "", sentence);
		AnnotationSet annotationSet = new AnnotationSet(sentence);

		annotationSet.addChunk(annotation);
		annotationSet.removeChunk(annotation);
		assertFalse(annotationSet.chunkSet().contains(annotation));
		assertFalse(annotationSet.contains(annotation));
	}

	@Test
	public void testChunkSet() {
		Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		Annotation annotation1 = new Annotation(1, "", sentence);
		Annotation annotation2 = new Annotation(2, "", sentence);
		Annotation annotation3 = new Annotation(3, "", sentence);
		AnnotationSet annotationSet = new AnnotationSet(sentence);

		assertNotNull(annotationSet.chunkSet());

		annotationSet.addChunk(annotation1);
		annotationSet.addChunk(annotation2);
		annotationSet.addChunk(annotation3);

		assertEquals(3, annotationSet.chunkSet().size());
		assertTrue(annotationSet.chunkSet().contains(annotation1));
		assertTrue(annotationSet.chunkSet().contains(annotation2));
		assertTrue(annotationSet.chunkSet().contains(annotation3));
	}

	@Test
	public void testGetSentence() {
		Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		AnnotationSet annotationSet = new AnnotationSet(sentence);

		assertNotNull(annotationSet.getSentence());
		assertEquals(sentence, annotationSet.getSentence());
	}

	@Test
	public void testContains() {
		Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		Annotation annotation1 = new Annotation(1, "", sentence);
		Annotation annotation2 = new Annotation(2, "", sentence);
		Annotation annotation3 = new Annotation(3, "", sentence);
		AnnotationSet annotationSet = new AnnotationSet(sentence);

		annotationSet.addChunk(annotation1);
		annotationSet.addChunk(annotation2);

		assertTrue(annotationSet.contains(annotation1));
		assertTrue(annotationSet.contains(annotation2));
		assertFalse(annotationSet.contains(annotation3));
	}

	// TODO Test do poprawy i podziału na mniejsze elementy.
	@Test
	public void testUnion() {
		// Scenario 1 (same sentence, not intersecting annotation sets)
		Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		Annotation annotation1 = new Annotation(1, "", sentence);
		Annotation annotation2 = new Annotation(2, "", sentence);
		Annotation annotation3 = new Annotation(3, "", sentence);
		AnnotationSet annotationSet1 = new AnnotationSet(sentence);
		AnnotationSet annotationSet2 = new AnnotationSet(sentence);

		annotationSet1.addChunk(annotation1);
		annotationSet1.addChunk(annotation2);
		annotationSet2.addChunk(annotation3);

		annotationSet1.union(annotationSet2);

		assertTrue(annotationSet1.contains(annotation1));
		assertTrue(annotationSet1.contains(annotation2));
		assertTrue(annotationSet1.contains(annotation3));

		// Scenario 2 (same sentence, intersecting annotation sets)
		sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		annotation1 = new Annotation(1, "", sentence);
		annotation2 = new Annotation(2, "", sentence);
		annotation3 = new Annotation(3, "", sentence);
		annotationSet1 = new AnnotationSet(sentence);
		annotationSet2 = new AnnotationSet(sentence);

		annotationSet1.addChunk(annotation1);
		annotationSet1.addChunk(annotation2);
		annotationSet2.addChunk(annotation2);
		annotationSet2.addChunk(annotation3);

		annotationSet1.union(annotationSet2);

//		assertFalse(annotationSet1.contains(annotation1));
//		assertFalse(annotationSet1.contains(annotation2));
//		assertFalse(annotationSet1.contains(annotation3));

		// Scenario 3 (same sentence, equal annotation sets)
		sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		annotation1 = new Annotation(1, "", sentence);
		annotation2 = new Annotation(2, "", sentence);
		annotation3 = new Annotation(3, "", sentence);
		annotationSet1 = new AnnotationSet(sentence);
		annotationSet2 = new AnnotationSet(sentence);

		annotationSet1.addChunk(annotation1);
		annotationSet1.addChunk(annotation2);
		annotationSet1.addChunk(annotation3);
		annotationSet2.addChunk(annotation1);
		annotationSet2.addChunk(annotation2);
		annotationSet2.addChunk(annotation3);

		annotationSet1.union(annotationSet2);

//		assertFalse(annotationSet1.contains(annotation1));
//		assertFalse(annotationSet1.contains(annotation2));
//		assertFalse(annotationSet1.contains(annotation3));

		// Scenario 4 (same sentence, same annotation set)
		sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		annotation1 = new Annotation(1, "", sentence);
		annotation2 = new Annotation(2, "", sentence);
		annotationSet1 = new AnnotationSet(sentence);

		annotationSet1.addChunk(annotation1);
		annotationSet1.addChunk(annotation2);

		annotationSet1.union(annotationSet1);

//		assertFalse(annotationSet1.contains(annotation1));
//		assertFalse(annotationSet1.contains(annotation2));

		// Scenario 5 (different sentence, not intersecting annotation sets)
		// TODO What should happen? Exception or true/false, for now nothing.
		sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		Sentence sentence2 = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		annotation1 = new Annotation(1, "", sentence);
		annotation2 = new Annotation(2, "", sentence2);
		annotationSet1 = new AnnotationSet(sentence);
		annotationSet2 = new AnnotationSet(sentence2);

		annotationSet1.addChunk(annotation1);
		annotationSet2.addChunk(annotation2);

		annotationSet1.union(annotationSet2);

//		assertFalse(annotationSet1.contains(annotation1));
//		assertTrue(annotationSet1.contains(annotation2));
	}

	// TODO test do poprawy i podziału na mniejsze
	@Test
	public void testGetAnnotationTypes() {
		Sentence sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		Annotation annotation1 = new Annotation(1, "1", sentence);
		Annotation annotation2 = new Annotation(2, "2", sentence);
		Annotation annotation3 = new Annotation(3, "3", sentence);
		AnnotationSet annotationSet = new AnnotationSet(sentence);

		// Scenario 1 (empty annotation set)
		assertNotNull(annotationSet.getAnnotationTypes());
		assertTrue(annotationSet.getAnnotationTypes().isEmpty());
		
		// Scenario 2 (filled annotation set, different types)
		annotationSet.addChunk(annotation1);
		annotationSet.addChunk(annotation2);
		annotationSet.addChunk(annotation3);

		assertEquals(3, annotationSet.getAnnotationTypes().size());
		assertTrue(annotationSet.getAnnotationTypes().contains(annotation1.getType()));
		assertFalse(annotationSet.getAnnotationTypes().contains("4"));
		
		// Scenario 3 (filled annotation set, same type)
		//TODO
		sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		annotation1 = new Annotation(1, "1", sentence);
		annotation2 = new Annotation(2, "1", sentence);
		annotation3 = new Annotation(3, "1", sentence);
		annotationSet = new AnnotationSet(sentence);
		
		annotationSet.addChunk(annotation1);
		annotationSet.addChunk(annotation2);
		annotationSet.addChunk(annotation3);
		
//		assertFalse(annotationSet.getAnnotationTypes().contains(annotation1.getType()));
//		assertFalse(annotationSet.getAnnotationTypes().contains(annotation2.getType()));
//		assertFalse(annotationSet.getAnnotationTypes().contains(annotation3.getType()));
		
		// Scenario 4 (filled annotation set, mixed types)
		//TODO
		sentence = SentenceTest.getSampleSentence(new TokenAttributeIndex());
		annotation1 = new Annotation(1, "1", sentence);
		annotation2 = new Annotation(2, "1", sentence);
		annotation3 = new Annotation(3, "2", sentence);
		annotationSet = new AnnotationSet(sentence);
		
		annotationSet.addChunk(annotation1);
		annotationSet.addChunk(annotation2);
		annotationSet.addChunk(annotation3);
		
//		assertFalse(annotationSet.getAnnotationTypes().contains(annotation1.getType()));
//		assertFalse(annotationSet.getAnnotationTypes().contains(annotation2.getType()));
//		assertFalse(annotationSet.getAnnotationTypes().contains(annotation3.getType()));
	}

}
