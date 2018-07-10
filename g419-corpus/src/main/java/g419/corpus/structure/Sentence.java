package g419.corpus.structure;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;


/**
 * Reprezentuje zdanie jako sekwencję tokenów i zbiór anotacji.
 *
 * @author czuk
 */
public class Sentence extends IdentifiableElement {

    /* Indeks nazw atrybutów */
    TokenAttributeIndex attributeIndex = null;

    /* Sekwencja tokenów wchodzących w skład zdania */
    List<Token> tokens = new ArrayList<>();

    /* Zbiór anotacji */
    LinkedHashSet<Annotation> chunks = new LinkedHashSet<>();

    /* Tymczasowe obejście braku odniesienia do dokumentu z poziomu klasy Annotation */
    Document document;

    /* Paragraf w którym jest zdanie*/
    Paragraph paragraph;

    private static final Comparator<Annotation> annotationComparator = new Comparator<Annotation>() {
        @Override
        public int compare(final Annotation a, final Annotation b) {
            if (a.getTokens().size() == b.getTokens().size()) {
                return String.CASE_INSENSITIVE_ORDER.compare(a.getType(), b.getType());
            }

            return Integer.signum(b.getTokens().size() - a.getTokens().size());
        }
    };

    public Sentence() {
    }

    public Sentence(final TokenAttributeIndex attrIndex) {
        attributeIndex = attrIndex;
    }

    public void addChunk(final Annotation chunk) {
        chunk.setSentence(this);
        chunks.add(chunk);
    }

    public void addAnnotations(final AnnotationSet chunking) {
        if (chunking != null) {
            for (final Annotation chunk : chunking.chunkSet()) {
                addChunk(chunk);
            }
        }
    }

    public void addToken(final Token token) {
        tokens.add(token);
    }

    /**
     * Zwraca pozycję zdania w dokumencie.
     *
     * @return
     */
    public int getOrd() {
        if (document != null) {
            return document.getSentences().indexOf(this);
        } else {
            return -1;
        }
    }

    /**
     * Return true if the sentence has an assigned identifier.
     *
     * @return True if the sentence identifier is set.
     */
    public boolean hasId() {
        return id != null;
    }

    /**
     * Return a list of annotations which contain a token with given index.
     */
    public List<Annotation> getChunksAt(final int idx) {
        final List<Annotation> returning = new ArrayList<>();
        final Iterator<Annotation> i_chunk = chunks.iterator();
        while (i_chunk.hasNext()) {
            final Annotation currentChunk = i_chunk.next();
            if (currentChunk.getTokens().contains(idx)) {
                returning.add(currentChunk);
            }
        }
        return returning;
    }

    /**
     * Return a list of annotations which contain a token with given index.
     */
    public List<Annotation> getChunksAt(final int idx, final List<Pattern> types) {
        final List<Annotation> returning = new ArrayList<>();
        final Iterator<Annotation> i_chunk = chunks.iterator();
        while (i_chunk.hasNext()) {
            final Annotation currentChunk = i_chunk.next();
            if (currentChunk.getTokens().contains(idx)) {
                if (types != null) {
                    for (final Pattern patt : types) {
                        if (patt.matcher(currentChunk.getType()).matches()) {
                            returning.add(currentChunk);
                            break;
                        }
                    }
                } else {
                    returning.add(currentChunk);
                }
            }
        }
        return returning;
    }

