package g419.liner2.core.tools.parser;

import com.google.common.collect.Lists;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MaltSentence {
    List<MaltSentenceLink> links = Lists.newArrayList();
    private Map<String, String> posMapping;
    private String[] maltData;
    private Set<Annotation> annotations;
    private Sentence sentence;

    public MaltSentence(final Sentence sent, final Map<String, String> posMapping) {
        this.posMapping = posMapping;
        maltData = convertToCoNLL(sent).stream()
                .map(r -> String.join("\t", Arrays.asList(r)))
                .toArray(String[]::new);
        annotations = sent.getChunks();
        sentence = sent;
    }

    public void setMaltDataAndLinks(final String[] output) {
        maltData = output;
        links = IntStream.range(0, output.length)
                .mapToObj(i -> new ImmutablePair<>(i, output[i].split("\t")))
                .map(p -> new MaltSentenceLink(p.getKey(), Integer.valueOf(p.getRight()[8])-1, p.getRight()[9]))
                .collect(Collectors.toList());
    }

    public String[] getMaltData() {
        return maltData;
    }

    public Sentence getSentence() {
        return this.sentence;
    }

    public MaltSentenceLink getLink(final int index) {
        return index >= this.links.size() ? null : this.links.get(index);
    }

    /**
     * Zwraca listę linków wskazujących na token o wskazanym indeksie.
     *
     * @param index
     * @return
     */
    public List<MaltSentenceLink> getLinksByTargetIndex(final int index) {
        return links.stream().filter(link -> link.getTargetIndex() == index).collect(Collectors.toList());
    }

    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    private List<String[]> convertToCoNLL(final Sentence sent) {
        List<String[]> tokens = Lists.newArrayList();
        ListIterator<Token> it = sent.getTokens().listIterator();
        TokenAttributeIndex attributes = sent.getAttributeIndex();
        while (it.hasNext()) {
            Token token = it.next();
            String ctag = token.getAttributeValue(attributes.getIndex("ctag"));
            List<String> ctag_elements = Arrays.asList(ctag.split(":"));

            String[] tokData = Stream.generate(() -> "_").limit(8).toArray(String[]::new);
            tokData[0] = String.valueOf(it.nextIndex());
            tokData[1] = token.getAttributeValue(attributes.getIndex("orth"));
            tokData[2] = token.getAttributeValue(attributes.getIndex("base"));
            tokData[3] = posMapping.get(ctag_elements.get(0));
            tokData[4] = ctag_elements.get(0);
            tokData[5] = getDefaultIfEmpty(String.join("|", ctag_elements.subList(1, ctag_elements.size())));
            tokens.add(tokData);
        }
        return tokens;
    }

    private static String getDefaultIfEmpty(String str) {
        return str == null || str.isEmpty() ? "_" : str;
    }
}
