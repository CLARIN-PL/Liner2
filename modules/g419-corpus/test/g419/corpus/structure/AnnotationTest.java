package g419.corpus.structure;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class AnnotationTest {

	@Test
	public void testHashCode() {
		Sentence sentence = new Sentence();

		Annotation annotation1 = new Annotation(0, "", sentence);
		Annotation annotation2 = new Annotation(0, "", sentence);

		Assert.assertEquals("Hashcode for the same object is diffrent.", annotation1.hashCode(), annotation1.hashCode());
		Assert.assertEquals("Hashcode for two equal objects is diffrent.", annotation1.hashCode(), annotation2.hashCode());
	}

	@Test
	public void testAnnotationIntIntStringSentence() {
		Sentence sentence = new Sentence();
		int begin = 0;
		int end = 1;
		String type = "";
		Annotation annotation = new Annotation(begin, end, type, sentence);

		Assert.assertNull("Constructor has not created an object", annotation);

		// TODO Check if all values passed to constructor have been set (for private use reflection)
	}

	@Test
	public void testAnnotationIntStringSentence() {
		Sentence sentence = new Sentence();
		int begin = 0;
		String type = "";
		Annotation annotation = new Annotation(begin, type, sentence);

		Assert.assertNull("Constructor has not created an object", annotation);

		// TODO Check if all values passed to constructor have been set (for private use reflection)
	}

	@Test
	public void testAnnotationIntStringIntSentence() {
		Sentence sentence = new Sentence();
		int begin = 0;
		String type = "";
		int channelIdx = 1;
		Annotation annotation = new Annotation(begin, type, channelIdx, sentence);

		Assert.assertNull("Constructor has not created an object", annotation);

		// TODO Check if all values passed to constructor have been set (for private use reflection)
	}

	@Test
	public void testSetChannelIdx() {
		Sentence sentence = new Sentence();
		int begin = 0;
		String type = "";
		int channelIdx = 1;
		Annotation annotation = new Annotation(begin, type, channelIdx, sentence);

		channelIdx = 2;
		annotation.setChannelIdx(channelIdx);
		Assert.assertEquals(annotation.getChannelIdx(), channelIdx);
	}

	@Test
	public void testGetChannelIdx() {
		Sentence sentence = new Sentence();
		int begin = 0;
		String type = "";
		int channelIdx = 1;
		Annotation annotation = new Annotation(begin, type, channelIdx, sentence);

		Assert.assertEquals(annotation.getChannelIdx(), channelIdx);
		channelIdx = 2;
		annotation.setChannelIdx(channelIdx);
		Assert.assertEquals(annotation.getChannelIdx(), channelIdx);
	}

	@Test
	public void testHasHead() {
		Sentence sentence = new Sentence();
		int begin = 0;
		String type = "";
		Annotation annotation = new Annotation(begin, type, sentence);
		
		Assert.assertFalse(annotation.hasHead());
		annotation.setHead(begin);
		Assert.assertTrue(annotation.hasHead());
	}

	@Test
	public void testGetHead() {
		Sentence sentence = new Sentence();
		int begin = 0;
		String type = "";
		Annotation annotation = new Annotation(begin, type, sentence);
		
		int head = 10;
		annotation.setHead(head);
		Assert.assertEquals("annotation.setHead did not caused annotation.getHead to return proper value.", head, annotation.getHead());
	}

	@Test
	public void testSetHead() {
		Sentence sentence = new Sentence();
		int begin = 0;
		String type = "";
		Annotation annotation = new Annotation(begin, type, sentence);
		
		annotation.setHead(begin);
		Assert.assertTrue("annotation.setHead did not caused annotation.hasHead to return true",annotation.hasHead());
		
		
	}

	@Test
	public void testAddToken() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testReplaceTokens() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testEqualsObject() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testGetId() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testGetBegin() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testGetEnd() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testGetTokens() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testGetSentence() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testGetType() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testGetText() {
		Sentence sentence1 = new Sentence();

		Annotation annotation1 = new Annotation(0, "", sentence1);
		Annotation annotation2 = new Annotation(0, "", sentence1);

		try {
			String text1 = annotation1.getText();
			String text2 = annotation2.getText();

			Assert.assertEquals("Text for the same object is diffrent", text1, text1);
			Assert.assertEquals("Text for the same object is diffrent", text2, text2);
			Assert.assertEquals("Text for two same object is diffrent", text1, text2);
		} catch (IndexOutOfBoundsException e) {
			fail("Annotation expect that sentence have at least one token");
		}
	}

	@Test
	public void testGetBaseText() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testSetId() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testSetType() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testSortChunks() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testClone() {
		// TODO Implement
		fail("Not yet implemented");
	}

	@Test
	public void testEquals() {
		Sentence sentence1 = new Sentence();
		Sentence sentence2 = new Sentence();

		Annotation annotation1 = new Annotation(0, "", sentence1);
		Annotation annotation2 = new Annotation(0, "", sentence1);
		Annotation annotation3 = new Annotation(0, "", sentence2);

		Assert.assertFalse("Annotation object is not equal to itself.", annotation1.equals(annotation1));
		Assert.assertFalse("Two same objects are not equal.", annotation1.equals(annotation2));
		Assert.assertTrue("Two differnt objects are equal.", annotation1.equals(annotation3));
	}

}
