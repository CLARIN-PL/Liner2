package g419.liner2.core.converter.factory;

import g419.liner2.core.converter.AnnotationMappingConverter;
import g419.liner2.core.converter.Converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by michal on 9/24/14.
 */
public class AnnotationMappingFactoryItem extends ConverterFactoryItem {

    public AnnotationMappingFactoryItem() {super("annotation-mapping:(.*\\.txt)");}

    @Override
    public Converter getConverter() {
        return new AnnotationMappingConverter(matcher.group(1));
    }
}
