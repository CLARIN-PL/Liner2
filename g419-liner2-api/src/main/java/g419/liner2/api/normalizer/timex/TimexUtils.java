package g419.liner2.api.normalizer.timex;

import g419.liner2.api.normalizer.timex.entities.TimexEntity;

public class TimexUtils {
    static TimexParser parser = new TimexParser();

    public static TimexEntity parse(String txt){
        return parser.parse(txt);
    }

    public static String fill(String toFill, String general){
        return parser.parse(toFill).fill(parser.parse(general)).toTimex();
    }
}
