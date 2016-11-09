package g419.liner2.api.normalizer.rbn;

import g419.liner2.api.normalizer.rbn.RuleSet;
import g419.liner2.api.normalizer.rbn.RuleSetLoader;
import junit.framework.TestCase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Including happens with paths. We cannot be sure that tests are executed in repository root (maybe we're running on
 * Jenkins or smth like that?), so we assume that we can write in curren working directory (cwd), copy all needed files
 * there, perform testing and clean up (remove copied files).
 *
 * Do not mix all.json and dates.json! all.json will be complete rule base, obtained by including all "real" rule sets,
 * while dates.json is only and example of including, used only in this test.
 */
public class IncludingTests extends TestCase{
    RuleSet ruleSet;

    private void copyToCWD(String resourceName) throws IOException {
        InputStream inStr = this.getClass().getClassLoader().getResourceAsStream(resourceName);
        Files.copy(inStr, Paths.get("./"+resourceName));
    }

    public void setUp() throws IOException {
        copyToCWD("dates.json");
        copyToCWD("dates_written.json");
        copyToCWD("dates_numbered.json");
        ruleSet = RuleSetLoader.getInstance().load(
            new File("./dates.json")
        );

    }

    public void tearDown() throws IOException {
        Files.delete(Paths.get("./dates.json"));
        Files.delete(Paths.get("./dates_written.json"));
        Files.delete(Paths.get("./dates_numbered.json"));
    }

    public void testNormalizing(){
        //written date
        assertEquals("2000-12", ruleSet.normalize("grudzień 2000"));
        //numbered date
        assertEquals("1912-05-12T15:20", ruleSet.normalize("1912-05-12 g. 15:20"));
    }
}
