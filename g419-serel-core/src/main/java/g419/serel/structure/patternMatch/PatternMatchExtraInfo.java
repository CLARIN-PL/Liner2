package g419.serel.structure.patternMatch;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import lombok.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class PatternMatchExtraInfo {

  Sentence sentence;
  private Map<String, NamedEntityWithToken> roleMap = new HashMap<>();

  public PatternMatchExtraInfo() {}

  public PatternMatchExtraInfo(final PatternMatchExtraInfo pmei) {
    this.sentence = pmei.sentence;
    this.roleMap = new HashMap<>();
    this.roleMap.putAll(pmei.getRoleMap());
  }


  public void putRole(final String role, final String _namedEntity, final Token _token) {
    final NamedEntityWithToken newt = new NamedEntityWithToken();
    newt.namedEntity = _namedEntity;
    newt.token = _token;
    roleMap.put(role, newt);
  }

  public NamedEntityWithToken getRole(final String key) {
    return roleMap.get(key);
  }


  public String getRoleValue(final String key) {
    final NamedEntityWithToken newt = this.getRole(key);
    final List<Integer> resultIds = sentence.getBoiIndexesForTokenAndName(newt.token, newt.namedEntity);
    return resultIds.stream().map(id -> sentence.getTokens().get(id - 1).getOrth()).collect(Collectors.joining(" "));
  }


  @Override
  public String toString() {
    return roleMap.toString();
  }

  public String description() {
    return roleMap.entrySet().stream().map(elem -> elem.getKey() + ":" + getRoleValue(elem.getKey())).collect(Collectors.joining(", "));
  }

}
