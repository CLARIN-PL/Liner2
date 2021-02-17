package g419.serel.wordnet;

import lombok.Data;

@Data
public class LexicalUnit {

  private int id;
  private String name;
  private String pos;
  private String domain;
  private String description;
  private String workstate;
  private String source;
  private int variant;

  public LexicalUnit(final int id, final String name) {
    this.id = id;
    this.name = name;
  }

}