package g419.liner2.core.converter.factory;

import g419.liner2.core.converter.AnnotationFilterByRegexConverter;
import g419.liner2.core.converter.Converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by michal on 9/25/14.
 */
public class AnnotationFilterByRegexFactoryItem extends ConverterFactoryItem{

    public AnnotationFilterByRegexFactoryItem() {super("annotation-filter-by-regex:(.*)");}

    @Override
    public Converter getConverter() {
        return new AnnotationFilterByRegexConverter(Pattern.compile("^" + matcher.group(1) + "$"));
    }
}
