package g419.serel.converter;

import com.google.common.collect.Lists;
import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.corpus.structure.Sentence;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.MaltSentenceLink;
import g419.serel.structure.SerelExpression;
import org.apache.commons.lang3.tuple.Pair;
import org.maltparser.core.exception.MaltChainedException;
import java.util.LinkedList;
import java.util.List;

/**
 * Converts set of annotations and relations between annotations into a set of serel expressions.
 */
public class DocumentToSerelExpressionConverter {

  MaltParser malt;

  public DocumentToSerelExpressionConverter(MaltParser maltParser) {
    malt = maltParser;
  }

  public List<SerelExpression> convert(final Document document) {

    if (document.getRelationsSet().size() == 0) {
      return Lists.newArrayList();
    }

    List<SerelExpression> result = new LinkedList<>();

    for (Relation rel : document.getRelations("Semantic relations")) {
      try {
        SerelExpression serel = extractSerelFromRel(rel);
        result.add(serel);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  public SerelExpression extractSerelFromRel(Relation rel) throws MaltChainedException {
    Sentence sentence = rel.getAnnotationFrom().getSentence();
    final MaltSentence maltSentence = new MaltSentence(sentence, MappingNkjpToConllPos.get());
    malt.parse(maltSentence);
    SerelExpression serel = this.extractSerelFromMaltSentence(rel,maltSentence);
    return serel;
  }

  public SerelExpression extractSerelFromMaltSentence(Relation rel, MaltSentence maltSentence) {
    int index1 = rel.getAnnotationFrom().getHead();
    int index2 = rel.getAnnotationTo().getHead();

    Pair<List<MaltSentenceLink>, List<MaltSentenceLink>> path = maltSentence.getPathBetween( index1, index2);

    SerelExpression se;
    if(path!=null) {
      se = new SerelExpression(rel,path.getLeft(), path.getRight());
    } else {
      se = new SerelExpression(rel,null, null);
    }

    return se;
  }




}
