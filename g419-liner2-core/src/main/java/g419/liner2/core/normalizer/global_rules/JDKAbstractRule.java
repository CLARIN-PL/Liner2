package g419.liner2.core.normalizer.global_rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class JDKAbstractRule implements Rule {

  private Logger logger = null;

  Logger getLogger() {
    if (logger == null) {
      logger = LoggerFactory.getLogger(this.getClass());
    }
    return logger;
  }

  boolean needsPrevious() {
    return true;
  }

  protected abstract String doNormalize(String lval, String base, String previous);

  @Override
  public String normalize(String lval, String base, String previous, String first, String creationDate) {
    if (needsPrevious() && previous == null) {
      return null;
    }
    return doNormalize(lval, base, previous);
  }

  static public String[] split(String date) {
    return date.split("[-]");
  }

  static public String year(String date) {
    return split(date)[0];
  }

  static public String month(String date) {
    return split(date)[1];
  }

  static public String day(String date) {
    return split(date)[2];
  }

  static public int toInt(String i) {
    return Integer.parseInt(i);
  }

  static public Calendar fromString(String date) {
    Calendar out = Calendar.getInstance();
    try {
      out.setTime(dateFormat.parse(date));
      addMonths(out, -1);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    return out;
  }

  static private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  static public String toString(Calendar calendar) {
    return dateFormat.format(calendar.getTime().getTime());
  }

  static public Calendar addYears(Calendar original, int years) {
    original.roll(Calendar.YEAR, years);
    return original;
  }

  static public Calendar addMonths(Calendar original, int months) {
    original.roll(Calendar.MONTH, months);
    return original;
  }

  static public Calendar addDays(Calendar original, int days) {
    original.roll(Calendar.DAY_OF_YEAR, days);
    return original;
  }

  static int toJDKWeekday(int weekday) {
//        switch(weekday){
//            case 1: return Calendar.MONDAY;
//            case 2: return Calendar.TUESDAY;
//            case 3: return Calendar.WEDNESDAY;
//            case 4: return Calendar.THURSDAY;
//            case 5: return Calendar.FRIDAY;
//            case 6: return Calendar.SATURDAY;
//            case 7: return Calendar.SUNDAY;
//            default: throw new IllegalArgumentException("I dont know how about you, " +
//                    "but in my world there are only 7 days of week... " +
//                    "I dont understand "+weekday);
//        }
    return (weekday + 7) % 7 + 1;
  }

  static public Calendar setWeekDay(Calendar original, int weekDay) {
    int step = (int) Math.signum(weekDay);
    int jdkWeekday = toJDKWeekday(step * weekDay);
    if (step > 0) {
      if (original.get(Calendar.DAY_OF_WEEK) == jdkWeekday) {
        return original;
      }
    } else {
      addDays(original, -7);
    }
    while (toJDKWeekday(weekDay) < original.get(Calendar.DAY_OF_WEEK)) {
      addDays(original, 1);
    }
    return original;
  }

  static public Calendar addYears(Calendar original, String years) {
    return addYears(original, toInt(years));
  }

  static public Calendar addMonths(Calendar original, String months) {
    return addMonths(original, toInt(months));
  }

  static public Calendar addDays(Calendar original, String days) {
    return addDays(original, toInt(days));
  }

  static public Calendar setWeekDay(Calendar original, String weekDay) {
    return setWeekDay(original, toInt(weekDay));
  }
}
