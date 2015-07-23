package g419.liner2.cli.action;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.MissingResourceException;

public class ResourcesUtils {
    /**
     * Will fail, if resource isn't in the plain filesystem, e.g. is in JAR or is fetched from URL.
     */
    static File resourceFile(String resource) throws URISyntaxException{
        ClassLoader classLoader = ResourcesUtils.class.getClassLoader();
        URL url = classLoader.getResource(resource);
        if (url==null)
            throw new MissingResourceException("Missing resource file", "", resource);
        return new File(url.toURI());
    }

    static File dataSetRoot;

    static {
        try {
            dataSetRoot = resourceFile("./2015-03-11-kpwr-timex-norm-global/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static File index(String name){
        return new File(dataSetRoot, name+".txt");
    }

//    static File indexAll = new File(dataSetRoot, "index_time.txt");
//    static File indexTest = new File(dataSetRoot, "index_time_test.txt");
//    static File indexTrain = new File(dataSetRoot, "index_time_train.txt");
//    static File indexTune = new File(dataSetRoot, "index_time_tune.txt");
//
//    static final Map<String, File> indexes = new HashMap<>();
//    static {
//        //todo: indexing will change!
//        indexes.put("all", indexAll);
//        indexes.put("test", indexTest);
//        indexes.put("train", indexTrain);
//        indexes.put("tune", indexTune);
//    }


}
