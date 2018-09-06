package g419.corpus.structure;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Klasa reprezentuje indeks atrybutów będący mapowaniem nazwy atrybutu na unikalny indeks.
 *
 * @author czuk
 */
public class TokenAttributeIndex {

    /**
     * Tablica zawiera nazwy atrybutów. Pozycja, na której znajduje się dany atrybut
     * jest indeksem tego atrybutu w tablicy atrybutów (klasa Token).
     */
    final List<String> indexes = Lists.newArrayList();

    final Map<String, Integer> nameToIndex = Maps.newHashMap();

    public TokenAttributeIndex with(final String name) {
        addAttribute(name);
        return this;
    }

    /**
     * TODO
     * Dodaje nowy atrybut do indeksu i zwraca jego numer porządkowy (indeks).
     *
     * @param name -- unikalna nazwa atrybutu
     * @return
     */
    public int addAttribute(final String name) {
        if (!nameToIndex.containsKey(name)) {
            indexes.add(name);
            nameToIndex.put(name, indexes.size() - 1);
            return indexes.size() - 1;
        } else {
            return nameToIndex.get(name);
        }
    }

    public List<String> getAttributes() {
        return Collections.unmodifiableList(indexes);
    }

    /**
     * Porównuje z innym obiektem tej klasy.
     */
    public boolean equals(final TokenAttributeIndex ai) {
        if (indexes.size() != ai.getLength()) {
            return false;
        }
        for (int i = 0; i < indexes.size(); i++) {
            if (ai.getIndex(indexes.get(i)) != i) {
                return false;
            }
        }
        return true;
    }

    /**
     * Dodaje listę atrybutów pomijając już zadeklarowane.
     *
     * @param features
     */
    public void update(final List<String> features) {
        features.forEach(this::addAttribute);
    }

    /**
     * Zwraca numer porządkowy atrybutu o danej nazwie.
     *
     * @param name
     * @return
     */
    public int getIndex(final String name) {
        return nameToIndex.containsKey(name) ? nameToIndex.get(name) : -1;
    }

    /**
     * Return name of an attribute for given index.
     *
     * @param index -- attribute index in the token feature vector
     * @return name of the attribute
     */
    public String getName(final int index) {
        return indexes.get(index);
    }

    /**
     * Return number of declared attributes.
     *
     * @return number of attributes
     */
    public int getLength() {
        return indexes.size();
    }

    /**
     * @param token
     * @param attributeName
     * @return
     */
    public String getAttributeValue(final Token token, final String attributeName) {
        return nameToIndex.containsKey(attributeName) ? token.getAttributeValue(nameToIndex.get(attributeName)) : null;
    }

    public List<String> allAtributes() {
        return indexes;
    }

    @Override
    public TokenAttributeIndex clone() {
        final TokenAttributeIndex index = new TokenAttributeIndex();
        index.update(indexes);
        return index;
    }
}
