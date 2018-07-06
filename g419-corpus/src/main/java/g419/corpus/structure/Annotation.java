package g419.corpus.structure;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Klasa reprezentuje anotację jako ciągłą sekwencję tokenów w zdaniu.
 *
 * @author Michał Marcińczuk
 */
public class Annotation extends IdentifiableElement {
    /**
     * Annotation type, i.e. name of category.
     */
    private String type = null;

    /**
     * Name of the group to which belongs the annotation type, ex. word, group, chunk, ne .
     */
    private String group = null;

    /**
     * Sentence to which the annotation belongs.
     */
    private Sentence sentence = null;

    /**
     * Indices of tokens which form the annotation.
     */
    private TreeSet<Integer> tokens = Sets.newTreeSet();

    /**
     * Index of a token that is the head of the annotation
     */
    private int head;

    /**
     * Lemmatized form of the annotation
     */
    private String lemma = null;

    /**
     * Indeks anotacji w kanale.
     */
    private int channelIdx;

    /**
     * Informacja czy anotacja ma oznaczoną głowę.
     */
    private boolean hasHead = false;

    /**
     * Wartość określająca pewność co do istnienia anotacji.
     * Głównie używane przy autoamtycznym rozpoznawaniu anotacji w tekście.
     */
    private double confidence = 1.0;


    private Map<String, String> metadata = Maps.newHashMap();

    public Annotation(final String id, final int begin, final int end, final String type, final Sentence sentence) {
        this(begin, end, type, sentence);
        this.id = id;
    }

    public Annotation(final int begin, final int end, final String type, final Sentence sentence) {
        for (int i = begin; i <= end; i++) {
            this.tokens.add(i);
        }
        this.type = type;
        this.sentence = sentence;
        this.assignHead();
    }

    public Annotation(final int tokenIndex, final String type, final Sentence sentence) {
        this(tokenIndex, tokenIndex, type, sentence);
    }

    public Annotation(final int begin, final String type, final int channelIdx, final Sentence sentence) {
        this(begin, begin, type, sentence);
        this.channelIdx = channelIdx;
    }

    public Annotation(final TreeSet<Integer> tokens, final String type, final Sentence sentence) {
        this.tokens = tokens;
        this.type = type;
        this.sentence = sentence;
        this.assignHead();
    }

    public void setChannelIdx(final int idx) {
        this.channelIdx = idx;
    }

    public int getChannelIdx() {
        return this.channelIdx;
    }

    public boolean hasHead() {
        return this.hasHead;
    }

    public void assignHead() {
        this.assignHead(false);
    }

    /**
     * Set the value of annotation lemma.
     *
     * @param lemma
     */
    public void setLemma(final String lemma) {
        this.lemma = lemma;
    }

    /**
     * Get the value of annotation lemma.
     *
     * @return
     */
    public String getLemma() {
        if (this.lemma == null) {
            if (this.metadata.containsKey("lemma")) {
                return this.metadata.get("lemma");
            } else {
                return this.getText();
            }
        } else {
            return this.lemma;
        }
    }

    /**
     * Ustaw pewność co do istnienia anotacji.
     *
     * @param confidence
     */
    public void setConfidence(final double confidence) {
        this.confidence = confidence;
    }

    /**
     * Zwraca wartość określającą pewność istnienia anotacji.
     *
     * @return
     */
    public double getConfidence() {
        return this.confidence;
    }

    /**
     * Przypisuje głowę do anotacji na podst. równoległej anotacji, lub jako pierwszy token.
     * Do użytku z anotacjami "anafora_wyznacznik" na potrzeby piśnika TEI
     */
    public void assignHead(final boolean force) {
        if (!force && hasHead()) {
            return;
        }

        int head = -1;
        for (final int i : this.getTokens()) {
            final Token t = this.sentence.getTokens().get(i);
            if (t.getDisambTag().getPos().equals("subst")) {
                head = i;
                break;
            }
        }

        if (head == -1) {
            for (final int i : this.getTokens()) {
                final Token t = this.sentence.getTokens().get(i);
                if (t.getDisambTag().getPos().equals("ign")) {
                    head = i;
                    break;
                }
            }
        }

        if (head == -1) {
            head = this.tokens.first();
        }

        this.setHead(head);
    }

    public Integer getHead() {
        return this.head;
    }

    /**
     * Zwraca token będący głową frazy.
     *
     * @return
     */
    public Token getHeadToken() {
        return this.sentence.getTokens().get(this.head);
    }

    public void setHead(final int idx) {
        hasHead = true;
        head = idx;
    }

    public void addToken(final int idx) {
        tokens.add(idx);
    }

    public void replaceTokens(final int begin, final int end) {
        tokens.clear();
        IntStream.rangeClosed(begin, end).forEach(tokens::add);
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        final Annotation that = (Annotation) object;
        return equalsWithTrueOnNull(getSentence().getId(), that.getSentence().getId())
                && equalsWithTrueOnNull(getId(), that.getId())
                && Objects.equals(getTokens(), that.getTokens())
                && Objects.equals(getType(), that.getType());
    }

    private static boolean equalsWithTrueOnNull(final Object o1, final Object o2) {
        return o1 == null || o2 == null || Objects.equals(o1, o2);
    }

