package g419.liner2.api.normalizer.timex.entities;

import java.util.regex.Pattern;

public class GeneralTimeEntity extends TimeEntity{
    protected String hours;
    protected String minutes;
    protected String seconds;

    public GeneralTimeEntity() {
    }

    public GeneralTimeEntity(String hours, String minutes, String seconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    @Override
    boolean isSpecified() {
        return hours!=null && minutes!=null;
    }

    @Override
    boolean isKnown() {
        return true;
    }

    @Override
    public String toTimex() {
        String out = "";
        if (hours!=null){
            out += timeSeparator+hours;
            if (minutes!=null){
                out+=":"+minutes;
                if (seconds!=null){
                    out += ":"+seconds;
                }
            }
        }
        return out;
    }

    @Override
    public TimexEntity fill(TimexEntity general) {
        return fill((GeneralTimeEntity) general);
    }

    public TimexEntity fill(GeneralTimeEntity general) {
        GeneralTimeEntity out = (GeneralTimeEntity) copy();
        if (general.hours!=null) {
            out.hours = overwrite(out.hours, general.hours, 'x');
            if (general.minutes!=null) {
                out.minutes = overwrite(out.minutes, general.minutes, 'x');
                if (general.seconds!=null)
                    out.seconds = overwrite(out.seconds, general.seconds, 'x');
            }
        }
        return out;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        throwUp(hours != null && !(hours.equals("xx") || Pattern.matches("[012]\\d]", hours)),
                "Hours have to match '[012]\\d', be null or equal to 'xx! Got '"+hours+"' instead!");
        this.hours = hours;
    }

    public String getMinutes() {
        return minutes;
    }

    public void setMinutes(String minutes) {
        throwUp(minutes != null && !(minutes.equals("xx") || Pattern.matches("[0-5]\\d]", minutes)),
                "Minutes have to match '[0-5]\\d', be null or equal to 'xx Got '"+minutes+"' instead!");
        this.minutes = minutes;
    }

    public String getSeconds() {
        return seconds;
    }

    public void setSeconds(String seconds) {
        throwUp(seconds != null && !(seconds.equals("xx") || Pattern.matches("[0-5]\\d]", seconds)),
                "Seconds have to match '[0-5]\\d', be null or equal to 'xx Got '"+seconds+"' instead!");
        this.seconds = seconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneralTimeEntity that = (GeneralTimeEntity) o;

        if (hours != null ? !hours.equals(that.hours) : that.hours != null) return false;
        if (minutes != null ? !minutes.equals(that.minutes) : that.minutes != null) return false;
        if (seconds != null ? !seconds.equals(that.seconds) : that.seconds != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = hours != null ? hours.hashCode() : 0;
        result = 31 * result + (minutes != null ? minutes.hashCode() : 0);
        result = 31 * result + (seconds != null ? seconds.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClockEntity{" +
                "hours='" + hours + '\'' +
                ", minutes='" + minutes + '\'' +
                ", seconds='" + seconds + '\'' +
                '}';
    }

    @Override
    public TimexEntity copy(){
        return new GeneralTimeEntity(hours, minutes, seconds);
    }
}
