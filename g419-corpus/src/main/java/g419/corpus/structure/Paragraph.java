package g419.corpus.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Paragraph extends IdentifiableElement {

  private TokenAttributeIndex attributeIndex = new TokenAttributeIndex();
  private HashMap<String, String> chunkMetaData = new HashMap<>();

  private final ArrayList<Sentence> sentences = new ArrayList<>();

  public Paragraph(final String id, final TokenAttributeIndex attributeIndex) {
    this.id = id;
    this.attributeIndex = attributeIndex;
  }

  public Paragraph(final String id) {
    this.id = id;
  }

  public void addSentence(final Sentence sentence) {
    sentences.add(sentence);
    if (sentence.getAttributeIndex() == null) {
      sentence.setAttributeIndex(attributeIndex);
    }
  }

  public TokenAttributeIndex getAttributeIndex() {
    return attributeIndex;
  }

  public ArrayList<Sentence> getSentences() {
    return sentences;
  }

  public void setAttributeIndex(final TokenAttributeIndex attributeIndex) {
    this.attributeIndex = attributeIndex;
    for (final Sentence s : sentences) {
      s.setAttributeIndex(attributeIndex);
    }
  }

  public void setChunkMetaData(final HashMap<String, String> chunkMetaData) {
    this.chunkMetaData = chunkMetaData;
  }

  public Set<String> getKeysChunkMetaData() {
    return chunkMetaData.keySet();
  }

  public String getChunkMetaData(final String key) {
    return chunkMetaData.get(key);
  }

  @Override
  public Paragraph clone() {
    final Paragraph copy = new Paragraph(id);
    copy.chunkMetaData = new HashMap<>(chunkMetaData);
    copy.attributeIndex = attributeIndex.clone();
    for (final Sentence s : sentences) {
      copy.addSentence(s.clone());
    }
    return copy;
  }

  public int numSentences() {
    return sentences.size();
  }

  public void setDocument(final Document document) {
    for (final Sentence sentence : sentences) {
      sentence.setDocument(document);
    }
  }
}
