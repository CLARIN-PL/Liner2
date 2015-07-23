package g419.liner2.api.normalizer.rbn;

import g419.liner2.api.normalizer.rbn.RuleSet;
import g419.liner2.api.normalizer.rbn.RuleSetLoader;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

public class DatesNumberedRulesTests extends TestCase{
    RuleSet ruleSet;

    public void setUp() throws FileNotFoundException, URISyntaxException {
        ruleSet = RuleSetLoader.getInstance().load(
            new File(
                this.getClass().getClassLoader().getResource("dates_numbered.json").toURI()
            )
        );
    }

    public void testNormalizing(){
        //year4Digits
        assertEquals("2000", ruleSet.normalize("2000"));
        //year2Digits
        assertEquals("XX00", ruleSet.normalize("00"));
        assertEquals("XX00", ruleSet.normalize("'00"));
        //fullDate
        assertEquals("2012-05-12", ruleSet.normalize("2012-05-12"));
        //fullDateNoCentury
        assertEquals("XX12-05-12", ruleSet.normalize("'12-05-12"));
        //dateWithHourAndMinutes
        assertEquals("1912-05-12T15:20", ruleSet.normalize("1912-05-12 g. 15:20"));
        assertEquals("1912-05-12T15:20", ruleSet.normalize("1912-05-12 godz. 15:20"));
        //dateWithHourMinutesAndSeconds
        assertEquals("1912-05-12T15:20:40", ruleSet.normalize("1912-05-12 godz. 15:20:40"));
        //dateWithHourMinutesNoCentury
        assertEquals("XX12-05-12T15:20", ruleSet.normalize("'12-05-12 godz. 15:20"));
        //dateWithHourMinutesAndSecondsNoCentury
        assertEquals("XX12-05-12T15:20:40", ruleSet.normalize("'12-05-12 godz. 15:20:40"));

    }
}
