package g419.corpus.io.writer;

import g419.corpus.structure.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

//import java.io.BufferedOutputStream;

public class WebNLGProcessedStreamWriter extends AbstractDocumentWriter {

  private static int counter = 0;

  private OutputStream os;

  public WebNLGProcessedStreamWriter(OutputStream os) {
    this.os = os;
  }

  public WebNLGProcessedStreamWriter() {
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

    return relatedStr;
  }


  private StringBuffer generateStringFromRelations(List<RelationDesc> relations) {

    StringBuffer sb = new StringBuffer();

    if (relations.size() == 0)
      return sb;

    counter++;
    sb.append("{ ");

    sb.append("\"sentText\": \"");
    sb.append(getSentenceForWebNLG(relations.get(0).getSentence()));
    sb.append("\", ");

    sb.append("\"relationMentions\": [");


    for (int i = 0; i < relations.size(); i++) {
      RelationDesc rd = relations.get(i);
      System.out.println(" RelDesc=" + rd);
      if (i > 0) {
        sb.append(", ");
      }
      StringBuffer str = convertRelationToWebNLG(rd);
      sb.append(str);
    }

    sb.append(" ] }");


    System.out.println(" returning length:" + sb.toString().length() + "'");
    return sb;
  }



    /*
{"sentText": "Alan Bean who was born in 1932 worked for NASA and became a crew member of Apollo 12 is now currently retired .",
"relationMentions": [
{"em1Text": "Bean", "em2Text": "12", "label": "was a crew member of"},
{"em1Text": "12", "em2Text": "NASA", "label": "operator"}
]}
    */


  private StringBuffer convertRelationToWebNLG(RelationDesc relDesc) {

    Sentence s = relDesc.getSentence();
    StringBuffer sb = new StringBuffer();


    String em1Text = s.getTokenById(relDesc.getFromTokenId()).getAttributeValue(1);
    String em2Text = s.getTokenById(relDesc.getToTokenId()).getAttributeValue(1);
    String label = relDesc.getType();

    String rdText = "{ \"em1Text\": \"" + em1Text + "\", \"em2Text\": \"" + em2Text + "\", \"label\": \"" + label + "\" }";

    sb.append(rdText);

    return sb;
  }

  private String getSentenceForWebNLG(Sentence s) {

    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < s.getTokens().size(); i++) {

      if (i > 0) {
        sb.append(" ");
      }
      String tokenString = s.getTokens().get(i).getAttributeValue(1);

      if (tokenString.equals("\"")) {
        sb.append("''");
      } else {
        sb.append(tokenString);
      }
    }

    return sb.toString();
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
          if (wasWritten) {
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

