package g419.spatial.action;

import com.google.common.base.Charsets;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.Liner2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class ActionAnalyzeObjects extends Action {

  private final static String OPTION_FILENAME_LONG = "filename";
  private final static String OPTION_FILENAME = "f";

  private final static String OPTION_MODEL_LINER2 = "m";
  private final static String OPTION_MODEL_LINER2_ARG = "path";
  private final static String OPTION_MODEL_LINER2_LONG = "liner2-model";
  private final static String OPTION_MODEL_LINER2_DESC = "Path to a Liner2 top9 model configuration";

  private List<Pattern> annotationsPrep = new LinkedList<>();
  private List<Pattern> annotationsNg = new LinkedList<>();

  private String filename = null;
  private String inputFormat = null;
  private String liner2Model = null;

  public ActionAnalyzeObjects() throws IOException {
    super("analyze-objects");
    this.setDescription("Prints spatial object mentions with information if they are part on a NE or NG. NG annotations must be in the input file.");
    this.setExample(IOUtils.toString(this.getClass().getResource("ActionAnalyzeObjects.example.txt"), Charsets.UTF_8));

    this.options.addOption(this.getOptionInputFilename());
    this.options.addOption(CommonOptions.getInputFileFormatOption());
    this.options.addOption(Option.builder(OPTION_MODEL_LINER2).longOpt(OPTION_MODEL_LINER2_LONG)
        .hasArg().argName(OPTION_MODEL_LINER2_ARG).desc(OPTION_MODEL_LINER2_DESC).required().build());

    this.annotationsPrep.add(Pattern.compile("^PrepNG.*"));
    this.annotationsNg.add(Pattern.compile("^NG.*"));
  }

  /**
   * Create Option object for input file name.
   *
   * @return Object for input file name parameter.
   */
  private Option getOptionInputFilename() {
    return Option.builder(ActionAnalyzeObjects.OPTION_FILENAME).longOpt(ActionAnalyzeObjects.OPTION_FILENAME_LONG)
        .hasArg().argName("filename").required().desc("path to the input file").build();
  }

  /**
   * Parse action options
   *
   * @param args The array with command line parameters
   */
  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    this.filename = line.getOptionValue(ActionAnalyzeObjects.OPTION_FILENAME);
    this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    this.liner2Model = line.getOptionValue(OPTION_MODEL_LINER2);
  }

  @Override
  public void run() throws Exception {
    AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.filename, this.inputFormat);

    Liner2 liner2 = new Liner2(this.liner2Model);

    Document document = null;
    while ((document = reader.nextDocument()) != null) {
      Logger.getLogger(this.getClass()).info("\nDocument: " + document.getName());

      liner2.chunkInPlace(document);

      for (Sentence sentence : document.getSentences()) {

        Map<String, Annotation> indexNG = this.makeAnnotationIndex(sentence.getAnnotations(Pattern.compile("NG")));
        Map<String, Annotation> indexNE = this.makeAnnotationIndex(sentence.getAnnotations(Pattern.compile("^nam_")));

        for (Annotation an : sentence.getAnnotations(Pattern.compile("(spatial_object)", Pattern.CASE_INSENSITIVE))) {
          String key = "" + sentence.hashCode() + "#" + an.getBegin();
          Annotation ne = indexNE.get(key);
          Annotation ng = indexNG.get(key);
          String str = "";
          if (ne != null) {
            str += " " + ne.getType();
            if (ne.getHead() == an.getBegin()) {
              str += " IS_HEAD";
            } else {
              str += " NO_HEAD";
            }
            str += " # " + ne.toString();
          } else if (ng != null) {
            str += " " + ng.getType();
            if (ng.getHead() == an.getBegin()) {
              str += " IS_HEAD";
            } else {
              str += " NO_HEAD";
            }
            str += " # " + ng.toString();
          } else {
            str += " NOT_FOUND";
          }
          System.out.println(String.format("%20s %10s %s", an.getText(), sentence.getTokens().get(an.getBegin()).getDisambTag().getPos(), str));
        }
      }

    }

    reader.close();
  }

  /**
   * @param anns
   * @return
   */
  public Map<String, Annotation> makeAnnotationIndex(Collection<Annotation> anns) {
    Map<String, Annotation> annotationIndex = new HashMap<String, Annotation>();
    for (Annotation an : anns) {
      for (int i = an.getBegin(); i <= an.getEnd(); i++) {
        String hash = "" + an.getSentence().hashCode() + "#" + i;
        if (annotationIndex.get(hash) != null) {
          if (annotationIndex.get(hash).getTokens().size() < an.getTokens().size()) {
            annotationIndex.remove(hash);
          }
        } else {
          annotationIndex.put(hash, an);
        }
      }
    }
    return annotationIndex;
  }

}
