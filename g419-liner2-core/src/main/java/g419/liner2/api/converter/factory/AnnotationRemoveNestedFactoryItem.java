package g419.liner2.api.converter.factory;

import g419.liner2.api.converter.AnnotationRemoveNestedConverter;
import g419.liner2.api.converter.Converter;

import java.util.regex.Matcher;

/**
 * Created by michal on 9/25/14.
 */
public class AnnotationRemoveNestedFactoryItem extends ConverterFactoryItem {

    public AnnotationRemoveNestedFactoryItem(){super("annotation-remove-nested");}


    @Override
    public Converter getConverter() {
        return new AnnotationRemoveNestedConverter();
    }
}
