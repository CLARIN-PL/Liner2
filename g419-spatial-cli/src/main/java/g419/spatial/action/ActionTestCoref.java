package g419.spatial.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.schema.kpwr.KpwrSpatial;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.DocumentAnnotationIndexTypePos;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import java.util.List;
import org.apache.commons.cli.CommandLine;

public class ActionTestCoref extends Action {

  private String inputFilename = null;
  private String inputFormat = null;

  public ActionTestCoref() {
    super("test-coref");
    setDescription("wypisuje grupy grupy koreferencyjne dla SO nie będących rzeczownikiem");
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getInputFileFormatOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
  }

  @Override
  public void run() throws Exception {
    final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFilename, inputFormat);
    reader.forEachRemaining(this::process);
  }

  private void process(final Document document) {
    final DocumentAnnotationIndexTypePos index = new DocumentAnnotationIndexTypePos(document);
    document.getAnnotationClusters().getClusters().forEach(c -> {
      final int countSO = c.getAnnotations().stream()
          .map(an -> index.getAnnotationsOfTypeAtHeadPos(an, KpwrSpatial.SPATIAL_ANNOTATION_SPATIAL_OBJECT))
          .mapToInt(ans -> ans.size())
          .sum();
      if (countSO > 0) {
        System.out.println(document.getName());
        for (final Annotation mention : c.getAnnotations()) {
          final List<Annotation> sos = index.getAnnotationsOfTypeAtHeadPos(mention, KpwrSpatial.SPATIAL_ANNOTATION_SPATIAL_OBJECT);
          System.out.println(String.format(
              "  [%s][%d] %s -> %s -- %s",
              sos.size() > 0 && !mention.getHeadToken().getDisambTag().getPos().equals("subst") ? "*" : " ",
              sos.size(),
              mention.getText(),
              mention.getHeadToken().getOrth(),
              mention.getSentence().toString()));
        }
        System.out.println("");
      }
    });
  }
}
