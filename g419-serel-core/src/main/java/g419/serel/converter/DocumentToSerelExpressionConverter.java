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
    return convert(document, "none");
  }

  public List<SerelExpression> convert(final Document document, final String caseMode) {

    final List<SerelExpression> result = new LinkedList<>();

    for (int sentenceIndex = 0; sentenceIndex < document.getSentences().size(); sentenceIndex++) {
      final Sentence sentence = document.getSentences().get(sentenceIndex);
      final SentenceMiscValues smv = SentenceMiscValues.from(sentence, sentenceIndex);

      if (sentence.getNamRels().size() > 0) {
        // jeśli w ogóle są jakieś relacje z których mamy tworzyć reguły to dopiero wtedy zaczynamy naprawianie
        //fixing possible problems with dependent tokens linked not to head of NE
//        System.out.println("BEFORE correction");
//        sentence.printAsTree();

        // SWITCH_1
        //sentence.checkAndFixBois();

//        System.out.println("AFTER correction");
//        sentence.printAsTree();
      }


      for (final RelationDesc relDesc : sentence.getNamRels()) {

        int typeCounter = typesCounter.computeIfAbsent(relDesc.getType(), k -> 0);
        typeCounter++;
        typesCounter.put(relDesc.getType(), typeCounter);


        if (relDesc.getFromTokenId() == relDesc.getToTokenId()) {
          // np. doc:101820, Tokyo Hobby Show
          System.out.println("ERROR !!! Relacje zagnieżdzone nie są wspieranie. Doc =" + document.getName() + " rel=" + relDesc);
          continue;
        }


        try {
//          System.out.println("Rel = " + relDesc);
          final SerelExpression serel = extractSerelFromRelDesc(relDesc);
//          System.out.println("Serel =" + serel);

          if (serel == null) {
            continue;
          }
          if (reportWriter != null) {
            this.reportSerel(serel);
          }
          result.add(serel);

        } catch (final Exception e) {
          e.printStackTrace();
        }
      }

    }


    return result;
  }

  /*
  public List<SerelExpression> convertAlreadyComboedFromRelDesc(final Set<RelationDesc> relationDescList) {

    final List<SerelExpression> result = new LinkedList<>();
    for (final RelationDesc relDesc : relationDescList) {
      try {
        log.trace("Rel = " + relDesc);
        final SerelExpression se = extractSerelFromRelDesc(relDesc);
        result.add(se);
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
    return result;

  }
*/

  public SerelExpression extractSerelFromRelDesc(final RelationDesc relDesc) throws Exception {
    if (relDesc.isMultiSentence()) {
      System.out.println("ERROR: Relation " + relDesc + " skipped because refers to more then one sentence");
      return null;
    }
    final SerelExpression serel = this.extractSerelFromParseTreeAndRelDesc(relDesc /*, parseTree*/);
    return serel;
  }

/*
  public SerelExpression extractSerelFromRel(final Relation rel) throws Exception {
    final Sentence sentenceFrom = rel.getAnnotationFrom().getSentence();
    final Sentence sentenceTo = rel.getAnnotationTo().getSentence();

    if (!(sentenceFrom.toString().equals(sentenceTo.toString()))) {
      System.out.println("ERROR: Relation " + rel + " skipped because refers to more then one sentence");
      return null;
    }

    final ParseTree parseTree = parseTreeGenerator.generate(sentenceFrom);

    final SerelExpression serel = this.extractSerelFromParseTree(rel, parseTree);
    return serel;
  }
*/
  /*
  public SerelExpression extractSerelFromRelDesc(final RelationDesc relDesc) throws Exception {
    final Sentence sentence = relDesc.getSentence();
    final ParseTree parseTree = parseTreeGenerator.generate(sentence);
    final SerelExpression result = this.extractSerelFromParseTreeWithRelDesc(relDesc, parseTree);
    return result;
  }

   */

  public SerelExpression extractSerelFromParseTreeAndRelDesc(final RelationDesc relDesc /*, final ParseTree parseTree*/) {
    final Sentence sentence = relDesc.getSentence();

    final Token tokenFrom = sentence.getTokenById(relDesc.getFromTokenId());
    final int boiFromHeadId = sentence.findActualHeadId(tokenFrom, relDesc.getFromType());

    final Token tokenTo = sentence.getTokenById(relDesc.getToTokenId());
    final int boiToHeadId = sentence.findActualHeadId(tokenTo, relDesc.getToType());

    final Pair<List<Token>, List<Token>> path = sentence.getPathBetweenIds(boiFromHeadId, boiToHeadId);

    final SerelExpression se;
    if (path != null) {
      se = new SerelExpression(relDesc, path.getLeft(), path.getRight() /* , parseTree*/, boiFromHeadId - 1, boiToHeadId - 1);
    } else {
      se = new SerelExpression(relDesc, null, null /* , parseTree*/, boiFromHeadId - 1, boiToHeadId - 1);
    }
    return se;
  }

