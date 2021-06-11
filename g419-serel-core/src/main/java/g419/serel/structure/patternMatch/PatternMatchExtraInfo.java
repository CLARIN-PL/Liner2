package g419.serel.structure.patternMatch;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import lombok.Data;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.min;

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


  public String getNEForRole(final String key) {
    final NamedEntityWithToken newt = this.getRole(key);
    return newt.namedEntity;
  }


  public String getRoleValue(final String key) {
    final NamedEntityWithToken newt = this.getRole(key);
    final LinkedHashSet<Integer> resultIds = sentence.getBoiTokensIdsForTokenAndName(newt.token, newt.namedEntity);
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
    return roleMap.values().stream().flatMap(newt -> sentence.getBoiTokensIdsForTokenAndName(newt.token, newt.namedEntity).stream()).collect(Collectors.toSet());
  }

  public Set<Integer> getRoleE1Ids() {
    return new HashSet<>(sentence.getBoiTokensIdsForTokenAndName(roleMap.get("e1").token, roleMap.get("e1").namedEntity));
  }

  public Integer getRoleE1MinId() {
    return min(getRoleE1Ids());
  }

  public Set<Integer> getRoleE2Ids() {
    return new HashSet<>(sentence.getBoiTokensIdsForTokenAndName(roleMap.get("e2").token, roleMap.get("e2").namedEntity));
  }

  public Integer getRoleE2MinId() {
    return min(getRoleE2Ids());
  }


  public String getTagNEFromToken(final Token token) {
    return token2tagNE.get(token);
  }


}
