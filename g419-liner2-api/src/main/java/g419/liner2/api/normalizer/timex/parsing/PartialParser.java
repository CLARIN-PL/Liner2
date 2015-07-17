package g419.liner2.api.normalizer.timex.parsing;

import g419.liner2.api.normalizer.timex.entities.TimexEntity;

public interface PartialParser<T extends TimexEntity> {
    void parse(String toParse);
    T getResult();
    String getUnparsed();
    void reset();
}