    @Override
    public int hashCode() {
        return (this.getText() + this.tokens.toString() + this.getType() + this.getSentence()).hashCode();
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(final Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getMetadata(final String key) {
        return metadata.get(key);
    }

    public void setMetadata(final String key, final String val) {
        metadata.put(key, val);
    }

    public boolean metaDataMatches(final Annotation other) {
        return this.metadata.equals(other.metadata);
    }

    public boolean metaDataMatchesKey(final String key, final Annotation other) {
        return this.metadata.getOrDefault(key, "none1").equals(other.metadata.getOrDefault(key, "none2"));
    }

    public int getBegin() {
        return this.tokens.first();
    }

    public int getEnd() {
        return this.tokens.last();
    }

    public TreeSet<Integer> getTokens() {
        return this.tokens;
    }

    /**
     * We should rename this function and 'getTokens()'
     *
     * @return
     */
    public List<Token> getTokenTokens() {
        return this.tokens.stream().map(i -> this.sentence.getTokens().get(i)).collect(Collectors.toList());
    }

    public Sentence getSentence() {
        return this.sentence;
    }

    public void setSentence(final Sentence sentence) {
        this.sentence = sentence;
    }

    public String getType() {
        return this.type;
    }

    public String getGroup() {
        return this.group;
    }

    /**
     * Zwraca treść chunku, jako konkatenację wartości pierwszych atrybutów.
     *
     * @return
     */
    public String getText() {
        return this.getText(false);
    }

    /**
     * Zwraca treść chunku, jako konkatenację wartości pierwszych atrybutów.
     *
     * @param markHead Jeżeli true, to głowa anotacji zostanie wypisana w nawiasach klamrowych.
     * @return
     */
    public String getText(final boolean markHead) {
        final List<Token> tokens = this.sentence.getTokens();
        if (tokens == null) {
            return "NO_TOKEN_IN_SENTENCE";
        }
        final StringBuilder text = new StringBuilder();
        for (final int i : this.tokens) {
            final Token token = tokens.get(i);
            if (markHead && this.head == i) {
                text.append("{");
            }
            text.append(token.getOrth());
            if (markHead && this.head == i) {
                text.append("}");
            }
            if ((!token.getNoSpaceAfter()) && (i < getEnd())) {
                text.append(" ");
            }
        }
        return text.toString();
    }

    /**
     * @return
     */
    public String getBaseText() {
        return this.getBaseText(true);
    }

    /**
     * @param includeNs
     * @return
     */
    public String getBaseText(final boolean includeNs) {
        final List<Token> tokens = this.sentence.getTokens();
        final StringBuilder text = new StringBuilder();
        final TokenAttributeIndex index = this.sentence.getAttributeIndex();
        for (final int i : this.tokens) {
            final Token token = tokens.get(i);
            text.append(token.getAttributeValue(index.getIndex("base")));
            final int a = getEnd();
            if ((includeNs == false || !token.getNoSpaceAfter()) && (i < getEnd())) {
                text.append(" ");
            }
        }
        return text.toString();
    }

    /**
     * Return a space-separated sequence of token ctags.
     *
     * @return
     */
    public String getCtags() {
        final List<Token> tokens = this.sentence.getTokens();
        final StringBuilder text = new StringBuilder();
        for (final int i : this.tokens) {
            text.append(tokens.get(i).getDisambTag().getCtag());
            text.append(" ");
        }
        return text.toString().trim();
    }

    /**
     * Return a space-separated sequence of ns values.
     *
     * @return
     */
    public String getNss() {
        final List<Token> tokens = this.sentence.getTokens();
        final StringBuilder text = new StringBuilder();
        for (final int i : this.tokens) {
            text.append(tokens.get(i).getNoSpaceAfter() ? "True" : "False");
            text.append(" ");
        }
        return text.toString().trim();
    }

    public void setType(final String type) {
        this.type = type.toLowerCase();
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public static Annotation[] sortChunks(final Set<Annotation> chunkSet) {
        final int size = chunkSet.size();
        final Annotation[] sorted = new Annotation[size];
        int idx = 0;
        for (final Annotation c : chunkSet) {
            sorted[idx++] = c;
        }
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if ((sorted[i].getBegin() > sorted[j].getBegin()) ||
                        ((sorted[i].getBegin() == sorted[j].getBegin()) &&
                                (sorted[i].getEnd() > sorted[j].getEnd()))) {
                    final Annotation aux = sorted[i];
                    sorted[i] = sorted[j];
                    sorted[j] = aux;
                }
            }
        }
        return sorted;
    }

    @Override
    public String toString() {
        return String.format("[%s:%s]", getText(), getType());
    }

    @Override
    public Annotation clone() {
        final Annotation cloned = new Annotation(getBegin(), getEnd(), getType(), sentence);
        cloned.setId(id);
        cloned.setHead(head);
        cloned.setMetadata(new HashMap<>(this.getMetadata()));
        return cloned;
    }

    public int length() {
        return tokens.size();
    }

    public boolean contains(final Annotation inner) {
        if (inner == null) {
            return false;
        }
        return inner.getBegin() >= getBegin() && inner.getBegin() <= getEnd()
                && inner.getEnd() >= getBegin() && inner.getEnd() <= getEnd();
    }

}
