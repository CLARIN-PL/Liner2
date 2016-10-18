package g419.liner2.api.normalizer.timex.parsing;

import g419.liner2.api.normalizer.timex.entities.GeneralDateEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneralDateParser extends AbstractPartialParser<GeneralDateEntity>{


    protected static final String oneCharacter = "[0-9x]";
    protected static final String yearRgx = oneCharacter+"{0,4}";
    protected static final String monthRgx = oneCharacter+"{2}";
    protected static final String dayRgx = oneCharacter+"{2}";
    protected static final String summaryRgx = "(?<year>"+yearRgx+")([-](?<month>"+monthRgx+")([-](?<day>"+dayRgx+"))?)?(?<unparsed>.*)";
    protected static final Pattern pattern = Pattern.compile(summaryRgx);

    @Override
    public void parse(String toParse){
        Matcher matcher = pattern.matcher(toParse);
        if (matcher.matches()) {
            result.setYear(silentGroup(matcher, "year"));
            result.setMonth(silentGroup(matcher, "month"));
            result.setDay(silentGroup(matcher, "day"));
            unparsed = silentGroup(matcher, "unparsed");
        }
    }

    @Override
    GeneralDateEntity getNewInstance() {
        return new GeneralDateEntity();
    }

}
