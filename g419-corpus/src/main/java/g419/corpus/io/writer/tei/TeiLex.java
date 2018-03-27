package g419.corpus.io.writer.tei;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class TeiLex {

    String base;
    String ctag;
    List<Pair<String, Integer>> msdList;
    boolean disamb;
    int disambMsdIdx;

    public TeiLex(String base, String ctag) {
        this.base = base;
        this.ctag = ctag;
        msdList = Lists.newArrayList();
        disamb = false;
    }

    public String getBase() {
        return this.base;
    }

    public String getCtag() {
        return this.ctag;
    }

    public boolean isDisamb() {
        return disamb;
    }

    public List<Pair<String, Integer>> getMsds() {
        return this.msdList;
    }

    public void setDisambTrue(int msdIdx) {
        if (!disamb) {
            disamb = true;
            disambMsdIdx = msdIdx;
        }
    }

    public void addMsd(String msd, int idx) {
        msdList.add(new ImmutablePair<>(msd, idx));
    }

    public boolean match(String base, String ctag) {
        return this.base.equals(base) && this.ctag.equals(ctag);
    }

    public int msdSize() {
        return msdList.size();
    }

}
