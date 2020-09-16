package g419.liner2.core.tools.parser;

import g419.corpus.structure.Sentence;

public class ComboSentence extends ParseTree {

  private Sentence sentence;

  public ComboSentence(Sentence s) { this.sentence = s;}

  public Sentence getSentence() { return this.sentence; }

}
