package g419.corpus.io.writer;

import g419.corpus.structure.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

//import java.io.BufferedOutputStream;

public class TacredStreamWriter extends AbstractDocumentWriter {

  private static int counter = 0;

  private OutputStream os;

  public TacredStreamWriter(OutputStream os) {
    this.os = os;
  }

  public TacredStreamWriter() {
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
                .type("no_relation")
                .sentence(s)
                .build();

            unrelated.add(rd);

            if (unrelated.size() >= related.size() * 4) {
              break outer;
            }
          }
        }
      }
    }

    StringBuffer unrelatedStr = generateStringFromRelations(unrelated);

    if (unrelatedStr.length() > 0) {
      //System.out.println("adding unrelated");
      relatedStr.append("\n, \n").append(unrelatedStr);
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
        if (isOkForTacred(rd)) {
          if (wasWritten) {
            sb.append("\n,\n");
          }

          System.out.println(" RelDesc=" + rd);

          StringBuffer str = convertToTacred(rd);

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


  private boolean isOkForTacred(RelationDesc rd) {
    // czy nazwy NE są co najwyzej trójczłonowe , chyba że goe_admin
    return true;
  }

  private StringBuffer convertToTacred(RelationDesc relDesc) {

    Sentence s = relDesc.getSentence();
    StringBuffer sb = new StringBuffer();

    sb.append("{\n");

    String id = relDesc.getSentence().getDocument().getName()
        + "_" + relDesc.getSentenceIndex()
        + "_" + System.currentTimeMillis()
        + "_" + counter;

    counter++;

    sb.append("\"id\": \"" + id + "\",\n");
    sb.append("\"relation\": \"" + relDesc.getType() + "\",\n");
    sb.append("\"token\": [ \n");
    sb.append(getTokensForTacred(s));
    sb.append("\n], \n");
    sb.append("\"subj_start\": " + relDesc.getFromTokenId() + ",\n");

    int subj_end = relDesc.getSentence()
        .getMaxBoiTokenIdForTokenAndName(
            relDesc.getSentence().getTokens().get(relDesc.getFromTokenId() - 1),
            relDesc.getFromType());

    sb.append("\"subj_end\": " + subj_end + ",\n");
    sb.append("\"obj_start\": " + relDesc.getToTokenId() + ",\n");

    int obj_end = relDesc.getSentence()
        .getMaxBoiTokenIdForTokenAndName(
            relDesc.getSentence().getTokens().get(relDesc.getToTokenId() - 1),
            relDesc.getToType());


    sb.append("\"obj_end\": " + obj_end + ",\n");
    sb.append("\"subj_type\": \"" + relDesc.getFromType() + "\",\n");
    sb.append("\"obj_type\": \"" + relDesc.getToType() + "\",\n");
    sb.append("\"stanford_pos\": [ \n");
    sb.append(getPosForTacred(s));
    sb.append("\n], \n");
    sb.append("\"stanford_ner\": [ \n");
    sb.append(getNersForTacred(s));
    sb.append("\n], \n");
    sb.append("\"stanford_head\": [ \n");
    sb.append(getHeadsForTacred(s));
    sb.append("\n], \n");
    sb.append("\"stanford_deprel\": [ \n");
    sb.append(getDepRelsForTacred(s));
    sb.append("\n] \n");
    sb.append("}\n");
    sb.append("\n");

    return sb;

  }

  private String getTokensForTacred(Sentence s) {
    return s.getTokens().stream().map(t -> "\"" + t.getAttributeValue(1) + "\"").collect(Collectors.joining(", \n"));
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


  @Override
  public void writeDocument(Document document) {

    boolean wasWritten = false;
    for (int i = 0; i < document.getSentences().size(); i++) {
      try {
        Sentence s = document.getSentences().get(i);

        StringBuffer sentenceStr = createSentenceStr(s);
        System.out.println("Sent str length =" + sentenceStr.length());

        if (sentenceStr.length() > 0) {
          if (wasWritten) {
            writeCommaNewLine();
            writeNewLine();
          }


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

