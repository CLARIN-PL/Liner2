package g419.liner2.api.normalizer.timex;

import g419.liner2.api.normalizer.timex.entities.*;
import g419.liner2.api.normalizer.timex.parsing.DateParser;
import g419.liner2.api.normalizer.timex.parsing.GeneralDateParser;

public class TimexParser {
    DateParser dateParser = new DateParser();

    TimexEntity parse(String lval){
        dateParser.reset();
        dateParser.parse(lval);
        DateEntity date = dateParser.getResult();
        String unparsed = dateParser.getUnparsed();
        return new LValEntity(new DateTimeEntity(date, null), unparsed);
    }

}
