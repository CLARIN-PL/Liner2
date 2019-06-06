package g419.liner2.core.tools;

import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.Paragraph;

import java.util.HashMap;

/**
 * Class for counting base occurrence frequency document-wise.
 *
 * @author Michał Gawor
 */

public class BaseCount {
    private HashMap<String, Integer> baseCount = null;

    public BaseCount(final Document document){
        this.baseCount = new HashMap<>();

        for (final Paragraph p : document.getParagraphs()) {
            for (final Sentence s : p.getSentences()) {
                for (final Token t : s.getTokens()) {
                    final String lemma = t.getAttributeValue("base");
                    if (this.baseCount.containsKey(lemma)) {
                        this.baseCount.put(lemma, baseCount.get(lemma) + 1);
                    } else {
                        this.baseCount.put(lemma, 1);
                    }
                }
            }
        }
    }

    public int getBaseCount(final Token t){
        return this.baseCount.get(t.getAttributeValue("base"));
    }

    public int getBaseCount(final String base){
        return this.baseCount.get(base);
    }
}
