package g419.liner2.api.normalizer;

import g419.liner2.api.normalizer.timex.TimexUtils;
import g419.liner2.api.normalizer.timex.entities.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeneralDateEntityTest {

    protected LValEntity asUnparsed(String u){
        return new LValEntity(new DateTimeEntity(null, null), u);
    }

    protected LValEntity asEntity(String y, String m, String d, String u){
        return new LValEntity(new DateTimeEntity(new GeneralDateEntity(y, m, d), null), u);
    }

    @Test
    public void testParse() throws Exception {
        assertEquals(asEntity("1234", "12", "13", "abc"), TimexUtils.parse("1234-12-13abc"));
        assertEquals(asEntity("1234", "12", null, "abc"), TimexUtils.parse("1234-12abc"));
        assertEquals(asEntity("1234", null, null, "abc"), TimexUtils.parse("1234abc"));
        assertEquals(asUnparsed("abc"), TimexUtils.parse("abc"));
    }

    @Test
    public void testToDateString() throws Exception {
        assertEquals(asEntity("1234", "12", "13", "abc").toTimex(), "1234-12-13abc");
        assertEquals(asEntity("1234", "12", null, "abc").toTimex(), "1234-12abc");
        assertEquals(asEntity("1234", null, null, "abc").toTimex(), "1234abc");
        assertEquals(asUnparsed("abc").toTimex(), "abc");
    }

    @Test
    public void testOverwrite() throws Exception {
        assertEquals(GeneralDateEntity.overwrite("abcxde", "defg", 'x'), "abcgde");
        assertEquals(GeneralDateEntity.overwrite("abcxde", "def", 'x'), "abcxde");
    }

    @Test
    public void testFill() throws Exception {
        assertEquals(TimexUtils.fill("xx34", "1950"), "1934");
        assertEquals(TimexUtils.fill("xx34-10", "1950"), "1934-10");
        assertEquals(TimexUtils.fill("xx34-xx", "1950"), "1934-xx");
        assertEquals(TimexUtils.fill("xx34-xx", "1950-12"), "1934-12");
        assertEquals(TimexUtils.fill("1834-xx", "1950-12"), "1834-12");
        assertEquals(TimexUtils.fill("1834-xx-10", "1834-11-12"), "1834-11-10");
        assertEquals(TimexUtils.fill("1834-xx-xx", "1834-11-12"), "1834-11-12");
        assertEquals(TimexUtils.fill("1834-10-xx", "1834-11-12"), "1834-10-12");
        assertEquals(TimexUtils.fill("1834-11-xx", "1834-11-12"), "1834-11-12");
        //these are not all cases, though these should be most frequent
    }
}