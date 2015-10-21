package g419.liner2.api.normalizer.global_rules;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

public class AbstractRuleTest{

    LocalDate date(int y, int m, int d){
        return new LocalDate(y, m, d);
    }

//    void assertEquals(Loc){
//        int y1 = calendar1.get(Calendar.YEAR);
//        int y2 = calendar2.get(Calendar.YEAR);
//
//        int m1 = calendar1.get(Calendar.MONTH);
//        int m2 = calendar2.get(Calendar.MONTH);
//
//        int d1 = calendar1.get(Calendar.DAY_OF_MONTH);
//        int d2 = calendar2.get(Calendar.DAY_OF_MONTH);
//
//        String msg = ""+y1+" "+(y1 == y2 ? "=" : "!=")+" "+y2;
//        msg += " ; "+m1+" "+(m1 == m2 ? "=" : "!=")+" "+m2;
//        msg += " ; "+d1+" "+(d1 == d2 ? "=" : "!=")+" "+d2;
//
//        assertTrue(
//                msg,
//                y1 == y2 && m1 == m2 && d1 == d2
//        );
//    }

    @Test
    public void testSetWeekDay() throws Exception {
        LocalDate nextMonday = date(2015, 6, 22);
        LocalDate lastMonday = date(2015, 6, 15);
        LocalDate nextWednesday = date(2015, 6, 24);
        LocalDate lastWednesday = date(2015, 6, 17);
        assertEquals(
                nextMonday,
                AbstractRule.setWeekDay(
                        date(2015, 6, 17),
                        1
                )
        );
        assertEquals(
                lastMonday,
                AbstractRule.setWeekDay(
                        date(2015, 6, 17),
                        -1
                )
        );
        assertEquals(
                nextWednesday,
                AbstractRule.setWeekDay(
                        date(2015, 6, 17),
                        3
                )
        );
        assertEquals(
                lastWednesday,
                AbstractRule.setWeekDay(
                        date(2015, 6, 17),
                        -3
                )
        );
    }

    @Test
    public void testFirstNotNull(){
        String a = "a";
        assert AbstractRule.firstNotNull(a, null) == a;
        assert AbstractRule.firstNotNull(null, a) == a;
    }
}