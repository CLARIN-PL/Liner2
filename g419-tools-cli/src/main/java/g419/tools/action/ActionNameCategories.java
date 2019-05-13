package g419.tools.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.toolbox.wordnet.NamToWordnet;
import g419.toolbox.wordnet.Wordnet3;
import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataRaw;

import java.util.*;
import java.util.regex.Pattern;

public class ActionNameCategories extends Action {

  private String inputFilename = null;
  private String inputFormat = null;
  private String wordnetPath = null;

  private Pattern patternNG = Pattern.compile(".*NG.*");
  private Pattern patternNam = Pattern.compile("^nam($|_.*)");

  private Wordnet3 wordnet = null;
  private NamToWordnet namToWordnet = null;

  public ActionNameCategories() {
    super("name-categories");
    this.setDescription("tworzy listę kategorii dla nazw w oparciu o określone konstrukcje językowe");
    this.options.addOption(CommonOptions.getInputFileFormatOption());
    this.options.addOption(CommonOptions.getInputFileNameOption());
    this.options.addOption(CommonOptions.getWordnetOption(true));
  }

  /**
   * Parse action options
   *
   * @param args The array with command line parameters
   */
  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    this.inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE_LONG);
    this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT_LONG);
    this.wordnetPath = line.getOptionValue(CommonOptions.OPTION_WORDNET_LONG);
  }

  @Override
  public void run() throws Exception {

    this.wordnet = new Wordnet3(this.wordnetPath);
    this.namToWordnet = new NamToWordnet(this.wordnet);

    Document document = null;
    AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.inputFilename, this.inputFormat);

    while ((document = reader.nextDocument()) != null) {
      Logger.getLogger(this.getClass()).info("Document: " + document.getName());
      for (Sentence sentence : document.getSentences()) {
        this.processSentence(sentence);
      }
    }

  }

  /**
   * @param sentence
   */
  private void processSentence(Sentence sentence) {
    Map<Integer, Annotation> indexNgs = new HashMap<Integer, Annotation>();
    for (Annotation an : sentence.getAnnotations(this.patternNG)) {
      for (int i = an.getBegin(); i <= an.getEnd(); i++) {
        Annotation indexNg = indexNgs.get(i);
        if (indexNg == null || indexNg.getTokens().size() < an.getTokens().size()) {
          indexNgs.put(i, an);
        }
      }
    }

    Set<Annotation> printed = new HashSet<Annotation>();
    for (Annotation an : sentence.getAnnotations(this.patternNam)) {
      Annotation ng = indexNgs.get(an.getBegin());
      if (ng != null && !printed.contains(ng) && !an.getTokens().contains(ng.getHead())
          && ng.getHeadToken().getDisambTag().getPos().equals("subst")
          && !an.getHeadToken().getDisambTag().getCase().equals("gen")
          && an.getHeadToken().getDisambTag().getCase().equals(ng.getHeadToken().getDisambTag().getCase())) {

        String namType = an.getType();
        String headBase = ng.getHeadToken().getDisambTag().getBase();
        boolean hypernym = false;
        Set<PrincetonDataRaw> hipernyms = this.namToWordnet.getSynsets(namType);

        for (PrincetonDataRaw synset : this.wordnet.getSynsets(headBase)) {
          if (!Collections.disjoint(this.wordnet.getAllSynsets(synset, Wordnet3.REL_HYPERNYM), hipernyms)) {
            hypernym = true;
            break;
          }
        }

        System.out.println(
            String.format("%s\t%s\t%s\t%s\t%s\t%s",
                an.getText(),
                namType,
                headBase,
                ng.toString(),
                an.getHeadToken().getDisambTag().getCtag(),
                hypernym ? "HYPERNYM-YES" : "HYPERNYM-NO"));
      }
    }
  }
}
