package g419.serel.converter;

import com.google.common.collect.Lists;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.corpus.structure.RelationDesc;
import g419.corpus.structure.Sentence;
import g419.liner2.core.tools.parser.SentenceLink;
import g419.liner2.core.tools.parser.*;
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

  public DocumentToSerelExpressionConverter(ParseTreeGenerator ptg, PrintWriter report) {
    parseTreeGenerator = ptg;
    reportWriter = report;
  }

  public List<SerelExpression> convert(final Document document) {

    if (document.getRelationsSet().size() == 0) {
      return Lists.newArrayList();
    }

    List<SerelExpression> result = new LinkedList<>();
    for (Relation rel : document.getRelations("Semantic relations")) {
      try {
        log.trace("Rel = "+rel);
        SerelExpression serel = extractSerelFromRel(rel);
        if(reportWriter !=null) {
          this.reportSerel(serel);
        }
        result.add(serel);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  public List<SerelExpression> convertAlreadyComboedFromRelDesc(final Set<RelationDesc> relationDescList) {

    List<SerelExpression> result = new LinkedList<>();
    for (RelationDesc relDesc : relationDescList) {
      try {
        log.trace("Rel = "+relDesc);
        SerelExpression se  = extractSerelFromRelDesc(relDesc);
        result.add(se);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return result;

  }




  public SerelExpression extractSerelFromRel(Relation rel) throws Exception {
    Sentence sentence = rel.getAnnotationFrom().getSentence();
    ParseTree parseTree = parseTreeGenerator.generate(sentence);
    SerelExpression serel = this.extractSerelFromParseTree(rel,parseTree);
    return serel;
  }

  public SerelExpression extractSerelFromRelDesc(RelationDesc relDesc) throws Exception {
    Sentence sentence = relDesc.getSentence();
    ParseTree parseTree = parseTreeGenerator.generate(sentence);
    SerelExpression result = this.extractSerelFromParseTreeWithRelDesc(relDesc,parseTree);
    return result;
  }









  public SerelExpression extractSerelFromParseTree(Relation rel, ParseTree parseTree) {
    int index1 = rel.getAnnotationFrom().getHead();
    int index2 = rel.getAnnotationTo().getHead();

    Pair<List<SentenceLink>, List<SentenceLink>> path = parseTree.getPathBetween( index1, index2);

    log.trace("LS1 = "+path.getLeft());
    log.trace("LS2 = "+path.getRight());

    SerelExpression se;
    if(path!=null) {
      se = new SerelExpression(RelationDesc.from(rel),path.getLeft(), path.getRight(),parseTree);
    } else {
      se = new SerelExpression(RelationDesc.from(rel),null, null, parseTree);
    }

    return se;

  }

  public SerelExpression extractSerelFromParseTreeWithRelDesc(RelationDesc relDesc, ParseTree parseTree) {
    int index1 = relDesc.getFromTokenIndex();
    int index2 = relDesc.getToTokenIndex();

    Pair<List<SentenceLink>, List<SentenceLink>> path = parseTree.getPathBetween( index1-1, index2-1);


    System.out.println("LS1 = "+path.getLeft());
    System.out.println("LS2 = "+path.getRight());

    SerelExpression se;
    if(path!=null) {
      se = new SerelExpression(relDesc,path.getLeft(), path.getRight(),parseTree);
    } else {
      se = new SerelExpression(relDesc,null, null, parseTree);
    }

    return se;
  }






  private void reportSerel(SerelExpression se) {
    /*
    //reportWriter.println(se.getRelationDesc().getDocument().getName());
    reportWriter.println(se.getSentence());
    //reportWriter.println(se.getPathAsString(true));
    reportWriter.println(se.getPathAsString());
    se.getParseTree().printAsTreeWithIndex(reportWriter);
    reportWriter.println("------------------------------------------------------");
    */

  }



}
