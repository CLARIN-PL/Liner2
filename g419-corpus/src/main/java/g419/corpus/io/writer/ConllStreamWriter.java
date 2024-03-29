package g419.corpus.io.writer;

import g419.corpus.structure.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

//import java.io.BufferedOutputStream;

public class ConllStreamWriter extends AbstractDocumentWriter {

  private OutputStream os;

  public ConllStreamWriter(OutputStream os) {
    this.os = os;
  }

  public ConllStreamWriter() {
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

  private void writeSentence(Sentence s) throws IOException {
    int index = 0;
    TokenAttributeIndex ai = s.getAttributeIndex();
    for (Token t : s.getTokens()) {
      this.os.write(convertToken(t, ai, ++index).getBytes());
      //writeToken(t, ai, ++index);
    }
  }

  ;

  public static String convertToken(Token t, TokenAttributeIndex ai, int tokenIndex) {
    String orth = t.getOrth();
    String base = ai.getAttributeValue(t, "base");
    String posext = ai.getAttributeValue(t, "class");
    String pos = ai.getAttributeValue(t, "pos");
    String ctag = "";
    String cpos = null;
    String head = "_";
    if(ai.getIndex("head") != -1) {
      ai.getAttributeValue(t, "head");
    }
    String deprel = "_";
    if(ai.getIndex("deprel") != -1) {
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
    if(ai.getIndex("misc")!=-1) {
      misc = ai.getAttributeValue(t, "misc");
    }


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
//			}
//		}
    //TODO: ctag dla interp conj, etc.
    //TODO: iż -> dlaczego nie ma pos?
    return String.format("%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t_\t%s\n", tokenIndex, orth, base, pos, posext, ctag, head,deprel,  misc);
  }

  ;

  public String getStreamAsString() {
    return this.os.toString();
  }

  private void writeNewLine() throws IOException {
    this.os.write("\n".getBytes());
  }

  @Override
  public void writeDocument(Document document) {
    for (Sentence s : document.getSentences()) {
      try {
        writeSentence(s);
        writeNewLine();
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
