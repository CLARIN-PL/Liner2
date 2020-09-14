package g419.serel.converter;

import com.google.common.collect.Lists;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.corpus.structure.Sentence;
import g419.liner2.core.tools.parser.SentenceLink;
import g419.liner2.core.tools.parser.*;
import g419.serel.structure.SerelExpression;
import org.apache.commons.lang3.tuple.Pair;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Converts set of annotations and relations between annotations into a set of serel expressions.
 */
public class DocumentToSerelExpressionConverter {

  ParseTreeGenerator parseTreeGenerator;
  PrintWriter report;

  public DocumentToSerelExpressionConverter(ParseTreeGenerator ptg, PrintWriter reportFile) {
    parseTreeGenerator = ptg;
    report = reportFile;
  }

  public List<SerelExpression> convert(final Document document) {

    System.out.println("convert::DocumentToSerelExpressionConverter");

    if (document.getRelationsSet().size() == 0) {
      return Lists.newArrayList();
    }

    List<SerelExpression> result = new LinkedList<>();

    for (Relation rel : document.getRelations("Semantic relations")) {
      try {
        SerelExpression serel = extractSerelFromRel(rel);
        if(report!=null) {
          this.reportSerel(serel);
        }
        result.add(serel);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  public SerelExpression extractSerelFromRel(Relation rel) throws Exception {
    Sentence sentence = rel.getAnnotationFrom().getSentence();
    ParseTree parseTree = parseTreeGenerator.generate(sentence);
    SerelExpression serel = this.extractSerelFromMaltSentence(rel,parseTree);
    return serel;
  }

  public SerelExpression extractSerelFromMaltSentence(Relation rel, ParseTree parseTree) {
    int index1 = rel.getAnnotationFrom().getHead();
    int index2 = rel.getAnnotationTo().getHead();

    Pair<List<SentenceLink>, List<SentenceLink>> path = parseTree.getPathBetween( index1, index2);

    SerelExpression se;
    if(path!=null) {
      se = new SerelExpression(rel,path.getLeft(), path.getRight(),parseTree);
    } else {
      se = new SerelExpression(rel,null, null, parseTree);
    }

    return se;
  }

  private void reportSerel(SerelExpression se) {
    report.println(se.getRelation().getDocument().getName());
    report.println(se.getSentence());
    report.println(se.getPathAsString());
    //TODO se.getMaltSentence().printAsTree(report);
    report.println("------------------------------------------------------");

  }



}
