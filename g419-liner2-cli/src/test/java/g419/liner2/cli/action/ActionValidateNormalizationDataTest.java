package g419.liner2.cli.action;

import g419.liner2.cli.Main;
import junit.framework.TestCase;
import org.junit.Ignore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;

@Ignore
public class ActionValidateNormalizationDataTest extends TestCase{
    File index;
    File config;
    PrintStream stdOut;


    public void setUp() throws URISyntaxException, FileNotFoundException {
        index = ResourcesUtils.index("index_time");
        stdOut = System.out;
        System.setOut(new PrintStream(new FileOutputStream("./timex-validation-result.txt")));
    }

    public void tearDown(){
        System.out.close();
        System.setOut(stdOut);
    }

    public void testValidation() throws Exception {
        String[] args = new String[]{
                "normalizer-validate",
                "-f",
                index.getAbsolutePath(),
                "-i",
                "batch:ccl",
                "-t",
                "t3_date;t3_duration;t3_set;t3_range",
                "-m",
                "lemma"
        };
        for (String a: args)
            System.out.print(a+" ");
        System.out.println();
        Main.main(args);
    }
}