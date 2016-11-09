package g419.liner2.api.normalizer.timex.parsing;

import g419.liner2.api.normalizer.timex.entities.TimexEntity;

import java.util.regex.Matcher;

public abstract class AbstractPartialParser<T extends TimexEntity> implements PartialParser<T>{
    protected T result;
    protected String unparsed;

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public String getUnparsed() {
        return unparsed;
    }

    abstract T getNewInstance();

    @Override
    public void reset() {
        result = getNewInstance();
        unparsed = null;
    }

    static String silentGroup(Matcher matcher, String groupName){
        try {
            return matcher.group(groupName);
        } catch (IllegalArgumentException ignored){
            return null;
        }
    }
}
