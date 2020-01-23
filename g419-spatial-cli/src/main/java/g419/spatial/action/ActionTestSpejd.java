package g419.spatial.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.schema.kpwr.KpwrSpatial;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.tools.NkjpSyntacticChunks;
import g419.spatial.tools.SpatialResources;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;

public class ActionTestSpejd extends Action {

  private String inputFilename = null;
  private String inputFormat = null;
  private final Pattern annotationsNg = Pattern.compile("^(NG|NumG).*$");
  private final Pattern annotationsPrep = Pattern.compile("^PrepN.*$");
  private final Set<String> regions = SpatialResources.getRegions();

  public ActionTestSpejd() {
    super("test-spejd");
    setDescription("wypisuje frazy NG zawierające TR lub LM");
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
    Document document = null;

    while ((document = reader.nextDocument()) != null) {
      for (final Sentence sentence : document.getSentences()) {
        NkjpSyntacticChunks.splitPrepNg(sentence);
        moveHeadForRegions(sentence);
        /* Zaindeksuj pierwsze tokeny anotacji NG* */
        final Map<Integer, List<Annotation>> chunkNgTokens = new HashMap<>();
        for (final Annotation an : sentence.getAnnotations(annotationsNg)) {
          for (Integer n = an.getBegin(); n <= an.getEnd(); n++) {
            if (!chunkNgTokens.containsKey(n)) {
              chunkNgTokens.put(n, new LinkedList<>());
            }
            chunkNgTokens.get(n).add(an);
          }
        }
        for (final Annotation an : sentence.getChunks()) {
          if (an.getType().equals(KpwrSpatial.SPATIAL_ANNOTATION_SPATIAL_OBJECT)) {
            final List<Annotation> ngs = chunkNgTokens.get(an.getBegin());
            final String object = an.toString();
            String head = "nonhead";
            String ng = "nong";
            String ngstr = "";
            if (ngs != null) {
              ng = "inng";
              if (ngs.get(0).getHead() == an.getBegin()) {
                head = "ishead";
              }
              ngstr = ngs.toString();
            }
            System.out.println(
                String.format("%s\t%s\t%s\t%s\t%s", document.getName(), object, ng, head, ngstr));
          }
        }
      }
    }
  }

  /**
   * Dla anotacji, który głową jest region, zmień głowę na element podrzędny
   *
   * @param sentence
   */
  public void moveHeadForRegions(final Sentence sentence) {
    /* Zaindeksuj tokeny anotacji NG* */
    final Map<Integer, List<Annotation>> mapTokenIdToAnnotations = new HashMap<>();
    for (final Annotation an : sentence.getAnnotations(annotationsNg)) {
      for (int i = an.getBegin(); i <= an.getEnd(); i++) {
        if (!mapTokenIdToAnnotations.containsKey(i)) {
          mapTokenIdToAnnotations.put(i, new LinkedList<>());
        }
        mapTokenIdToAnnotations.get(i).add(an);
      }
    }

    for (final Annotation an : sentence.getAnnotations(annotationsNg)) {
      int i = an.getBegin();
      // Ustaw i na pierwszy token nie będący regionem
      while (i <= an.getEnd() && regions.contains(sentence.getTokens().get(i).getDisambTag().getBase())) {
        System.err.println(sentence.getTokens().get(i).getDisambTag().getBase());
        i++;
      }
      if (i > an.getBegin() && i <= an.getEnd()) {
        final List<Annotation> inners = mapTokenIdToAnnotations.get(i);
        boolean foundNewHead = false;
        if (inners != null) {
          for (final Annotation inner : inners) {
            if (inner.getBegin() == i) {
              an.setHead(inner.getHead());
              foundNewHead = true;
              break;
            }
          }
        }
        if (!foundNewHead) {
          for (int j = i; j <= an.getEnd() && !foundNewHead; j++) {
            final String pos = sentence.getTokens().get(j).getDisambTag().getPos();
            if ("subst".equals(pos) || "ign".equals(pos)) {
              an.setHead(j);
              System.err.println("=> " + sentence.getTokens().get(j).getOrth());
              foundNewHead = true;
            }
          }
        }
      }
    }
  }
}
