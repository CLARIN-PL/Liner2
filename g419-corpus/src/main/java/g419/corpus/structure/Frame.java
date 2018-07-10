package g419.corpus.structure;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Struktura reprezentuje zbiór obiektów, gdzie każdy z obiektów posiada określoną rolę i zbiór atrybutów
 *
 * @author czuk
 */
public class Frame<T> {

    final String type;
    //final Map<String, String> attributes = Maps.newHashMap();
    final Map<String, T> slots = Maps.newHashMap();
    final Map<String, Map<String, String>> slotsAttributes = Maps.newHashMap();

    public Frame(final String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public T getSlot(final String name) {
        return slots.get(name);
    }

    public Map<String, T> getSlots() {
        return slots;
    }

    public boolean has(final String name) {
        return slots.containsKey(name);
    }

    public void set(final String name, final T annotation) {
        slots.put(name, annotation);
    }

    public void setSlotAttribute(final String slot, final String attribute, final String value) {
        slotsAttributes
                .computeIfAbsent(slot, p -> Maps.newHashMap())
                .put(attribute, value);
    }

    public Map<String, String> getSlotAttributes(final String slot) {
        return slotsAttributes.computeIfAbsent(slot, p -> Maps.newHashMap());
    }

    public Map<String, Map<String, String>> getSlotAttributes() {
        return slotsAttributes;
    }

    @Override
    public String toString() {
        return "Frame{" +
                "type='" + type + '\'' +
                ", slots=" + slots +
                ", slotsAttributes=" + slotsAttributes +
                '}';
    }
}
