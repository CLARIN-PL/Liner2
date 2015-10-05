package g419.liner2.cli.action;

import g419.liner2.cli.Main;
import junit.framework.TestCase;
import org.junit.Ignore;

import java.io.*;
import java.net.URISyntaxException;

@Ignore
public class ActionNormalizerEvalTest extends TestCase{
    File index;
    File config;
    PrintStream stdOut;


    public void setUp() throws URISyntaxException, FileNotFoundException {
        config = ResourcesUtils.resourceFile("cfg.ini");
    }

    protected void setStdOut(File out) throws FileNotFoundException {
        stdOut = System.out;
        System.setOut(new PrintStream(new FileOutputStream(out, false)));
    }

    protected void restoreStdOut(){
        System.out.close();
        System.setOut(stdOut);
    }


    protected String[] getArgs(File index, File misses){
        return misses==null ? new String[]{
                "normalizer-eval",
                "-f",
                index.getAbsolutePath(),
                "-m",
                config.getAbsolutePath(),
                "-i",
                "batch:ccl",
                "-v",
                "--metaKeys",
                "lval"
        } : new String[]{
                "normalizer-eval",
                "-f",
                index.getAbsolutePath(),
                "-m",
                config.getAbsolutePath(),
                "-i",
                "batch:ccl",
                "-v",
                "--misses",
                misses.getAbsolutePath(),
                "--metaKeys",
                "lval"
        };
    }



    protected void doTest(String dataset) throws Exception {
        File index = ResourcesUtils.index(dataset);
        if (!index.exists())
            throw new RuntimeException("Specified dataset doesn't exist! Put it in resources directory!");
        doTest(index, new File("./misses-"+dataset+".local.txt"), new File(dataset+".results.local.txt"));
    }

    protected void doTest(File index, File misses, File out) throws Exception {
        String[] args = getArgs(index, misses);
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
        System.out.println("testTimexAll");
        doTest("index_time");
    }

    public void testTimexTune() throws Exception {
        System.out.println("testTimexTune");
        doTest("index_time_tune");
    }

    public void testTimexTest() throws Exception {
        System.out.println("testTimexTest");
        doTest("index_time_test");
    }


}