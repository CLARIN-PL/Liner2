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
public class ActionPartsEvalTest extends TestCase{
    File index;
    File config;
    PrintStream stdOut;


    public void setUp() throws URISyntaxException, FileNotFoundException {
        config = ResourcesUtils.resourceFile("cfg_global_rule.ini");
    }

    protected void setStdOut(File out) throws FileNotFoundException {
        stdOut = System.out;
        System.setOut(new PrintStream(new FileOutputStream(out, false)));
    }

    protected void restoreStdOut(){
        System.out.close();
        System.setOut(stdOut);
    }


    protected String[] getArgs(File index){
        return new String[]{
                "constituents-eval",
                "-f",
                index.getAbsolutePath(),
                "-m",
                config.getAbsolutePath(),
                "-i",
                "batch:ccl",
                "-v",
                "--inKeys",
                "lval",
                "--outKeys",
                "val"
        };
    }



    protected void doTest(String dataset) throws Exception {
        File index = ResourcesUtils.index(dataset);
        if (!index.exists())
            throw new RuntimeException("Specified dataset doesn't exist! Put it in resources directory!");
        doTest(index, new File(dataset+".results.constituents.txt"));
    }

    protected void doTest(File index, File out) throws Exception {
        String[] args = getArgs(index);
        try {
            setStdOut(out);
            for (String a: args)
                System.out.print(a+" ");
            System.out.println();
            Main.main(args);
        } finally {
            restoreStdOut();
        }
    }

    public void testTimexTrain() throws Exception {
        doTest("index_time_train");
    }

    public void testTimexAll() throws Exception {
        doTest("index_time");
    }

    public void testTimexTune() throws Exception {
        doTest("index_time_tune");
    }

    public void testTimexTest() throws Exception {
        doTest("index_time_test");
    }


}