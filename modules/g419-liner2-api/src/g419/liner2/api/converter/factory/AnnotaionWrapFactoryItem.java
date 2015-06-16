package g419.liner2.api.converter.factory;

import g419.liner2.api.converter.AnnotationWrapConverter;
import g419.liner2.api.converter.Converter;

/**
 * Created by michal on 2/20/15.
 */
public class AnnotaionWrapFactoryItem extends ConverterFactoryItem {

    public AnnotaionWrapFactoryItem(){super("wrap-annotations:(.*\\.txt)");}
    @Override
    public Converter getConverter() {
        return new AnnotationWrapConverter(matcher.group(1));
    }
}