    /*
    Sprawdza, czy token o podanym indeksie jest chunkiem typu 'type'
     */
    public boolean isChunkAt(final int idx, final String type) {
        final Iterator<Annotation> i_chunk = chunks.iterator();
        while (i_chunk.hasNext()) {
            final Annotation currentChunk = i_chunk.next();
            if (currentChunk.getTokens().contains(idx) && currentChunk.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param idx
     * @param types
     * @param sorted
     * @return
     */
    public List<Annotation> getChunksAt(final int idx, final List<Pattern> types, final boolean sorted) {
        final List<Annotation> result = getChunksAt(idx, types);
        if (sorted) {
            sortTokenAnnotations(result);
        }
        return result;
    }

    /**
     * @param tokenIdx
     * @return
     */
    public String getTokenClassLabel(final int tokenIdx) {
        final List<Annotation> tokenAnnotations = getChunksAt(tokenIdx);

        if (tokenAnnotations.isEmpty()) {
            return "O";
        } else {
            final List<String> classLabels = new ArrayList<>();
            sortTokenAnnotations(tokenAnnotations);
            for (final Annotation ann : tokenAnnotations) {
                String classLabel = "";
                if (ann.getBegin() == tokenIdx) {
                    classLabel += "B-";
                } else {
                    classLabel += "I-";
                }
                classLabel += ann.getType();
                classLabels.add(classLabel);
            }
            return StringUtils.join(classLabels, "#");
        }
    }


    public String getTokenClassLabel(final int tokenIdx, final List<Pattern> types) {
        final List<Annotation> tokenAnnotations = getChunksAt(tokenIdx, types, true);

        if (tokenAnnotations.isEmpty()) {
            return "O";
        } else {
            final ArrayList<String> classLabels = new ArrayList<>();
            sortTokenAnnotations(tokenAnnotations);
            for (final Annotation ann : tokenAnnotations) {
                String classLabel = "";
                if (ann.getBegin() == tokenIdx) {
                    classLabel += "B-";
                } else {
                    classLabel += "I-";
                }
                classLabel += ann.getType();
                classLabels.add(classLabel);

            }
            return StringUtils.join(classLabels, "#");
        }

    }

    private void sortTokenAnnotations(final List<Annotation> tokenAnnotations) {
        Collections.sort(tokenAnnotations, annotationComparator);
    }

    /**
     * Return a set of annotations with a type matching the pattern `type`.
     *
     * @param type Pattern of annotation type.
     * @return Set of annotations.
     */
    public LinkedHashSet<Annotation> getAnnotations(final Pattern type) {
        final LinkedHashSet<Annotation> annotationsForTypes = new LinkedHashSet<>();
        for (final Annotation annotation : chunks) {
            if (type.matcher(annotation.getType()).find()) {
                annotationsForTypes.add(annotation);
            }
        }

        return annotationsForTypes;
    }

    public LinkedHashSet<Annotation> getAnnotations(final String type) {
        final LinkedHashSet<Annotation> annotationsForTypes = new LinkedHashSet<>();
        for (final Annotation annotation : chunks) {
            if (type.equals(annotation.getType())) {
                annotationsForTypes.add(annotation);
            }
        }

        return annotationsForTypes;
    }

    public LinkedHashSet<Annotation> getAnnotations(final List<Pattern> types) {
        final LinkedHashSet<Annotation> annotationsForTypes = new LinkedHashSet<>();
        for (final Annotation annotation : chunks) {
            for (final Pattern type : types) {
                if (type.matcher(annotation.getType()).find()) {
                    annotationsForTypes.add(annotation);
                }
            }
        }

        return annotationsForTypes;
    }

    /**
     * Return a set of all annotations assigned to the sentence.
     *
     * @return Set of all annotations.
     */
    public LinkedHashSet<Annotation> getChunks() {
        return chunks;
    }

    public int getAttributeIndexLength() {
        return attributeIndex.getLength();
    }

    public TokenAttributeIndex getAttributeIndex() {
        return attributeIndex;
    }

    /**
     * Zwraca ilość tokenów.
     */
    public int getTokenNumber() {
        return tokens.size();
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setAttributeIndex(final TokenAttributeIndex attributeIndex) {
        this.attributeIndex = attributeIndex;
        for (final Token t : tokens) {
            t.setAttributeIndex(attributeIndex);
        }
    }

    public void setAnnotations(final AnnotationSet chunking) {
        chunks = chunking.chunkSet();
    }

    public String annotationsToString() {
        final StringBuilder output = new StringBuilder();
        for (final Annotation chunk : chunks) {
            output.append(chunk.getType() + " | " + chunk.getText() + "\n");
        }
        return output.toString();
    }

    public void removeAnnotations(final String annotation) {
        final Set<Annotation> toRemove = new HashSet<>();
        for (final Annotation an : chunks) {
            if (an.getType().equals(annotation)) {
                toRemove.add(an);
            }
        }
        chunks.removeAll(toRemove);
    }

    public Annotation getAnnotationInChannel(final String channelName, final int annotationIdx) {
        for (final Annotation annotation : chunks) {
            if (annotation.getType().equalsIgnoreCase(channelName) && annotation.getChannelIdx() == annotationIdx) {
                return annotation;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final Token t : tokens) {
            sb.append(t.getOrth());
            sb.append(t.getNoSpaceAfter() ? "" : " ");
        }
        return sb.toString().trim();
    }

    public String toBaseString() {
        final StringBuilder sb = new StringBuilder();
        for (final Token t : tokens) {
            sb.append(t.getAttributeValue("base"));
            sb.append(t.getNoSpaceAfter() ? "" : " ");
        }
        return sb.toString().trim();
    }

    public void setTokens(final List<Token> newTokens) {
        tokens = newTokens;
    }

    @Override
    public Sentence clone() {
        final Sentence copy = new Sentence();
        copy.attributeIndex = attributeIndex.clone();
        copy.setId(getId());
        for (final Token t : tokens) {
            final Token newT = t.clone();
            newT.attrIdx = copy.attributeIndex;
            copy.addToken(newT);
        }
        for (final Annotation a : chunks) {
            copy.addChunk(a.clone());
        }
        return copy;
    }

    public void setDocument(final Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    public void setParagraph(final Paragraph p) {
        paragraph = p;
    }

    public Paragraph getParagraph() {
        return paragraph;
    }

}
