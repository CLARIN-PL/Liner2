package g419.liner2.api.converter.factory;

import g419.liner2.api.converter.Converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by michal on 9/24/14.
 */
abstract public class ConverterFactoryItem {

    protected Pattern pattern = null;
    protected Matcher matcher;

    public ConverterFactoryItem(String stringPattern){
        pattern = Pattern.compile("^"+stringPattern+"$");

    }

    public boolean matchPattern(String description){
       matcher = pattern.matcher(description);
       return matcher.find();
    }

    abstract public Converter getConverter();
}
