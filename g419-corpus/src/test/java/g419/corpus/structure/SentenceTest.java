package g419.corpus.structure;

import org.json.JSONObject;
import org.junit.Test;

import static g419.corpus.structure.TestDataProvider.getTokenAttributeIndex;
import static org.junit.Assert.*;

public class SentenceTest {


	@Test
	public void shouldHaveOrthAttribute(){

		TokenAttributeIndex index = getTokenAttributeIndex();

		Sentence sentence = TestDataProvider.sentence_Ala_ma_kota(index);

		JSONObject test = sentence.toJson();

		assertEquals(4, test.getJSONArray("tok").length());
		assertTrue(test.has("id"));
		System.out.println(test);
	}

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

	
}
