package g419.liner2.api.normalizer.timex.entities;

import java.util.regex.Pattern;

public class GeneralDateEntity extends DateEntity {
    protected String month;
    protected String day;

    public GeneralDateEntity() {
    }

    public GeneralDateEntity(String year, String month, String day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    @Override
    public String toTimex() {
        StringBuffer out = new StringBuffer();
        if (year!=null){
            out.append(year);
            if (month!=null) {
                out.append("-"+month);
                if (day!=null){
                    out.append("-"+day);
                }
            }
        }
        return out.toString();
    }

    public GeneralDateEntity fill(GeneralDateEntity general) {
        GeneralDateEntity out = (GeneralDateEntity) copy();
        if (general.year!=null) {
            out.year = overwrite(out.year, general.year, 'x');
            if (general.month!=null) {
                out.month = overwrite(out.month, general.month, 'x');
                if (general.day!=null)
                    out.day = overwrite(out.day, general.day, 'x');
            }
        }
        return out;
    }

    @Override
    public TimexEntity fill(TimexEntity general) {
        return fill((GeneralDateEntity) general);
    }

    @Override
    boolean isSpecified() {
        return month == null || day == null;
    }

    @Override
    boolean isKnown() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneralDateEntity that = (GeneralDateEntity) o;

        if (day != null ? !day.equals(that.day) : that.day != null) return false;
        if (month != null ? !month.equals(that.month) : that.month != null) return false;
        if (year != null ? !year.equals(that.year) : that.year != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = year != null ? year.hashCode() : 0;
        result = 31 * result + (month != null ? month.hashCode() : 0);
        result = 31 * result + (day != null ? day.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DateEntity{" +
                "year='" + year + '\'' +
                ", month='" + month + '\'' +
                ", day='" + day + '\'' +
                '}';
    }

    @Override
    public TimexEntity copy() {
        return new GeneralDateEntity(year, month, day);
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        throwUp(month!=null && !Pattern.matches("xx|0[1-9]|1[012]", month),
                "Month should match '0[1-9]|1[012]', be null or 'xx'! Got '"+month+"' instead!");
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        throwUp(day!=null && !Pattern.matches("xx|0[1-9]|[12][0-9]|3[01]", day),
                "Month should match '0[1-9]|[12][0-9]|3[01]', be null or 'xx'! Got '"+day+"' instead!");
        this.day = day;
    }
}
