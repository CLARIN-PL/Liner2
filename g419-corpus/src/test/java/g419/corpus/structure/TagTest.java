package g419.corpus.structure;

import static org.junit.Assert.*;
import org.json.JSONObject;
import org.junit.Test;

public class TagTest {

	@Test
	public void shouldReturnJson(){
		Tag tag = TestDataProvider.tag_kot();

		JSONObject test = tag.toJson();

		assertEquals(test.get("base"), tag.base);
		assertEquals(test.get("ctag"), tag.ctag);
		assertEquals(test.get("disamb"), tag.disamb ? 1 : 0);

		System.out.println(test);
	}


	@Test
	public void shouldReturnJsonDisambFalse(){
		Tag tag = TestDataProvider.tag_with_disamb_false();

		JSONObject test = tag.toJson();

		assertEquals(test.get("base"), tag.base);
		assertEquals(test.get("ctag"), tag.ctag);
		assertFalse(test.has("disamb"));

		System.out.println(test);
	}

	@Test
	public void shouldReturnJsonWhenNullsProvided(){
		Tag tag1 = TestDataProvider.tag_with_null_base();
		Tag tag2 = TestDataProvider.tag_with_null_ctag();

		JSONObject nullBase = tag1.toJson();
		JSONObject nullCtag = tag2.toJson();

        assertFalse(nullBase.has("base"));
		assertEquals(nullBase.get("ctag"), tag1.ctag);
		assertEquals(nullBase.get("disamb"), tag1.disamb ? 1 : 0);

		assertEquals(nullCtag.get("base"), tag2.base);
        assertFalse(nullCtag.has("ctag"));
		assertEquals(nullCtag.get("disamb"), tag2.disamb ? 1 : 0);

		System.out.println(nullBase);
        System.out.println(nullCtag);
	}

	@Test
	public void testTag() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testTagStringStringBoolean() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetBase() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetCtag() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetDisamb() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetDisamb() {
		//fail("Not yet implemented"); // TODO
	}

}
