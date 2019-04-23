package g419.corpus.io.writer.tei;

import com.google.common.collect.Lists;
import g419.corpus.structure.Tag;

import java.util.List;

public class Interps {

  List<TeiLex> lexemes = null;
  String disamb = null;
  int disambIdx = 0;

  public Interps(List<Tag> tags) {
    tagsToTEI(tags);
  }

  public List<TeiLex> getLexemes() {
    return this.lexemes;
  }

  public int getDisambIdx() {
    return this.disambIdx;
  }

  public String getDisamb() {
    return this.disamb;
  }

  public void tagsToTEI(List<Tag> tags) {
    lexemes = Lists.newArrayList();
    int msdIdx = 0;
    for (Tag tag : tags) {
      String base = tag.getBase();
      String[] ctag = tag.getCtag().split(":");
      String TEIctag = ctag[0];
      String msd;
      if (ctag.length > 1) {
        msd = tag.getCtag().substring(TEIctag.length() + 1);
      } else {
        msd = "";
      }

      if (disamb == null && tag.getDisamb()) {
        disambIdx = msdIdx;
        disamb = base + ":" + TEIctag + ":" + msd;
      }

      boolean foundMatch = false;
      for (TeiLex lex : lexemes) {
        if (lex.match(base, TEIctag)) {
          foundMatch = true;
          lex.addMsd(msd, msdIdx++);
          break;
        }
      }
      if (!foundMatch) {
        TeiLex newLex = new TeiLex(base, TEIctag);
        newLex.addMsd(msd, msdIdx++);
        lexemes.add(newLex);
      }
    }

    if (disamb == null) {
      TeiLex firstLex = lexemes.get(0);
      disamb = firstLex.base + ":" + firstLex.ctag + ":" + firstLex.msdList.get(0).getLeft();
    }
  }
}
