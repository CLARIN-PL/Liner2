package g419.liner2.core.converter.factory;

import g419.liner2.core.converter.AnnotationFilterByGroupRegexConverter;
import g419.liner2.core.converter.Converter;

import java.util.regex.Pattern;

public class AnnotationFilterByGroupRegexFactoryItem extends ConverterFactoryItem {

    public AnnotationFilterByGroupRegexFactoryItem() {
        super("annotation-filter-by-group-regex:(.*)");
    }

    @Override
    public Converter getConverter() {
        return new AnnotationFilterByGroupRegexConverter(Pattern.compile("^" + matcher.group(1) + "$"));
    }
}
