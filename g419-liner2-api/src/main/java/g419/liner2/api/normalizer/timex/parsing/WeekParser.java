package g419.liner2.api.normalizer.timex.parsing;

import g419.liner2.api.normalizer.timex.entities.WeekEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeekParser extends AbstractPartialParser<WeekEntity> {
    protected final String yearRgx = "[x0-9]{0,4}";
    protected final String weekNoRgx = "[x0-9]{2}";
    protected final String weedDayRgx = "[x0-9]";
    protected final Pattern pattern = Pattern.compile(
            "(?<year>"+yearRgx+")?W(?<weekNo>"+weekNoRgx+")([-](?<weekDay>"+weedDayRgx+"))?(?<unparsed>.*)"
    );

    @Override
    WeekEntity getNewInstance() {
        return new WeekEntity();
    }


    @Override
    public void parse(String toParse) {
        Matcher matcher = pattern.matcher(toParse);
        if (matcher.matches()){
            result.setYear(silentGroup(matcher, "year"));
            result.setWeekNumber(silentGroup(matcher, "weekNo"));
            result.setDayOfWeek(silentGroup(matcher, "weekDay"));
            unparsed = silentGroup(matcher, "unparsed");
        } else {
            unparsed = toParse;
        }
    }
}
