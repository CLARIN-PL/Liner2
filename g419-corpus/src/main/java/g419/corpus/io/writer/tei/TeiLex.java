package g419.corpus.io.writer.tei;

import com.google.common.collect.Lists;
import g419.corpus.structure.IdentifiableElement;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class TeiLex extends IdentifiableElement {

  String base;
  String ctag;
  List<Pair<String, Integer>> msdList;
  boolean disamb;
  int disambMsdIdx;

  public TeiLex(final String base, final String ctag) {
    this.base = base;
    this.ctag = ctag;
    msdList = Lists.newArrayList();
    disamb = false;
  }

  public String getBase() {
    return base;
  }

  public String getCtag() {
    return ctag;
  }

  public String getFullCtag() {
    final List<String> full = Lists.newArrayList(base, ctag);
    if (msdList.size() > 0 && disambMsdIdx < msdList.size()
        && msdList.get(disambMsdIdx).getKey().length() > 0) {
      full.add(msdList.get(disambMsdIdx).getKey());
    }
    return String.join(":", full);
  }

  public boolean isDisamb() {
    return disamb;
  }

  public List<Pair<String, Integer>> getMsds() {
    return msdList;
  }

  public void setDisambTrue(final int msdIdx) {
    if (!disamb) {
      disamb = true;
      disambMsdIdx = msdIdx;
    }
  }

  public void addMsd(final String msd, final int idx) {
    msdList.add(new ImmutablePair<>(msd, idx));
  }

  public boolean match(final String base, final String ctag) {
    return this.base.equals(base) && this.ctag.equals(ctag);
  }

  public int msdSize() {
    return msdList.size();
  }

}
