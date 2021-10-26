package g419.corpus.io.writer;

import g419.corpus.structure.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

//import java.io.BufferedOutputStream;

public class SciERCStreamWriter extends AbstractDocumentWriter {

  private static int counter = 0;

  private OutputStream os;

  public SciERCStreamWriter(OutputStream os) {
    this.os = os;
  }

  public SciERCStreamWriter() {
    this.os = new ByteArrayOutputStream();
  }

  public static String[] convertSentence(Sentence s) {
    int index = 0;
    String[] sentenceConll = new String[s.getTokenNumber()];
    TokenAttributeIndex ai = s.getAttributeIndex();
    for (Token t : s.getTokens()) {
      sentenceConll[index] = convertToken(t, ai, ++index);
    }
    return sentenceConll;
  }

  private StringBuffer createSentenceStr(Sentence s) throws IOException {

    SentenceMiscValues smv = SentenceMiscValues.from(s, 0);

    // does it have relations?
    System.out.println("Creating related");
    List<RelationDesc> related = s.getNamRels();
    StringBuffer relatedStr = generateSentenceStringFromRelations(related, s);

    return relatedStr;
  }


  private StringBuffer generateSentenceStringFromRelations(List<RelationDesc> relations, Sentence s) {

    StringBuffer sb = new StringBuffer();

    if (relations.size() > 0) {
      boolean wasWritten = false;

/*
  {"clusters": [[[35, 35], [69, 69]]],
    "sentences": [
        ["Graph", "unification", "remains", "the", "most", "expensive", "part", "of", "unification-based", "grammar", "parsing", "."],
        ["We", "focus", "on", "one", "speed-up", "element", "in", "the", "design", "of", "unification", "algorithms", ":", "avoidance", "of", "copying", "of", "unmodified", "subgraphs", "."],
        ["We", "propose", "a", "method", "of", "attaining", "such", "a", "design", "through", "a", "method", "of", "structure-sharing", "which", "avoids", "log", "-LRB-", "d", "-RRB-", "overheads", "often", "associated", "with", "structure-sharing", "of", "graphs", "without", "any", "use", "of", "costly", "dependency", "pointers", "."],
        ["The", "proposed", "scheme", "eliminates", "redundant", "copying", "while", "maintaining", "the", "quasi-destructive", "scheme", "'s", "ability", "to", "avoid", "over", "copying", "and", "early", "copying", "combined", "with", "its", "ability", "to", "handle", "cyclic", "structures", "without", "algorithmic", "additions", "."]
    ],
    "ner": [[[0, 1, "Task"], [8, 10, "Task"]], [[16, 17, "Method"], [22, 23, "Method"], [27, 30, "OtherScientificTerm"], [29, 30, "OtherScientificTerm"]], [[35, 35, "Generic"], [45, 45, "Method"], [48, 52, "OtherScientificTerm"], [56, 58, "OtherScientificTerm"], [64, 65, "Method"]], [[69, 69, "Generic"], [71, 72, "OtherScientificTerm"], [76, 79, "OtherScientificTerm"], [82, 83, "OtherScientificTerm"], [85, 86, "OtherScientificTerm"], [93, 94, "OtherScientificTerm"]]],
    "relations": [[[0, 1, 8, 10, "PART-OF"]], [[16, 17, 22, 23, "PART-OF"]], [[45, 45, 35, 35, "USED-FOR"]], [[69, 69, 93, 94, "USED-FOR"], [76, 79, 69, 69, "FEATURE-OF"], [82, 83, 85, 86, "CONJUNCTION"]]],
    "doc_key": "C92-2068"}
 */
      sb.append("{");
      sb.append("\"clusters\": [], \"sentences\": [ ");
      sb.append(generateSentenceForSciREC(s));
      sb.append(" ] ");

      sb.append(" , \"ner\": [" + generateNers(s) + " ] ");
      sb.append(" , \"relations\": [" + generateRelations(relations) + " ] ");


      String id = s.getDocument().getName()
          + "_" + System.currentTimeMillis()
          + "_" + counter;
      sb.append(" , \"doc_key\": \"" + id + "\" ");
      sb.append(" }\n");

    }

    System.out.println(" returning length:" + sb.toString().length() + "'");
    return sb;


  }

  private StringBuffer generateSentenceForSciREC(Sentence s) {
    StringBuffer sb = new StringBuffer();

    sb.append("[");
    sb.append(getTokensForSciECR(s));
    sb.append("]");

    return sb;
  }

