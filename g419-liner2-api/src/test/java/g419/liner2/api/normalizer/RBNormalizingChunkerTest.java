package g419.liner2.api.normalizer;

import eu.clarin_pl.rbn.RuleSetLoader;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

public class RBNormalizingChunkerTest extends TestCase {

    String expected;
    NormalizingChunker normalizer;
    Annotation annotation;

    public void setUp() throws Exception {
        expected = "2005-03-21";
        normalizer = new RBNormalizingChunker(
                Arrays.asList(Pattern.compile("t3_date")),
                RuleSetLoader.getInstance().load(
                        new File(this.getClass().getClassLoader().getResource("rules.json").toURI())
                )
        );
        URL url = this.getClass().getClassLoader().getResource("sample.xml");
        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(url.toString(), url.openStream(), "ccl");
        Document document = reader.nextDocument();
        reader.close();
        Assert.assertTrue(document.getAnnotations().size() == 1);
        annotation = document.getAnnotations().get(0);
    }

    public void testNormalize() throws Exception {
        normalizer.normalize(annotation);
        Assert.assertEquals(expected, annotation.getMetadata("lval"));
    }
}