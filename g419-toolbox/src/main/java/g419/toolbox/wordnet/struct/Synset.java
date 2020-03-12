package g419.toolbox.wordnet.struct;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.Data;

@Data
public class Synset {

  private int id;
  private String workstate;
  private int split;
  private String owner;
  private String definition;
  private String description;
  private boolean isAbstract;
  List<LexicalUnit> lexicalUnits = Lists.newArrayList();

  public Synset(final int id) {
    this.id = id;
  }
}
