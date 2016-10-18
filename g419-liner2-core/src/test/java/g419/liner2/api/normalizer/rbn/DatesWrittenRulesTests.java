package g419.liner2.api.normalizer.rbn;

import g419.liner2.api.normalizer.rbn.RuleSet;
import g419.liner2.api.normalizer.rbn.RuleSetLoader;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

public class DatesWrittenRulesTests extends TestCase{
    RuleSet ruleSet;

    public void setUp() throws FileNotFoundException, URISyntaxException {
        ruleSet = RuleSetLoader.getInstance().load(
            new File(
                this.getClass().getClassLoader().getResource("dates_written.json").toURI()
            )
        );
    }

    public void testNormalizing(){
        //writtenYearMonth
        assertEquals("2000-12", ruleSet.normalize("grudzień 2000"));
        assertEquals("1990-01", ruleSet.normalize("styczen 1990"));
        //writtenYearMonthNoCentury
        assertEquals("XX85-08", ruleSet.normalize("sierpień '85"));
        assertEquals("XX60-04", ruleSet.normalize("kwiecien '60"));
        //writtenFullDate
        assertEquals("2008-08-12", ruleSet.normalize("dwunasty sierpnia 2008"));
        assertEquals("2008-09-12", ruleSet.normalize("dwunasty września 2008"));
        //writtenDateNoCentury
        assertEquals("XX08-08-12", ruleSet.normalize("dwunasty sierpnia 08"));
        assertEquals("XX08-09-12", ruleSet.normalize("dwunasty września '08"));
        //writtenFullDateWithHoursAndMinutes
        assertEquals("2008-08-12T14:05", ruleSet.normalize("dwunasty sierpnia 2008 14:05"));
        assertEquals("2008-08-12T14:05", ruleSet.normalize("dwunasty sierpnia 2008 g. 14:05"));
        assertEquals("2008-08-12T14:05", ruleSet.normalize("dwunasty sierpnia 2008 godz. 14:05"));
        //writtenFullDateWithHoursMinutesAndSeconds
        assertEquals("2008-08-12T14:05:15", ruleSet.normalize("dwunasty sierpnia 2008 godz. 14:05:15"));
    }
}