  private StringBuffer generateNers(Sentence s) {
    StringBuffer sb = new StringBuffer();

    List<Boi> bois = s.getAllMainRawBois();

    List<Boi> filtered = bois.stream().filter(b -> !b.label.equals("O")).collect(Collectors.toList());

    filtered.stream().forEach(b -> b.endId =
        s.getMaxBoiTokenIdForTokenAndName(
            s.getTokens().get(b.startId - 1),
            b.label)
    );

    sb.append("[");
    sb.append(filtered.stream().map(b -> generateBoiForSciECR(b)).collect(Collectors.joining(", ")));
    sb.append("]");

    return sb;
  }

  private StringBuffer generateBoiForSciECR(Boi b) {
    StringBuffer sb = new StringBuffer();
    sb.append(" [ ");
    sb.append(b.startId - 1);
    sb.append(", " + (b.endId - 1));
    sb.append(", \"" + b.label + "\"");
    sb.append(" ] ");
    return sb;
  }


  private StringBuffer generateRelForSciECR(RelationDesc relDesc) {
    StringBuffer sb = new StringBuffer();
    sb.append(" [ ");

    sb.append((relDesc.getFromTokenId() - 1) + ", ");

    int subj_end = relDesc.getSentence()
        .getMaxBoiTokenIdForTokenAndName(
            relDesc.getSentence().getTokens().get(relDesc.getFromTokenId() - 1),
            relDesc.getFromType());
    sb.append((subj_end - 1) + ", ");


    sb.append((relDesc.getToTokenId() - 1) + ", ");
    int obj_end = relDesc.getSentence()
        .getMaxBoiTokenIdForTokenAndName(
            relDesc.getSentence().getTokens().get(relDesc.getToTokenId() - 1),
            relDesc.getToType());
    sb.append((obj_end - 1) + ", ");

    sb.append("\"" + relDesc.getType() + "\"");
    sb.append(" ] ");

    return sb;
  }

  private StringBuffer generateRelations(List<RelationDesc> rels) {
    StringBuffer sb = new StringBuffer();
    sb.append(" [ ");
    sb.append(rels.stream().map(rd -> generateRelForSciECR(rd)).collect(Collectors.joining(", ")));
    sb.append(" ] ");
    return sb;
  }


  private String getTokensForSciECR(Sentence s) {
    return s.getTokens().stream().map(t -> "\"" + escape(t.getAttributeValue(1)) + "\"").collect(Collectors.joining(", "));
  }

  private String escape(String s) {
    return s.replaceAll("\"", "\\\\\"");
  }

  private String getPosForTacred(Sentence s) {
    return s.getTokens().stream().map(t -> "\"" + t.getAttributeValue(3) + "\"").collect(Collectors.joining(", \n"));
  }

  private String getNersForTacred(Sentence s) {
    return s.getTokens().stream().map(t -> "\"" + t.getMainBoiRaw() + "\"").collect(Collectors.joining(", \n"));
  }

  private String getHeadsForTacred(Sentence s) {
    return s.getTokens().stream().map(t -> "\"" + t.getAttributeValue(6) + "\"").collect(Collectors.joining(", \n"));
  }

  private String getDepRelsForTacred(Sentence s) {
    return s.getTokens().stream().map(t -> "\"" + t.getAttributeValue(7) + "\"").collect(Collectors.joining(", \n"));
  }


