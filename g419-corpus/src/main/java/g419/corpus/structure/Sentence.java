package g419.corpus.structure;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Reprezentuje zdanie jako sekwencję tokenów i zbiór anotacji.
 *
 * @author czuk
 */
public class Sentence extends IdentifiableElement {

  /* Indeks nazw atrybutów */
  TokenAttributeIndex attributeIndex = null;

  /* Sekwencja tokenów wchodzących w skład zdania */
  List<Token> tokens = new ArrayList<>();

  /* Zbiór anotacji */
  LinkedHashSet<Annotation> chunks = new LinkedHashSet<>();

  /* Tymczasowe obejście braku odniesienia do dokumentu z poziomu klasy Annotation */
  Document document;

  /* Paragraf w którym jest zdanie*/
  Paragraph paragraph;

  /* Indeks zdania w pliku */
  public int sentenceNumber;

  private static final Comparator<Annotation> annotationComparator = new Comparator<Annotation>() {
    @Override
    public int compare(final Annotation a, final Annotation b) {
      if (a.getTokens().size() == b.getTokens().size()) {
        return String.CASE_INSENSITIVE_ORDER.compare(a.getType(), b.getType());
      }

      return Integer.signum(b.getTokens().size() - a.getTokens().size());
    }
  };

  public Sentence() {
  }

  public Sentence(final TokenAttributeIndex attrIndex) {
    attributeIndex = attrIndex;
  }

  public Sentence withId(final String id) {
    setId(id);
    return this;
  }

  public void addChunk(final Annotation chunk) {
    chunk.setSentence(this);
    chunks.add(chunk);
  }

  public Annotation createAnnotation(final Integer tokenIndex, final String type) {
    final Annotation an = new Annotation(tokenIndex, type, this);
    chunks.add(an);
    return an;
  }

  public void addAnnotations(final AnnotationSet chunking) {
    if (chunking != null) {
      for (final Annotation chunk : chunking.chunkSet()) {
        addChunk(chunk);
      }
    }
  }

  public void addToken(final Token token) {
    tokens.add(token);
  }

  /**
   * Zwraca pozycję zdania w dokumencie.
   *
   * @return
   */
  public int getOrd() {
    if (document != null) {
      return document.getSentences().indexOf(this);
    } else {
      return -1;
    }
  }

  /**
   * Return true if the sentence has an assigned identifier.
   *
   * @return True if the sentence identifier is set.
   */
  public boolean hasId() {
    return id != null;
  }

  /**
   * Return a list of annotations which contain a token with given index.
   */
  public List<Annotation> getChunksAt(final int idx) {
    final List<Annotation> returning = new ArrayList<>();
    final Iterator<Annotation> i_chunk = chunks.iterator();
    while (i_chunk.hasNext()) {
      final Annotation currentChunk = i_chunk.next();
      if (currentChunk.getTokens().contains(idx)) {
        returning.add(currentChunk);
      }
    }
    return returning;
  }

  /**
   * Return a list of annotations which contain a token with given index.
   */
  public List<Annotation> getChunksAt(final int idx, final List<Pattern> types) {
    final List<Annotation> returning = new ArrayList<>();
    final Iterator<Annotation> i_chunk = chunks.iterator();
    while (i_chunk.hasNext()) {
      final Annotation currentChunk = i_chunk.next();
      if (currentChunk.getTokens().contains(idx)) {
        if (types != null) {
          for (final Pattern patt : types) {
            if (patt.matcher(currentChunk.getType()).matches()) {
              returning.add(currentChunk);
              break;
            }
          }
        } else {
          returning.add(currentChunk);
        }
      }
    }
    return returning;
  }

