package g419.serel.converter;

import com.google.common.collect.Lists;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.corpus.structure.RelationDesc;
import g419.corpus.structure.Sentence;
import g419.liner2.core.tools.parser.ParseTree;
import g419.liner2.core.tools.parser.ParseTreeGenerator;
import g419.liner2.core.tools.parser.SentenceLink;
import g419.serel.structure.SerelExpression;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Converts set of annotations and relations between annotations into a set of serel expressions.
 */
@Slf4j
public class DocumentToSerelExpressionConverter {

  ParseTreeGenerator parseTreeGenerator;
  PrintWriter reportWriter;

  public DocumentToSerelExpressionConverter(final ParseTreeGenerator ptg, final PrintWriter report) {
    parseTreeGenerator = ptg;
    reportWriter = report;
  }

  public List<SerelExpression> convert(final Document document) {

    if (document.getRelationsSet().size() == 0) {
      //log.debug("Relset is empty");
      return Lists.newArrayList();
    }

    final List<SerelExpression> result = new LinkedList<>();
    for (final Relation rel : document.getRelations("Semantic relations")) {
      try {
        //log.debug("Rel = " + rel);
        final SerelExpression serel = extractSerelFromRel(rel);
        //log.debug("Serel =" + serel);

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
    return result;
  }

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


  public SerelExpression extractSerelFromRel(final Relation rel) throws Exception {
    final Sentence sentenceFrom = rel.getAnnotationFrom().getSentence();
    final Sentence sentenceTo = rel.getAnnotationTo().getSentence();

    if (!(sentenceFrom.toString().equals(sentenceTo.toString()))) {
      System.out.println(" Relation " + rel + " skipped because refers to more then one sentence");
      return null;
    }

    final ParseTree parseTree = parseTreeGenerator.generate(sentenceFrom);
    final SerelExpression serel = this.extractSerelFromParseTree(rel, parseTree);
    return serel;
  }

  public SerelExpression extractSerelFromRelDesc(final RelationDesc relDesc) throws Exception {
    final Sentence sentence = relDesc.getSentence();
    final ParseTree parseTree = parseTreeGenerator.generate(sentence);
    final SerelExpression result = this.extractSerelFromParseTreeWithRelDesc(relDesc, parseTree);
    return result;
  }


  public SerelExpression extractSerelFromParseTree(final Relation rel, final ParseTree parseTree) {
    final int index1 = rel.getAnnotationFrom().getHead();
    final int index2 = rel.getAnnotationTo().getHead();

    final Pair<List<SentenceLink>, List<SentenceLink>> path = parseTree.getPathBetween(index1, index2);

    log.trace("LS1 = " + path.getLeft());
    log.trace("LS2 = " + path.getRight());

    final SerelExpression se;
    if (path != null) {
      se = new SerelExpression(RelationDesc.from(rel), path.getLeft(), path.getRight(), parseTree);
    } else {
      se = new SerelExpression(RelationDesc.from(rel), null, null, parseTree);
    }

    return se;

  }

  public SerelExpression extractSerelFromParseTreeWithRelDesc(final RelationDesc relDesc, final ParseTree parseTree) {
    final int index1 = relDesc.getFromTokenIndex();
    final int index2 = relDesc.getToTokenIndex();

    final Pair<List<SentenceLink>, List<SentenceLink>> path = parseTree.getPathBetween(index1 - 1, index2 - 1);


    //System.out.println("LS1 = "+path.getLeft());
    //System.out.println("LS2 = "+path.getRight());

    final SerelExpression se;
    if (path != null) {
      se = new SerelExpression(relDesc, path.getLeft(), path.getRight(), parseTree);
    } else {
      se = new SerelExpression(relDesc, null, null, parseTree);
    }

    return se;
  }


  private void reportSerel(final SerelExpression se) {

    //reportWriter.println(se.getRelationDesc().getDocument().getName());
    //reportWriter.println(se.getSentence());
    //reportWriter.println(se.getPathAsString(true));
    reportWriter.println(se.getPathAsString());
    //se.getParseTree().printAsTreeWithIndex(reportWriter);
    //reportWriter.println("------------------------------------------------------");


  }


}
