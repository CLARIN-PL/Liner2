package g419.serel.structure.patternMatch;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import lombok.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class PatternMatchExtraInfo {

  Sentence sentence;
  private Map<String, NamedEntityWithToken> roleMap = new HashMap<>();
  private Map<Token, String> token2tagNE = new HashMap<>();

  public PatternMatchExtraInfo() {}

  public PatternMatchExtraInfo(final PatternMatchExtraInfo pmei) {
    this.sentence = pmei.sentence;
    this.roleMap = new HashMap<>();
    this.roleMap.putAll(pmei.getRoleMap());
    this.token2tagNE.putAll(pmei.token2tagNE);
  }


  public void putRole(final String role, final String _namedEntity, final Token _token) {
    final NamedEntityWithToken newt = new NamedEntityWithToken();
    newt.namedEntity = _namedEntity;
    newt.token = _token;
    roleMap.put(role, newt);
    token2tagNE.put(_token, _namedEntity);
  }

  public NamedEntityWithToken getRole(final String key) {
    return roleMap.get(key);
  }


  public String getRoleValue(final String key) {
    final NamedEntityWithToken newt = this.getRole(key);
    final List<Integer> resultIds = sentence.getBoiTokensIdsForTokenAndName(newt.token, newt.namedEntity);
    return resultIds.stream().map(id -> sentence.getTokens().get(id - 1).getOrth()).collect(Collectors.joining(" "));
  }


  @Override
  public String toString() {
    return roleMap.toString();
  }

  public String description() {
    return roleMap.entrySet().stream().map(elem -> elem.getKey() + ":" + getRoleValue(elem.getKey())).collect(Collectors.joining(", "));
  }

  public Set<Integer> getAnchorIds() {
    return roleMap.values().stream().map(newt -> newt.token.getNumberId()).collect(Collectors.toSet());
  }

  public String getTagNEFromToken(final Token token) {
    return token2tagNE.get(token);
  }

}