  /*
  Sprawdza, czy token o podanym indeksie jest chunkiem typu 'type'
   */
  public boolean isChunkAt(final int idx, final String type) {
    final Iterator<Annotation> i_chunk = chunks.iterator();
    while (i_chunk.hasNext()) {
      final Annotation currentChunk = i_chunk.next();
      if (currentChunk.getTokens().contains(idx) && currentChunk.getType().equals(type)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param idx
   * @param types
   * @param sorted
   * @return
   */
  public List<Annotation> getChunksAt(final int idx, final List<Pattern> types, final boolean sorted) {
    final List<Annotation> result = getChunksAt(idx, types);
    if (sorted) {
      sortTokenAnnotations(result);
    }
    return result;
  }

  /**
   * @param tokenIdx
   * @return
   */
  public String getTokenClassLabel(final int tokenIdx) {
    final List<Annotation> tokenAnnotations = getChunksAt(tokenIdx);

    if (tokenAnnotations.isEmpty()) {
      return "O";
    } else {
      final List<String> classLabels = new ArrayList<>();
      sortTokenAnnotations(tokenAnnotations);
      for (final Annotation ann : tokenAnnotations) {
        String classLabel = "";
        if (ann.getBegin() == tokenIdx) {
          classLabel += "B-";
        } else {
          classLabel += "I-";
        }
        classLabel += ann.getType();
        classLabels.add(classLabel);
      }
      return StringUtils.join(classLabels, "#");
    }
  }


  public String getTokenClassLabel(final int tokenIdx, final List<Pattern> types) {
    final List<Annotation> tokenAnnotations = getChunksAt(tokenIdx, types, true);

    if (tokenAnnotations.isEmpty()) {
      return "O";
    } else {
      final ArrayList<String> classLabels = new ArrayList<>();
      sortTokenAnnotations(tokenAnnotations);
      for (final Annotation ann : tokenAnnotations) {
        String classLabel = "";
        if (ann.getBegin() == tokenIdx) {
          classLabel += "B-";
        } else {
          classLabel += "I-";
        }
        classLabel += ann.getType();
        classLabels.add(classLabel);

      }
      return StringUtils.join(classLabels, "#");
    }

  }

  private void sortTokenAnnotations(final List<Annotation> tokenAnnotations) {
    Collections.sort(tokenAnnotations, annotationComparator);
  }

  /**
   * Return a set of annotations with a type matching the pattern `type`.
   *
   * @param type Pattern of annotation type.
   * @return Set of annotations.
   */
  public LinkedHashSet<Annotation> getAnnotations(final Pattern type) {
    final LinkedHashSet<Annotation> annotationsForTypes = new LinkedHashSet<>();
    for (final Annotation annotation : chunks) {
      if (type.matcher(annotation.getType()).find()) {
        annotationsForTypes.add(annotation);
      }
    }

    return annotationsForTypes;
  }

  public LinkedHashSet<Annotation> getAnnotations(final String type) {
    final LinkedHashSet<Annotation> annotationsForTypes = new LinkedHashSet<>();
    for (final Annotation annotation : chunks) {
      if (type.equals(annotation.getType())) {
        annotationsForTypes.add(annotation);
      }
    }

    return annotationsForTypes;
  }

  public LinkedHashSet<Annotation> getAnnotations(final List<Pattern> types) {
    final LinkedHashSet<Annotation> annotationsForTypes = new LinkedHashSet<>();
    for (final Annotation annotation : chunks) {
      for (final Pattern type : types) {
        if (type.matcher(annotation.getType()).find()) {
          annotationsForTypes.add(annotation);
        }
      }
    }

    return annotationsForTypes;
  }

  /**
   * Return a set of all annotations assigned to the sentence.
   *
   * @return Set of all annotations.
   */
  public LinkedHashSet<Annotation> getChunks() {
    return chunks;
  }

  public int getAttributeIndexLength() {
    return attributeIndex.getLength();
  }

  public TokenAttributeIndex getAttributeIndex() {
    return attributeIndex;
  }

  /**
   * Zwraca ilość tokenów.
   */
  public int getTokenNumber() {
    return tokens.size();
  }

  public List<Token> getTokens() {
    return tokens;
  }

  public Token getTokenById(final int id) {
    return getTokens().get(id - 1);
  }

  public Token getTokenByIndex(final int index) {
    return getTokens().get(index);
  }

  public void setAttributeIndex(final TokenAttributeIndex attributeIndex) {
    this.attributeIndex = attributeIndex;
    for (final Token t : tokens) {
      t.setAttributeIndex(attributeIndex);
    }
  }

  public void setAnnotations(final AnnotationSet chunking) {
    chunks = chunking.chunkSet();
  }

  public String annotationsToString() {
    final StringBuilder output = new StringBuilder();
    for (final Annotation chunk : chunks) {
      output.append(chunk.getType() + " | " + chunk.getText() + "\n");
    }
    return output.toString();
  }

  public void removeAnnotations(final String annotation) {
    final Set<Annotation> toRemove = new HashSet<>();
    for (final Annotation an : chunks) {
      if (an.getType().equals(annotation)) {
        toRemove.add(an);
      }
    }
    chunks.removeAll(toRemove);
  }

  public Annotation getAnnotationInChannel(final String channelName, final int annotationIdx) {
    for (final Annotation annotation : chunks) {
      if (annotation.getType().equalsIgnoreCase(channelName) && annotation.getChannelIdx() == annotationIdx) {
        return annotation;
      }
    }

    return null;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (final Token t : tokens) {
      sb.append(t.getOrth());
      sb.append(t.getNoSpaceAfter() ? "" : " ");
    }
    return sb.toString().trim();
  }


  public String toStringDecorated(final LinkedHashSet<Integer> indexes) {
    return toStringDecorated(indexes, 0);
  }


  public String toStringDecorated(final LinkedHashSet<Integer> indexes, final int correction) {

    final StringBuilder sb = new StringBuilder();
    for (final Token t : tokens) {
      if (indexes.contains(t.getNumberId() - correction)) {
        sb.append(">");
      }
      sb.append(t.getOrth());
      if (indexes.contains(t.getNumberId() - correction)) {
        sb.append("<");
      }

      sb.append(t.getNoSpaceAfter() ? "" : " ");
    }
    return sb.toString().trim();
  }


  public String toBaseString() {
    final StringBuilder sb = new StringBuilder();
    for (final Token t : tokens) {
      sb.append(t.getAttributeValue("base"));
      sb.append(t.getNoSpaceAfter() ? "" : " ");
    }
    return sb.toString().trim();
  }

  public void setTokens(final List<Token> newTokens) {
    tokens = newTokens;
  }

  @Override
  public Sentence clone() {
    final Sentence copy = new Sentence();
    copy.attributeIndex = attributeIndex.clone();
    copy.setId(getId());
    for (final Token t : tokens) {
      final Token newT = t.clone();
      newT.attrIdx = copy.attributeIndex;
      copy.addToken(newT);
    }
    for (final Annotation a : chunks) {
      copy.addChunk(a.clone());
    }
    return copy;
  }

  public void setDocument(final Document document) {
    this.document = document;
  }

  public Document getDocument() {
    return document;
  }

  public void setParagraph(final Paragraph p) {
    paragraph = p;
  }

  public Paragraph getParagraph() {
    return paragraph;
  }


  // searching for parent token of ...

  public Token getParentTokenFromToken(final Token token) {
    final int parentTokenId = token.getParentTokenId();
    if (parentTokenId == 0) {
      return null;
    }
    return tokens.get(parentTokenId - 1);
  }

  public Token getRootToken() {
    return getTokens().stream().filter(t -> t.getParentTokenId() == 0).findFirst().get();
  }

  public Token getPreviousToken(final Token token) {
    final int previousTokenId = token.getNumberId() - 1;
    if (previousTokenId == 0) {
      return null;
    }
    return tokens.get(previousTokenId - 1);
  }

  public Token getFollowingToken(final Token token) {
    final int followingTokenId = token.getNumberId() + 1;
    if (followingTokenId > tokens.size()) { // tokens are indexed from 0 and ids from 1 so > and not >=
      return null;
    }
    return tokens.get(followingTokenId - 1); // tokens are indexed from 0 and ids from 1
  }


  public Token getParentTokenFromTokenIndex(final int tokenIndex) {
    final Token token = tokens.get(tokenIndex);
    return getParentTokenFromToken(token);
  }

  public Token getParentTokenFromTokenId(final int tokenId) {
    return getParentTokenFromTokenIndex(tokenId - 1);
  }

  // searching for child tokens of ...
  public List<Token> getChildrenTokensFromToken(final Token token) {
    return tokens.stream().filter(t -> t.getParentTokenId() == token.getNumberId()).collect(Collectors.toList());
  }

  public List<Token> getChildrenTokensFromTokenIndex(final int tokenIndex) {
    final Token token = tokens.get(tokenIndex);
    return getChildrenTokensFromToken(token);
  }

  public List<Token> getChildrenTokensFromTokenId(final int tokenId) {
    return getChildrenTokensFromTokenIndex(tokenId - 1);
  }

  public List<Token> selectTokensWithBoiInsideTag(final List<Token> tokensToCheck, final String name) {
    return tokensToCheck.stream().filter(t -> t.hasBoiInsideTag(name)).collect(Collectors.toList());
  }

  public List<RelationDesc> getNamRels() {
    return getTokens().stream().flatMap(t -> t.getNamRels().stream()).collect(Collectors.toList());
  }


  //////////////////////////////////////////////////
  // START: Constructing whole BOI tokens ids sequence ....
  //////////////////////////////////////////////////

  public void checkAndFixBois() {
    for (final Token token : this.getTokens()) {
//      System.out.println("Checking token " + token);
      final List<String> boisBeginsRaw = token.getBoisBeginsRaw();
      for (final String boi : boisBeginsRaw) {
        checkAndFixBoi(token, boi);
      }
    }
  }

  public void checkAndFixBoi(final Token token, final String boiRaw) {
    final Set<Integer> boiIds = getBoiTokensIdsForTokenAndName(token, boiRaw);
//    System.out.println("BoisIds = " + boiIds);
    final int headId = fixBoiHead(boiIds);
//    System.out.println("ActualHEadId =" + headId);
    final Set<Integer> boiIdsAsSet = new HashSet<>(boiIds);

    // fix all out-of BOI tokens - if pointing to NE but not to head change them to point to determined head
    for (final Token t : this.getTokens()) {
      if (boiIdsAsSet.contains(t.getNumberId())) { continue; }
      if (t.getParentTokenId() == headId) { continue; }
      if (boiIdsAsSet.contains(t.getParentTokenId())) {
        //System.out.println("ERROR !!! Token linked not to head of BOI. TokenID = " + t.getNumberId() + " Doc= " + this.getDocument().getName() + " Boi= " + boiRaw + "  sent = " + this.toString());
        t.setAttributeValue("head", "" + headId);
      }
    }
  }


  public LinkedHashSet<Integer> getBoiTokensIdsForTokenAndName(final Token tokenToCheck, final String boiName) {
    final LinkedHashSet<Integer> resultIds = new LinkedHashSet<>();

    if (!tokenToCheck.hasBoi(boiName)) {
      return resultIds;
    }
    resultIds.add(tokenToCheck.getNumberId());

    fillBoiFrontIds(tokenToCheck, boiName, resultIds);
    fillBoiBackIds(tokenToCheck, boiName, resultIds);

    //TOREVERT
    //resultIds.sort(Comparator.naturalOrder());
//    System.out.println(" returning listIds = " + resultIds);
    return resultIds;
  }

  private void fillBoiFrontIds(final Token tokenToCheckFrom, final String boiName, final Set<Integer> resultIds) {
    if (!tokenToCheckFrom.hasBoi(boiName)) {
      return;
    }
    if (tokenToCheckFrom.hasBoiBeginTag(boiName)) {
      return;
    }

    Token currentToken = this.getPreviousToken(tokenToCheckFrom);
    while (currentToken != null) {
      if (currentToken.hasBoi(boiName)) {
        //TOREVERT
        //resultIds.add(0, currentToken.getNumberId());
        resultIds.add(currentToken.getNumberId());
      }
      if (currentToken.hasBoiBeginTag(boiName)) {
        break;
      }
      currentToken = this.getPreviousToken(currentToken);
    }

    if (currentToken == null) {
      System.out.println(" Error in boi tagging - no start for this one: " + boiName + " CTX:");
    }
  }

  private void fillBoiBackIds(final Token tokenToCheckFrom, final String boiName, final Set<Integer> resultIds) {
    if (!tokenToCheckFrom.hasBoi(boiName)) {
      return;
    }

    Token currentToken = this.getFollowingToken(tokenToCheckFrom);
    while (currentToken != null) {
      if (currentToken.hasBoiInsideTag(boiName)) {    // InsideTag !!!!
        //TOREVERT
        // resultIds.add(0, currentToken.getNumberId());
        resultIds.add(currentToken.getNumberId());
      } else {
        break;
      }
      currentToken = this.getFollowingToken(currentToken);
    }
  }

  //////////////////////////////////////////////////
  // FINISH: Constructing whole BOI tokens sequence ....
  //////////////////////////////////////////////////

  public int findActualHeadId(final Token token, final String name) {
//    System.out.println("findAHI name = " + name);
    final Set<Integer> boiTokensIds = this.getBoiTokensIdsForTokenAndName(token, name);
//    System.out.println("bois =" + list);
    for (final int id : boiTokensIds) {
      final Token t = getTokenById(id);
//    System.out.println("Checking HEADID token:" + t);
      if (!boiTokensIds.contains(t.getParentTokenId())) {
        // zakładamy, że już jest wszystko naprawione
        // i pierwszy który trafimy jest tym co trzeba
        return t.getNumberId();
      }
    }

    return token.getNumberId();
  }


  public int fixBoiHead(final Token token, final String name) {
    final Set<Integer> boiTokensIds = this.getBoiTokensIdsForTokenAndName(token, name);
    return fixBoiHead(boiTokensIds);
  }

  public int fixBoiHead(final Set<Integer> boiTokensIds) {

    final Set<Integer> possibleHeadIds = new LinkedHashSet<>();
    int determinedHeadId = -1;


    for (final int id : boiTokensIds) {
      final Token t = getTokenById(id);
//    System.out.println("Checking HEADID token:" + t);
      if (!boiTokensIds.contains(t.getParentTokenId())) {
        if (possibleHeadIds.size() > 0) {
          //System.out.println("WARN !!! Boi has more then one head: found  " + possibleHeadIds + " and new = " + id + " doc: " + this.getDocument().getName() + " sentId=" + this.toString());
        }
        possibleHeadIds.add(id);
      }
    }

    // jeśli jest tylko jeden token z linkiem na zewnątrz BOI to wszsytko jest OK
    // on jest tą głową
    if (possibleHeadIds.size() == 1) {
      return possibleHeadIds.iterator().next();
    }


    final Set<Integer> boiTokensIdsAsSet = new HashSet<>(boiTokensIds);

    // najpierw prosty przypadek :
    // jak na razie jeśli któryś "z podejrzanych" jest na samej górze _całego drzewa_ to on jest head
    for (final int id : possibleHeadIds) {
      final Token t = getTokenById(id);
      if (t.getParentTokenId() == 0) {
        determinedHeadId = id;
        break; // zakładamy, że jest tylko jeden taki który ma parentId = 0
      }
    }


    // jeśli dotąd nie można było rozstrzygnąć co jest głową to bierz pierwszy z możliwych
    if (determinedHeadId == -1) {
      //System.out.println("WARN !!! Could not really determimne Boi head: found posHeadIds  " + possibleHeadIds + " doc: " + this.getDocument().getName() + " sentId=" + this.toString());
      determinedHeadId = possibleHeadIds.iterator().next();
    }

    // "naprawiany" wszystkie pozostałe tokeny "być-może-head" by wskazywały na znaleziony head
    // te tokeny które są w ramach NE ok pozostawiamy niezmienione
    for (final int possibleHeadTokenId : possibleHeadIds) {
      final Token possibleHeadToken = getTokenById(possibleHeadTokenId);
      if (possibleHeadToken.getNumberId() != determinedHeadId) {
        possibleHeadToken.setAttributeValue("head", "" + determinedHeadId);
      }
    }

    return determinedHeadId;
  }


  public Pair<List<Token>, List<Token>> getPathBetweenIds(final int id1, final int id2) {
//    System.out.println(" getPathBetweenIds " + id1 + " - " + id2);
    final List<Token> parents1 = getParentsFromId(id1);
//    System.out.println("p1 =" + parents1);
    final List<Token> parents2 = getParentsFromId(id2);
//    System.out.println("p2 =" + parents2);

    final Pair<Integer, Integer> indexes = findIndexesToLowestCommonLink(parents1, parents2);
//    System.out.println(" Pair = " + indexes);

    if (indexes == null) {
      return null;
    }

    return Pair.of(
        parents1.subList(0, indexes.getLeft() + 1),
        parents2.subList(0, indexes.getRight() + 1)
    );


  }


  /**
   * @param list1 - ordered list of first element and all its parents ROOT
   * @param list2 - ordered list of second element and all its parents ROOT
   * @return - TOKENS in above lists to first element that is the same on both lists when travelling from element(s) to ROOT
   */
  public Pair<Integer, Integer> findIndexesToLowestCommonLink(final List<Token> list1,
                                                              final List<Token> list2) {
    for (int i = 0; i < list1.size(); i++) {
      for (int j = 0; j < list2.size(); j++) {
        if (list1.get(i).getNumberId() == list2.get(j).getNumberId()) {
          return Pair.of(i, j);
        }
      }
    }

    // it should never come here !
    return null;
  }


  public List<Token> getParentsFromId(int id) {
    final List<Token> list = new ArrayList<>();

    do {
      final Token t = getTokens().get(id - 1);
      list.add(t);
      id = t.getParentTokenId();
    } while (id != 0);

    return list;
  }


  //////////////////////////////////////////////////
  // START: Print as tree
  //////////////////////////////////////////////////

  public void printAsTree(final PrintWriter pw) {
    final Token rootToken = this.getRootToken();
    System.out.println("Root token =" + rootToken);
    printTokenSubTree(pw, rootToken, 0);
    pw.flush();
  }

  public void printTokenSubTree(final PrintWriter pw, final Token token, int level) {
    printlnToken(pw, token, level);

    final List<Token> children = this.getChildrenTokensFromToken(token);
    level++;
    for (final Token child : children) {
      printTokenSubTree(pw, child, level);
    }
  }

  public void printlnToken(final PrintWriter pw, final Token token, final int level) {
    final StringBuilder spaces = new StringBuilder();
    for (int i = 0; i < level; i++) { spaces.append("   "); }
    pw.println(spaces + "(" + token.getAttributeValue("deprel") + ") " + token.getAttributeValue(1) + "\tId=" + token.getNumberId() + " parId=" + token.getParentTokenId());
  }

  //////////////////////////////////////////////////
  // FINISH: Print as tree
  //////////////////////////////////////////////////


}