package g419.corpus.io.reader;

import com.google.common.collect.Maps;
import g419.corpus.io.DataFormatException;
import g419.corpus.structure.*;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**   (from https://universaldependencies.org/format.html ):
 *
 * Sentences consist of one or more word lines, and word lines contain the following fields:
 *
 *     ID: Word index, integer starting at 1 for each new sentence; may be a range for multiword tokens; may be a decimal number for empty nodes (decimal numbers can be lower than 1 but must be greater than 0).
 *     FORM: Word form or punctuation symbol.
 *     LEMMA: Lemma or stem of word form.
 *     UPOS: Universal part-of-speech tag.
 *     XPOS: Language-specific part-of-speech tag; underscore if not available.
 *     FEATS: List of morphological features from the universal feature inventory or from a defined language-specific extension; underscore if not available.
 *     HEAD: Head of the current word, which is either a value of ID or zero (0).
 *     DEPREL: Universal dependency relation to the HEAD (root iff HEAD = 0) or a defined language-specific subtype of one.
 *     DEPS: Enhanced dependency graph in the form of a list of head-deprel pairs.
 *     MISC: Any other annotation.
 *
 * The fields DEPS and MISC replace the obsolete fields PHEAD and PDEPREL of the CoNLL-X format. In
 * addition, we have modified the usage of the ID, FORM, LEMMA, XPOS, FEATS and HEAD fields as explained below.
 *
 * The fields must additionally meet the following constraints:
 *
 *     Fields must not be empty.
 *     Fields other than FORM, LEMMA, and MISC must not contain space characters.
 *     Underscore (_) is used to denote unspecified values in all fields except ID. Note that no format-level
 *     distinction is made for the rare cases where the FORM or LEMMA is the literal underscore – processing in
 *     such cases is application-dependent. Further, in UD treebanks the UPOS, HEAD, and DEPREL columns are not
 *     allowed to be left unspecified except in multiword tokens, where all must be unspecified, and empty nodes,
 *     where UPOS is optional and HEAD and DEPREL must be unspecified.
 */

public class ConlluStreamReader extends AbstractDocumentReader {
  static private Pattern annotationPattern = Pattern.compile("([IB])-([^#]*)");
  private BufferedReader stream;
  private TokenAttributeIndex attributeIndex = null;
  private String nextParagraphId = null;
  private Document documentBuffor;

  public ConlluStreamReader(InputStream is) {
    stream = new BufferedReader(new InputStreamReader(is));
    attributeIndex = new TokenAttributeIndex();
    attributeIndex.addAttribute("id");
    attributeIndex.addAttribute("orth");
    //attributeIndex.addAttribute("form");
    attributeIndex.addAttribute("lemma");
    attributeIndex.addAttribute("upos");
    attributeIndex.addAttribute("xpos");
    attributeIndex.addAttribute("feats");
    attributeIndex.addAttribute("head");
    attributeIndex.addAttribute("deprel");
    attributeIndex.addAttribute("deps");
    attributeIndex.addAttribute("misc");

    documentBuffor = readNextDocument();
  }

  @Override
  public void close() {
    try {
      stream.close();
    } catch (IOException ex) {
      LoggerFactory.getLogger(getClass()).error("Failed to close the input stream", ex);
    }
  }

  @Override
  protected TokenAttributeIndex getAttributeIndex() {
    return this.attributeIndex;
  }

  @Override
  public Document nextDocument() throws DataFormatException {
    Document document = documentBuffor;
    documentBuffor = readNextDocument();
    return document;
  }

  private Document readNextDocument() {
    String line;
    TokenAttributeIndex index = this.attributeIndex.clone();

    Paragraph paragraph = new Paragraph(nextParagraphId);
    paragraph.setAttributeIndex(index);
    this.nextParagraphId = null;
    Sentence currentSentence = new Sentence();

    try {
      while ((line = this.stream.readLine()) != null) {
        line = line.trim();
        if (line.length() == 0) {
          if (currentSentence.getTokenNumber() > 0) {
            currentSentence.setId("sent" + paragraph.numSentences() + 1);
            paragraph.addSentence(currentSentence);
            currentSentence = new Sentence();
          }
        } else {
          String[] columns = line.split("\t");

          if(columns[0].startsWith("#")) {
            continue;
          }

          //Token token = new Token(columns[0], new Tag("base", "ctag", false), index);
          NodeToken token = new NodeToken(this.attributeIndex);
          for (int i = 0; i < columns.length; i++) {
            token.setAttributeValue(i, columns[i]);
          }
          currentSentence.addToken(token);
        }
      }
    } catch (IOException e) {
      // ToDo: Use logger to log the exception
      e.printStackTrace();
      System.out.println("Exception");
    }

    if (currentSentence.getTokenNumber() > 0) {
      currentSentence.setId("sent" + paragraph.numSentences() + 1);
      paragraph.addSentence(currentSentence);
    }

    // Setup children i parent nodes
    int parentAttribute = 6;  //HEAD
    for (Sentence s : paragraph.getSentences()) {
      for (Token token : s.getTokens()) {
        int parentIndex = Integer.parseInt(token.getAttributeValue(parentAttribute)) - 1;
        if (parentIndex >= 0) {
          ((NodeToken) token).setParent((NodeToken) s.getTokens().get(parentIndex));
          ((NodeToken) s.getTokens().get(parentIndex)).addChild((NodeToken) token);
        }
      }
    }

    return paragraph.getSentences().size() == 0 ? null : new Document("conllu", Arrays.asList(paragraph), index);
  }

  @Override
  public boolean hasNext() {
    return documentBuffor != null;
  }
}
