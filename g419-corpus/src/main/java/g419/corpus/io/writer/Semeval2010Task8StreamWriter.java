package g419.corpus.io.writer;

import g419.corpus.structure.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

//import java.io.BufferedOutputStream;

public class Semeval2010Task8StreamWriter extends AbstractDocumentWriter {

  private static int counter = 0;

  private OutputStream os;

  public Semeval2010Task8StreamWriter(OutputStream os) {
    this.os = os;
  }

  public Semeval2010Task8StreamWriter() {
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
    StringBuffer relatedStr = generateStringFromRelations(related);

    // try to write negative examples


    System.out.println("Creating unrelated");
    List<Boi> bois = s.getAllMainRawBoisBegin();
    bois = bois.stream().filter(Boi::isOkForTacred).collect(Collectors.toList());
    List<RelationDesc> namRels = s.getNamRels();

    List<RelationDesc> unrelated = new LinkedList<>();

    outer:
    for (int i = 0; i < bois.size(); i++) {
      for (int j = 0; j < bois.size(); j++) {
        if (i != j) {
          Boi boiFrom = bois.get(i);
          Boi boiTo = bois.get(j);

          if (!isRelated(boiFrom, boiTo, namRels)) {
            RelationDesc rd = RelationDesc.builder()
                .fromTokenId(boiFrom.startId)
                .fromType(boiFrom.label)
                .toTokenId(boiTo.startId)
                .toType(boiTo.label)
                .type("Other")
                .sentence(s)
                .build();

            unrelated.add(rd);

            if (related.size() > 0) {
              if (unrelated.size() >= related.size() * 3) {
                break outer;
              }
            }
          }
        }
      }
    }

    StringBuffer unrelatedStr = generateStringFromRelations(unrelated);

    if (unrelatedStr.length() > 0) {
      System.out.println("adding unrelated");
      relatedStr.append(unrelatedStr);
    }


    return relatedStr;
  }


  private boolean isRelated(Boi from, Boi to, List<RelationDesc> namRels) {
    for (RelationDesc rd : namRels) {
      if (
          (
              (rd.getFromTokenId() == from.startId)
                  && (rd.getFromType().equals(from.label))
                  && (rd.getToTokenId() == to.startId)
                  && (rd.getToType().equals(to.label))
          )
              ||   // sprawdzamy oba kierunki relacji - > ???
              (
                  (rd.getFromTokenId() == to.startId)
                      && (rd.getFromType().equals(to.label))
                      && (rd.getToTokenId() == from.startId)
                      && (rd.getToType().equals(from.label))
              )

      ) {
        return true;
      }
    }

    return false;
  }


  private StringBuffer generateStringFromRelations(List<RelationDesc> relations) {

    StringBuffer sb = new StringBuffer();

    if (relations.size() > 0) {
      boolean wasWritten = false;

      for (RelationDesc rd : relations) {
        if (isOkForSemeval2010Task8(rd)) {
//          if (wasWritten) {
//            sb.append("\n");
//          }

          System.out.println(" RelDesc=" + rd);

          StringBuffer str = convertToSemeval2010Task8(rd);

          if (str.length() > 0) {
            sb.append(str);
            wasWritten = true;
          }
        }
      }
    }

    System.out.println(" returning length:" + sb.toString().length() + "'");
    return sb;


  }


  private boolean isOkForSemeval2010Task8(RelationDesc rd) {
    // czy nazwy NE są co najwyzej trójczłonowe , chyba że goe_admin
    return true;
  }

  private StringBuffer convertToSemeval2010Task8(RelationDesc relDesc) {

    Sentence s = relDesc.getSentence();
    StringBuffer sb = new StringBuffer();

    String id = relDesc.getSentence().getDocument().getName()
        + "_" + relDesc.getSentenceIndex()
        + "_" + System.currentTimeMillis()
        + "_" + counter;

    /*
    {
      'token': ['The', 'ship', 'left', 'from', 'the', 'port', 'of', 'Bremen', 'with', '434', 'passengers', '.'],
      'relation':'Entity-Origin(e1,e2)', 'h':{'pos': [1, 2]},'t':{'pos': [5, 6]}
    }
    */

    counter++;
    sb.append("{ ");


    sb.append("'token': [ ");
    sb.append(getTokensForSemeval2010Task8(s));
    sb.append(" ], ");

    sb.append("'relation': '" + relDesc.getType() + "', ");

    int subj_end = relDesc.getSentence()
        .getMaxBoiTokenIdForTokenAndName(
            relDesc.getSentence().getTokens().get(relDesc.getFromTokenId() - 1),
            relDesc.getFromType());
    sb.append("'h':{'pos':[ " + (relDesc.getFromTokenId() - 1) + "," + subj_end + " ]}, ");

    int obj_end = relDesc.getSentence()
        .getMaxBoiTokenIdForTokenAndName(
            relDesc.getSentence().getTokens().get(relDesc.getToTokenId() - 1),
            relDesc.getToType());
    sb.append("'t':{'pos':[ " + (relDesc.getToTokenId() - 1) + "," + obj_end + " ]} ");
    sb.append("} ");
    sb.append("\n");


    return sb;

  }

  private String getTokensForSemeval2010Task8(Sentence s) {

    int insideDoubleQuotes = 0;

    int[] offsets = new int[s.getTokens().size()];
    int offset = 0;

    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < s.getTokens().size(); i++) {

      String tokenString = s.getTokens().get(i).getAttributeValue(1);

      if (insideDoubleQuotes == 1) { // własnie dopiero wszedł ...


      }


      if (tokenString.equals("\"")) {

        if (insideDoubleQuotes > 0) {
          offset--;
          insideDoubleQuotes = 0;
          // doklejasz do ostatniego co był ...


        } else {
          offset--;
          insideDoubleQuotes = 1; // just stepped into ...
          // doklejasz do nastepnego co będzie ...


        }
      } else if (tokenString.contains("'")) {
        // splituj po '
        // roczłonkowywuj

      }


    }


    return s.getTokens().stream().map(t -> "\'" + escape(t.getAttributeValue(1)) + "\'").collect(Collectors.joining(", "));
  }

  private String escape(String s) {
    return s.replaceAll("\"", "\\\\\"");
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


  @Override
  public void writeDocument(Document document) {

    boolean wasWritten = false;
    for (int i = 0; i < document.getSentences().size(); i++) {
      try {
        Sentence s = document.getSentences().get(i);

        StringBuffer sentenceStr = createSentenceStr(s);
        System.out.println("Sent str length =" + sentenceStr.length());

        if (sentenceStr.length() > 0) {
//          if (wasWritten) {
//            writeNewLine();
//          }

          wasWritten = true;
          this.os.write(sentenceStr.toString().getBytes());
        }

      } catch (IOException e) {
        // TODO Auto-generated catch block
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

