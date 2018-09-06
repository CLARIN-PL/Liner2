package g419.liner2.core.normalizer.global_rules;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public abstract class AbstractRule implements Rule {

    private Logger logger = null;

     Logger getLogger(){
        if (logger == null)
            logger = LoggerFactory.getLogger(this.getClass());
        return logger;
    }

     boolean needsPrevious(){
        return true;
    }

     protected abstract String doNormalize(String lval, String base, String previous, String first, String creationDate);

    @Override
    public String normalize(String lval, String base, String previous, String first, String creationDate) {
//        if (needsPrevious() && previous == null)
//            return null;
        return doNormalize(lval, base, previous, first, creationDate);
    }

    static public String[] split(String date){
        return date.split("[-]");
    }

    static public String year(String date){
        return split(date)[0];
    }

    static public String month(String date){
        return split(date)[1];
    }

    static public String day(String date){
        return split(date)[2];
    }

    static public int toInt(String i){
        try {
            return Integer.parseInt(i);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static public LocalDate fromString(String date){
        return LocalDate.parse(date);
    }

    static private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    static public String toString(LocalDate calendar){
        return calendar.toString();
    }

    static public LocalDate addYears(LocalDate original, int years){
        return original.plusYears(years);
    }

    static public LocalDate addMonths(LocalDate original, int months){
        return original.plusMonths(months);
    }

    static public LocalDate addDays(LocalDate original, int days){
        return original.plusDays(days);
    }

    static public LocalDate closestWeekday(LocalDate original, int weekDay){
        LocalDate plus = setWeekDay(original, weekDay);
        LocalDate minus = setWeekDay(original, -1*weekDay);
        if (plus.toDate().getTime() - original.toDate().getTime() < original.toDate().getTime() - minus.toDate().getTime() )
            return plus;
        return minus;
    }

    static public LocalDate setWeekDay(LocalDate original, int weekDay){
        LocalDate out = original.withDayOfWeek(Math.abs(weekDay));
        if (Math.signum(weekDay) > 0){
            while (out.compareTo(original)<=0)
                out = out.plusDays(7);
        } else if (Math.signum(weekDay) < 0){
            while (out.compareTo(original)>0)
                out = out.minusDays(7);
        }
        return out;
    }

    static public LocalDate addYears(LocalDate original, String years){
        return addYears(original, toInt(years));
    }

    static public LocalDate addMonths(LocalDate original, String months){
        return addMonths(original, toInt(months));
    }

    static public LocalDate addDays(LocalDate original, String days){
        return addDays(original, toInt(days));
    }

    static public LocalDate setWeekDay(LocalDate original, String weekDay){
        return setWeekDay(original, toInt(weekDay));
    }

    static public LocalDate closestWeekday(LocalDate original, String weekDay){
        return closestWeekday(original, toInt(weekDay));
    }

    static public String firstNotNull(String... objs){
        for (String o: objs){
            if (o!=null)
                return o;
        }
        throw new RuntimeException("Need at least one not-null!");
    }
}
