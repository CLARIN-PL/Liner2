package g419.liner2.api.normalizer.rbn;

import g419.liner2.api.normalizer.rbn.Rule;
import g419.liner2.api.normalizer.rbn.RuleSet;
import g419.liner2.api.normalizer.rbn.RuleSetLoader;
import junit.framework.TestCase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RuleSetLoaderTest extends TestCase {

    RuleSetLoader loader;
    RuleSet expected;

    public void setUp(){
        loader = RuleSetLoader.getInstance();
        Map<String, Rule> rules = new HashMap<String, Rule>();
        rules.put("year4Digits", new Rule("year4Digits", "(1\\d|2\\d)(\\d\\d)", "@group(0)@group(1)"));
        rules.put("year2Digits", new Rule("year2Digits", "(\\d\\d)", "XX@group(0)"));
        Map<String, String> normalization = new HashMap<String, String>();
        expected = new RuleSet(rules, normalization);
    }

    public void testLoad() throws Exception {
        RuleSet loaded = loader.load(
            new File(
                this.getClass().getClassLoader().getResource("example.json").toURI()
            )
        );
        assert expected.equals(loaded);
    }
}