/*
  public SerelExpression extractSerelFromParseTree(final Relation rel, final ParseTree parseTree) {
//    final int index1 = rel.getAnnotationFrom().getHead();
//    final int index2 = rel.getAnnotationTo().getHead();

//    final int index1 = rel.getAnnotationFrom().getHeadActual();
//    final int index2 = rel.getAnnotationTo().getHeadActual();
//    --- nie ma tych relacji wpisanych w tokenach na tym poziomie

    // truly - index not id
    final int index1 = findActualHeadIndex(rel.getAnnotationFrom(), parseTree);
    final int index2 = findActualHeadIndex(rel.getAnnotationTo(), parseTree);
//    System.out.println("Found head index = " + index1);
//    System.out.println("Found head index = " + index2);
//    System.out.println("\n");


    final Pair<List<SentenceLink>, List<SentenceLink>> path = parseTree.getPathBetween(index1, index2);

//    log.trace("LS1 = " + path.getLeft());
//    log.trace("LS2 = " + path.getRight());

    final SerelExpression se;
    if (path != null) {
      se = new SerelExpression(RelationDesc.from(rel), path.getLeft(), path.getRight(), parseTree, index1, index2);
    } else {
      se = new SerelExpression(RelationDesc.from(rel), null, null, parseTree, index1, index2);
    }

    return se;

  }
 */


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

/*
  public SerelExpression extractSerelFromParseTreeWithRelDesc(final RelationDesc relDesc, final ParseTree parseTree) {

    // TODO - czy to czsem nie jest ID - i dlatego jest potrzebne odejmowanie tek jedynki tu dalej ?
    final int index1 = relDesc.getFromTokenIndex();
    final int index2 = relDesc.getToTokenIndex();

    final Pair<List<SentenceLink>, List<SentenceLink>> path = parseTree.getPathBetween(index1 - 1, index2 - 1);


    System.out.println("LS1 = " + path.getLeft());
    System.out.println("LS2 = " + path.getRight());

    final SerelExpression se;
    if (path != null) {

      System.out.println("NN:LS1 = " + path.getLeft());
      System.out.println("NN:LS2 = " + path.getRight());


      se = new SerelExpression(relDesc, path.getLeft(), path.getRight(), parseTree);
    } else {

      System.out.println("NULL:LS1 = " + path.getLeft());
      System.out.println("NULL:LS2 = " + path.getRight());


      se = new SerelExpression(relDesc, null, null, parseTree);
    }

    return se;
  }

 */


  private void reportSerel(final SerelExpression se) {

    //reportWriter.println(se.getRelationDesc().getDocument().getName());
    //reportWriter.println(se.getSentence());
    //reportWriter.println(se.getPathAsString(true));

    // WHOLE REPORT
    //reportWriter.println(se.getPathAsString());

    //JUST PATTERNS
    reportWriter.println(se.getJustPattern());

    // GENERATE TREE
    //se.getParseTree().printAsTreeWithIndex(reportWriter);

    //reportWriter.println("------------------------------------------------------");


  }


}
