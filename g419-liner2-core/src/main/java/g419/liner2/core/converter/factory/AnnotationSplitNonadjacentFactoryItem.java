package g419.liner2.core.converter.factory;

import g419.liner2.core.converter.AnnotationSplitNonadjacent;
import g419.liner2.core.converter.Converter;

public class AnnotationSplitNonadjacentFactoryItem extends ConverterFactoryItem {

    public AnnotationSplitNonadjacentFactoryItem() {
        super("annotation-split-nonadjacent");
    }

    @Override
    public Converter getConverter() {
        return new AnnotationSplitNonadjacent();
    }
}
