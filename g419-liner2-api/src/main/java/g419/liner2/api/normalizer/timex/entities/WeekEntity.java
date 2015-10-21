package g419.liner2.api.normalizer.timex.entities;

import java.util.regex.Pattern;

public class WeekEntity extends DateEntity{
    protected String weekNumber;
    //todo: add support for weekend ...Wxx-WE
    protected String dayOfWeek;

    public WeekEntity() {
    }

    public WeekEntity(String weekNumber, String dayOfWeek) {
        this.weekNumber = weekNumber;
        this.dayOfWeek = dayOfWeek;
    }

    @Override
    boolean isSpecified() {
        return weekNumber!=null && dayOfWeek !=null;
    }

    @Override
    boolean isKnown() {
        return true;
    }

    @Override
    public String toTimex() {
        String out = "";
        if (weekNumber!=null){
            out += "W"+weekNumber;
            if (dayOfWeek !=null){
                out+= "-"+ dayOfWeek;
            }
        } else {
            if (dayOfWeek !=null){
                out += "Wxx-"+ dayOfWeek;
            }
        }
        return out;
    }

    public WeekEntity fill(WeekEntity general) {
        return new WeekEntity(
            general.weekNumber != null ? overwrite(weekNumber, general.weekNumber, 'x') : weekNumber,
            general.dayOfWeek != null ? overwrite(dayOfWeek, general.dayOfWeek, 'x') : dayOfWeek
        );
    }

    @Override
    public TimexEntity fill(TimexEntity general) {
        return fill((WeekEntity) general);
    }

    @Override
    public TimexEntity copy() {
        return new WeekEntity(weekNumber, dayOfWeek);
    }

    public String getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(String weekNumber) {
        throwUp(weekNumber!=null && !Pattern.matches("xx|0[1-9]|[1-4][1-9]|5[012]", weekNumber),
                "Week number should match '0[1-9]|[1-4][1-9]|5[012]', be null or 'xx'! Got '"+weekNumber+"' instead!");
        this.weekNumber = weekNumber;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        throwUp(dayOfWeek !=null && !Pattern.matches("x|[1-7]", dayOfWeek),
                "Day of week should match '[1-7]', be null or 'x'! Got '"+dayOfWeek+"' instead!");
        this.dayOfWeek = dayOfWeek;
    }
}
