package g419.liner2.api.normalizer.timex.parsing;

import g419.liner2.api.normalizer.timex.entities.DateEntity;

import java.util.Arrays;
import java.util.List;

public class DateParser extends AbstractPartialParser<DateEntity>{
    protected List<? extends PartialParser<? extends DateEntity>> subparsers = Arrays.asList(
            new GeneralDateParser(),
            new WeekParser()
    );

    @Override
    DateEntity getNewInstance() {
        return null;
    }

    @Override
    public void parse(String toParse) {
        unparsed = toParse;
        for (PartialParser<? extends DateEntity> parser: subparsers){
            parser.reset();
            parser.parse(toParse);
            if (!parser.getUnparsed().equals(toParse)){
                result = parser.getResult();
                unparsed = parser.getUnparsed();
                break;
            }
        }
    }
}
