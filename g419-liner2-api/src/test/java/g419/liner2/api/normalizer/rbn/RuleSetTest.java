package g419.liner2.api.normalizer.rbn;

import g419.liner2.api.normalizer.rbn.RuleSet;
import g419.liner2.api.normalizer.rbn.RuleSetLoader;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class RuleSetTest extends TestCase {

    RuleSet ruleSet;
    Map<String, String> fixtures;

    public void setUp() throws FileNotFoundException, URISyntaxException {
        ruleSet = RuleSetLoader.getInstance().load(
            new File(
                this.getClass().getClassLoader().getResource("example.json").toURI()
            )
        );
        fixtures = new HashMap<String, String>();
        fixtures.put("2010", "2010");
        fixtures.put("90", "XX90");
    }

    public void testNormalize() throws Exception {
        for (Map.Entry<String,String> entry: fixtures.entrySet())
            assertEquals(entry.getValue(), ruleSet.normalize(entry.getKey()));
    }
}