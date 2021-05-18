package g419.serel.converter;

import com.google.common.collect.Lists;
import g419.corpus.structure.*;
import g419.liner2.core.tools.parser.ParseTree;
import g419.liner2.core.tools.parser.ParseTreeGenerator;
import g419.liner2.core.tools.parser.SentenceLink;
import g419.serel.structure.SentenceMiscValues;
import g419.serel.structure.SerelExpression;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Converts set of annotations and relations between annotations into a set of serel expressions.
 */
@Slf4j
public class DocumentToSerelExpressionConverter {

  ParseTreeGenerator parseTreeGenerator;
  PrintWriter reportWriter;

  public Map<String, Integer> typesCounter = new HashMap<>();

  public DocumentToSerelExpressionConverter(final ParseTreeGenerator ptg, final PrintWriter report) {
    parseTreeGenerator = ptg;
    reportWriter = report;
  }

  public List<SerelExpression> convertOld(final Document document) {

    if (document.getRelationsSet().size() == 0) {
      //log.debug("Relset is empty");
      return Lists.newArrayList();
    }

    final List<SerelExpression> result = new LinkedList<>();
    for (final Relation rel : document.getRelations("Semantic relations")) {

      final Sentence sentenceFrom = rel.getAnnotationFrom().getSentence();
      final Sentence sentenceTo = rel.getAnnotationTo().getSentence();

      if (!(sentenceFrom.toString().equals(sentenceTo.toString()))) {
        System.out.println(" Doc: " + document.getName() + " Relation " + rel + " skipped because refers to more then one sentence");
      }
    }
    return result;
  }


  public List<SerelExpression> convert(final Document document) {

    final List<SerelExpression> result = new LinkedList<>();

    for (int sentenceIndex = 0; sentenceIndex < document.getSentences().size(); sentenceIndex++) {
      final Sentence sentence = document.getSentences().get(sentenceIndex);
      final SentenceMiscValues smv = SentenceMiscValues.from(sentence, sentenceIndex);

      if (sentence.getNamRels().size() > 0) {
        // jeśli w ogóle są jakieś relacje z których mamy tworzyć reguły to dopiero wtedy zaczynamy naprawianie
        //fixing possible problems with dependent tokens linked not to head of NE

//        System.out.println("BEFORE correction");
//        sentence.printAsTree(new PrintWriter(System.out));
        // SWITCH_1
        sentence.checkAndFixBois();
//        System.out.println("AFTER correction");
//        sentence.printAsTree(new PrintWriter(System.out));
      }


      for (final RelationDesc relDesc : sentence.getNamRels()) {

        int typeCounter = typesCounter.computeIfAbsent(relDesc.getType(), k -> 0);
        typeCounter++;
        typesCounter.put(relDesc.getType(), typeCounter);

        if (relDesc.isNested()) {
          System.out.println("ERROR !!! Relacje zagnieżdzone nie są wspieranie. Doc =" + document.getName() + " rel=" + relDesc);
          continue;
        }

        try {
          final SerelExpression serel = extractSerelFromRelDesc(relDesc);

          if (serel == null) {
            continue;
          }
          result.add(serel);

        } catch (final Exception e) {
          e.printStackTrace();
        }
      }
    }

    return result;
  }

  public SerelExpression extractSerelFromRelDesc(final RelationDesc relDesc) throws Exception {
    if (relDesc.isMultiSentence()) {
      System.out.println("ERROR: Relation " + relDesc + " skipped because refers to more then one sentence");
      return null;
    }
    final SerelExpression serel = this.extractSerelFromParseTreeAndRelDesc(relDesc /*, parseTree*/);
    return serel;
  }

  public SerelExpression extractSerelFromParseTreeAndRelDesc(final RelationDesc relDesc /*, final ParseTree parseTree*/) {
    final Sentence sentence = relDesc.getSentence();

    final Token tokenFrom = sentence.getTokenById(relDesc.getFromTokenId());
    final int boiFromHeadId = sentence.findActualHeadId(tokenFrom, relDesc.getFromType());


    final Token tokenTo = sentence.getTokenById(relDesc.getToTokenId());
    final int boiToHeadId = sentence.findActualHeadId(tokenTo, relDesc.getToType());

    final Pair<List<Token>, List<Token>> path = sentence.getPathBetweenIds(boiFromHeadId, boiToHeadId);

    final SerelExpression se;
    if (path != null) {
      se = new SerelExpression(relDesc, path.getLeft(), path.getRight(), boiFromHeadId - 1, boiToHeadId - 1);
    } else {
      se = new SerelExpression(relDesc, null, null, boiFromHeadId - 1, boiToHeadId - 1);
    }
    return se;
  }

  private int findActualHeadIndex(final Annotation ann, final ParseTree parseTree) {
//    System.out.println("tokens =" + ann.getTokens());

    for (final int i : ann.getTokens()) {
      final SentenceLink sl = parseTree.getLinksBySourceIndex(i).get();
      if (!ann.getTokens().contains(sl.getTargetIndex())) {
        return i;
      }
    }
    return -1;
  }


}
