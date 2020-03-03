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
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class ActionAnalyzeObjects extends Action {

  private final static String OPTION_FILENAME_LONG = "filename";
  private final static String OPTION_FILENAME = "f";

  private final static String OPTION_MODEL_LINER2 = "m";
  private final static String OPTION_MODEL_LINER2_ARG = "path";
  private final static String OPTION_MODEL_LINER2_LONG = "liner2-model";
  private final static String OPTION_MODEL_LINER2_DESC = "Path to a Liner2 top9 model configuration";

  private final List<Pattern> annotationsPrep = new LinkedList<>();
  private final List<Pattern> annotationsNg = new LinkedList<>();

  private String filename = null;
  private String inputFormat = null;
  private String liner2Model = null;

  public ActionAnalyzeObjects() throws IOException {
    super("analyze-objects");
    setDescription("Prints spatial object mentions with information if they are part on a NE or NG. " +
        "NG annotations must be in the input file.");
    setExample(IOUtils.toString(
        getClass().getResource("ActionAnalyzeObjects.example.txt"), Charsets.UTF_8));

    options.addOption(getOptionInputFilename());
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(Option.builder(OPTION_MODEL_LINER2).longOpt(OPTION_MODEL_LINER2_LONG)
        .hasArg().argName(OPTION_MODEL_LINER2_ARG).desc(OPTION_MODEL_LINER2_DESC).required().build());

    annotationsPrep.add(Pattern.compile("^PrepNG.*"));
    annotationsNg.add(Pattern.compile("^NG.*"));
  }

  private Option getOptionInputFilename() {
    return Option.builder(ActionAnalyzeObjects.OPTION_FILENAME).longOpt(ActionAnalyzeObjects.OPTION_FILENAME_LONG)
        .hasArg().argName("filename").required().desc("path to the input file").build();
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    filename = line.getOptionValue(ActionAnalyzeObjects.OPTION_FILENAME);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    liner2Model = line.getOptionValue(OPTION_MODEL_LINER2);
  }

  @Override
  public void run() throws Exception {
    final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(filename, inputFormat);

    final Liner2 liner2 = new Liner2(liner2Model);

    Document document = null;
    while ((document = reader.nextDocument()) != null) {
      Logger.getLogger(getClass()).info("\nDocument: " + document.getName());

      liner2.chunkInPlace(document);

      for (final Sentence sentence : document.getSentences()) {

        final Map<String, Annotation> indexNG =
            makeAnnotationIndex(sentence.getAnnotations(Pattern.compile("NG")));
        final Map<String, Annotation> indexNE =
            makeAnnotationIndex(sentence.getAnnotations(Pattern.compile("^nam_")));

        for (final Annotation an : sentence.getAnnotations(
            Pattern.compile("(spatial_object)", Pattern.CASE_INSENSITIVE))) {
          final String key = "" + sentence.hashCode() + "#" + an.getBegin();
          final Annotation ne = indexNE.get(key);
          final Annotation ng = indexNG.get(key);
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
          System.out.println(String.format("%20s %10s %s", an.getText(),
              sentence.getTokens().get(an.getBegin()).getDisambTag().getPos(), str));
        }
      }

    }

    reader.close();
  }

  /**
   * @param anns
   * @return
   */
  public Map<String, Annotation> makeAnnotationIndex(final Collection<Annotation> anns) {
    final Map<String, Annotation> annotationIndex = new HashMap<>();
    for (final Annotation an : anns) {
      for (int i = an.getBegin(); i <= an.getEnd(); i++) {
        final String hash = "" + an.getSentence().hashCode() + "#" + i;
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
