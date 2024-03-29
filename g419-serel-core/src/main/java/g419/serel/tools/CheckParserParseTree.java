package g419.serel.tools;

import g419.corpus.structure.Annotation;
import g419.liner2.core.tools.parser.SentenceLink;
import g419.serel.structure.ParseTreeMalfunction;
import g419.serel.structure.SerelExpression;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CheckParserParseTree {

  public  List<ParseTreeMalfunction> checkParseTree(final List<SerelExpression> serelExpressions  ) {


    List<ParseTreeMalfunction> result = new LinkedList<>();
    /*
    for(SerelExpression se : serelExpressions ) {

      Annotation aFrom = se.getRelationDesc().getAnnotationFrom();
      result.addAll(isAnnotationHeadPointingOut(se, aFrom));
      result.addAll(isHavingMoreElementsPointingOut(se, aFrom));

      Annotation aTo = se.getRelationDesc().getAnnotationTo();
      result.addAll(isAnnotationHeadPointingOut(se, aTo));
      result.addAll(isHavingMoreElementsPointingOut(se, aFrom));
    }
    */
    return result;
  }

  /*
  Widać, że nie zawsze głowa jest elementem "wychodzącym" z nazwy, np.
  "location: nam_org_organization -> wielki -> finał -> z -> problem <- w <- Wrocław <- nam_loc_gpe_city" :
  bierzemy node i sprawdzamy czy link z "głowy" wychodzi ma zewnątrz
  */
  private List<ParseTreeMalfunction> isAnnotationHeadPointingOut(SerelExpression se, Annotation a) {

    List<ParseTreeMalfunction> result = new LinkedList<>();

    int headForOut = a.getHead();
    Optional<SentenceLink> linkForOut = se.getParseTree().getLinksBySourceIndex(headForOut);
    if(linkForOut.isPresent()) {
      int targetOutIndex = linkForOut.get().getTargetIndex();
      if (a.isTokenIndexWithin(targetOutIndex)) {

        ParseTreeMalfunction ptm = ParseTreeMalfunction.builder()
            .malfunctionCode(ParseTreeMalfunction.MalfunctionCode.AHPI)
            .documentPath(se.getSentence().getDocument().getName())
            .annotationId(a.getId())
            .sourceIndex(headForOut)
            .targetIndex(targetOutIndex)
            .annStartRange(a.getBegin())
            .annEndRange((a.getEnd()))
            .build();
        result.add(ptm);
      }
    }
    return result;
  }


  /*
   2. sprawdzenie czy są przypadki, gdzie w relacji była nazwa wielowyrazowa
   i oba elementy nazwy wskazywały na element poza nazwą :
   bierzemy node, jeśli nazwa jest wielowyrazowa to sprawdzamy czy nie jest czasem
   tak, że więcej niż jeden wyraz wskazuje na zewnątrz "poza" nazwą
  */
  private List<ParseTreeMalfunction> isHavingMoreElementsPointingOut(SerelExpression se, Annotation ann) {
    List<ParseTreeMalfunction> result = new LinkedList<>();

    List<SentenceLink> links = ann.getTokens()
        .stream()
        .map(index -> se.getParseTree().getLinksBySourceIndex(index))
        .filter(optLink -> optLink.isPresent())
        .filter(optLink -> ! ann.isTokenIndexWithin(optLink.get().getTargetIndex()))
        .map(optLink -> optLink.get())
        .collect(Collectors.toList());

    if (links.size() > 1) {
      for (SentenceLink maltSentenceLink : links) {
        ParseTreeMalfunction ptm = ParseTreeMalfunction.builder()
            .malfunctionCode(ParseTreeMalfunction.MalfunctionCode.MEPO)
            .documentPath(se.getSentence().getDocument().getName())
            .annotationId(ann.getId())
            .sourceIndex(maltSentenceLink.getSourceIndex())
            .targetIndex(maltSentenceLink.getTargetIndex())
            .annStartRange(ann.getBegin())
            .annEndRange((ann.getEnd()))
            .build();

        result.add(ptm);
      }
    }
    return result;
  }





}