  public static String convertToken(Token t, TokenAttributeIndex ai, int tokenIndex) {
    String orth = t.getOrth();
    String base = ai.getAttributeValue(t, "base");
    String posext = ai.getAttributeValue(t, "class");
    String pos = ai.getAttributeValue(t, "pos");
    String ctag = "";
    String cpos = null;
    String head = "_";
    if (ai.getIndex("head") != -1) {
      head = ai.getAttributeValue(t, "head");
    }
    String deprel = "_";
    if (ai.getIndex("deprel") != -1) {
      deprel = ai.getAttributeValue(t, "deprel");
    }

    /*
    String boiAnnotations = "_";
    if(ai.getIndex("boi")!=-1) {
      boiAnnotations = ai.getAttributeValue(t, "boi");
    }
    */
    /*
    String relations = "_";
    if(ai.getIndex("nam_rel")!=-1) {
      relations = ai.getAttributeValue(t, "nam_rel");
    }
    */
    String misc = "_";
    if (ai.getIndex("misc") != -1) {
      misc = ai.getAttributeValue(t, "misc");
    }


    String lemma = "_";
    if (ai.getIndex("lemma") != -1) {
      lemma = ai.getAttributeValue(t, "lemma");
    }

    String upos = "_";
    if (ai.getIndex("upos") != -1) {
      upos = ai.getAttributeValue(t, "upos");
    }

    String xpos = "_";
    if (ai.getIndex("xpos") != -1) {
      xpos = ai.getAttributeValue(t, "xpos");
    }

    String feats = "_";
    if (ai.getIndex("feats") != -1) {
      feats = ai.getAttributeValue(t, "feats");
    }


    pos = upos;
    posext = xpos;
    base = lemma;
    ctag = feats;


    if ((t.getTags() != null) && (t.getTags().size() != 0)) {
      Tag disambTag = t.getTags().get(0);
      for (Tag iterTag : t.getTags()) {
        if (iterTag.getDisamb()) {
          disambTag = iterTag;
        }
      }


//		for(Tag tag : t.getTags()){
//			if(tag.getDisamb() || t.getTags().size() == 1){
      Tag tag = disambTag;
      int firstSep = Math.max(0, tag.getCtag().indexOf(":"));
      if (firstSep > 0) {
        cpos = tag.getCtag().substring(0, firstSep);
      }
      ctag = tag.getCtag().substring(tag.getCtag().indexOf(":") + 1).replace(":", "|");
      if (ctag.equals(pos) || ctag.equals(posext)) {
        ctag = "_";
      }
      if (pos == null && posext == null) {
        if (cpos != null) {
          pos = cpos;
          posext = cpos;
        } else {
          pos = ctag;
          posext = ctag;
          ctag = "_";
        }
      }
    }


//			}
//		}
    //TODO: ctag dla interp conj, etc.
    //TODO: iż -> dlaczego nie ma pos?
    return String.format("%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t_\t%s\n", tokenIndex, orth, base, pos, posext, ctag, head, deprel, misc);
  }

  ;

  public String getStreamAsString() {
    return this.os.toString();
  }

  private void writeNewLine() throws IOException {
    this.os.write("\n".getBytes());
  }

  private void writeCommaNewLine() throws IOException {
    this.os.write(",\n".getBytes());
  }

/*
  {"clusters": [[[35, 35], [69, 69]]],
    "sentences": [
        ["Graph", "unification", "remains", "the", "most", "expensive", "part", "of", "unification-based", "grammar", "parsing", "."],
        ["We", "focus", "on", "one", "speed-up", "element", "in", "the", "design", "of", "unification", "algorithms", ":", "avoidance", "of", "copying", "of", "unmodified", "subgraphs", "."],
        ["We", "propose", "a", "method", "of", "attaining", "such", "a", "design", "through", "a", "method", "of", "structure-sharing", "which", "avoids", "log", "-LRB-", "d", "-RRB-", "overheads", "often", "associated", "with", "structure-sharing", "of", "graphs", "without", "any", "use", "of", "costly", "dependency", "pointers", "."],
        ["The", "proposed", "scheme", "eliminates", "redundant", "copying", "while", "maintaining", "the", "quasi-destructive", "scheme", "'s", "ability", "to", "avoid", "over", "copying", "and", "early", "copying", "combined", "with", "its", "ability", "to", "handle", "cyclic", "structures", "without", "algorithmic", "additions", "."]
    ],
    "ner": [[[0, 1, "Task"], [8, 10, "Task"]], [[16, 17, "Method"], [22, 23, "Method"], [27, 30, "OtherScientificTerm"], [29, 30, "OtherScientificTerm"]], [[35, 35, "Generic"], [45, 45, "Method"], [48, 52, "OtherScientificTerm"], [56, 58, "OtherScientificTerm"], [64, 65, "Method"]], [[69, 69, "Generic"], [71, 72, "OtherScientificTerm"], [76, 79, "OtherScientificTerm"], [82, 83, "OtherScientificTerm"], [85, 86, "OtherScientificTerm"], [93, 94, "OtherScientificTerm"]]],
    "relations": [[[0, 1, 8, 10, "PART-OF"]], [[16, 17, 22, 23, "PART-OF"]], [[45, 45, 35, 35, "USED-FOR"]], [[69, 69, 93, 94, "USED-FOR"], [76, 79, 69, 69, "FEATURE-OF"], [82, 83, 85, 86, "CONJUNCTION"]]],
    "doc_key": "C92-2068"}
 */


  @Override
  public void writeDocument(Document document) {

    boolean wasWritten = false;
    for (int i = 0; i < document.getSentences().size(); i++) {
      try {
        Sentence s = document.getSentences().get(i);
        StringBuffer sentenceStr = createSentenceStr(s);

        if (sentenceStr.length() > 0) {
          if (wasWritten) {
            writeNewLine();
          }
          wasWritten = true;
          this.os.write(sentenceStr.toString().getBytes());
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  @Override
  public void flush() {
    try {
      os.flush();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void close() {
    try {
      this.os.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}

