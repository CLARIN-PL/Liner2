package g419.corpus.io.writer.tei;

import com.google.common.collect.Lists;
import g419.corpus.structure.Tag;
import java.util.List;

public class Interps {

  List<TeiLex> lexemes = null;
  String disamb = null;
  int disambIdx = 0;

  public Interps(final List<Tag> tags) {
    tagsToTEI(tags);
  }

  public List<TeiLex> getLexemes() {
    return lexemes;
  }

  public int getDisambIdx() {
    return disambIdx;
  }

  public String getDisamb() {
    return disamb;
  }

  public TeiLex getDisambLex() {
    return lexemes.get(disambIdx);
  }

  public void tagsToTEI(final List<Tag> tags) {
    lexemes = Lists.newArrayList();
    int msdIdx = 0;
    for (final Tag tag : tags) {
      final String base = tag.getBase();
      final String[] ctag = tag.getCtag().split(":");
      final String TEIctag = ctag[0];
      final String msd;
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
      for (final TeiLex lex : lexemes) {
        if (lex.match(base, TEIctag)) {
          foundMatch = true;
          lex.addMsd(msd, msdIdx++);
          break;
        }
      }
      if (!foundMatch) {
        final TeiLex newLex = new TeiLex(base, TEIctag);
        newLex.addMsd(msd, msdIdx++);
        newLex.setId(tag.getId());
        lexemes.add(newLex);
      }
    }

    if (disamb == null) {
      final TeiLex firstLex = lexemes.get(0);
      disamb = firstLex.base + ":" + firstLex.ctag + ":" + firstLex.msdList.get(0).getLeft();
    }
  }
}
