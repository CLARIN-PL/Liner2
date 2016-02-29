package g419.corpus.structure;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class ParagraphTest {

	@Test
	public void testParagraph() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testAddSentence() {
		String id1 = "id1";

		Paragraph paragraph1 = new Paragraph(id1);
		Sentence sentence1 = new Sentence();
		paragraph1.addSentence(sentence1);

		assertTrue(paragraph1.getSentences().contains(sentence1));
	}

	@Test
	public void testGetAttributeIndex() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetId() {
		String id1 = "id1";

		Paragraph paragraph1 = new Paragraph(id1);

		assertEquals(id1, paragraph1.getId());
	}

	@Test
	public void testGetSentences() {
		String id1 = "id1";

		Paragraph paragraph1 = new Paragraph(id1);
		Sentence sentence1 = new Sentence();
		Sentence sentence2 = new Sentence();
		paragraph1.addSentence(sentence1);
		paragraph1.addSentence(sentence2);

		ArrayList<Sentence> sentencesFromParagraph = paragraph1.getSentences();

		assertTrue(sentencesFromParagraph.contains(sentence1));
		assertTrue(sentencesFromParagraph.contains(sentence2));
	}

	@Test
	public void testSetAttributeIndex() {
		TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetChunkMetaData() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetKeysChunkMetaData() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetChunkMetaData() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testClone() {
		String id1 = "id1";
		
		Paragraph paragraph1 = new Paragraph(id1);
		
		Paragraph paragraph2 = paragraph1.clone();
		
		// TODO
		//assertEquals(paragraph1,paragraph2);
	}

	@Test
	public void testNumSentences() {
		//fail("Not yet implemented"); // TODO
	}

}
