package g419.corpus.structure;

import org.json.JSONObject;
import org.junit.Test;

import static g419.corpus.structure.TestDataProvider.getTokenAttributeIndex;
import static org.junit.Assert.*;

public class TokenTest {

	@Test
	public void shouldHaveOrthAttribute(){

        TokenAttributeIndex index = getTokenAttributeIndex();

        Token token = TestDataProvider.token_kota(index);

		JSONObject test = token.toJson(null);

		assertEquals(test.get("orth"), token.getOrth());

		//System.out.println(test);
	}



    @Test
    public void shouldHaveLexTokens(){

        TokenAttributeIndex index = getTokenAttributeIndex();

        Token token = TestDataProvider.token_Ala_dubble(index);

        JSONObject test = token.toJson(null);

        assertEquals(test.get("orth"), token.getOrth());
        assertTrue(test.has("lex"));
        assertEquals(test.getJSONArray("lex").length(), 2);

       // System.out.println(test);
    }


    @Test
    public void shouldHaveProps(){

        TokenAttributeIndex index = getTokenAttributeIndex();

        Token token = TestDataProvider.token_token_with_props(index);

        JSONObject test = token.toJson(null);

        assertEquals(test.get("orth"), token.getOrth());
        assertTrue(test.has("prop"));
        assertEquals(4, test.getJSONArray("prop").length());

     //   System.out.println(test);
    }

	@Test
	public void testTokenTokenAttributeIndex() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testTokenStringTagTokenAttributeIndex() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testClearAttributes() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testRemoveAttribute() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetAttributeValue() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetNumAttributes() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetId() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetOrth() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetNoSpaceAfter() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testAddTag() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetTags() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testPackAtributes() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetAttributeValue() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetNoSpaceAfter() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetId() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetAttributeIndex() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testClone() {
		//fail("Not yet implemented"); // TODO
	}

}
