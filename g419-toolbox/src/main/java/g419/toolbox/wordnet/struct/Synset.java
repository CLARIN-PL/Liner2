package g419.toolbox.wordnet.struct;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
  private int depth;
  List<LexicalUnit> lexicalUnits = Lists.newArrayList();

  public Synset(final int id) {
    this.id = id;
  }

  public String getPos() {
    return lexicalUnits.stream().map(LexicalUnit::getPos).findFirst().orElse("");
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Synset synset = (Synset) o;
    return id == synset.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public String getLexicalUnitsStr() {
    return lexicalUnits.stream()
        .map(lu -> String.format("%s.%d", lu.getName(), lu.getVariant()))
        .collect(Collectors.joining(", "));
  }
}
