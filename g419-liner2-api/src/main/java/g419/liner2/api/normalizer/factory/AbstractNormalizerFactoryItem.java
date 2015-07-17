package g419.liner2.api.normalizer.factory;

import g419.liner2.api.chunker.factory.ChunkerFactoryItem;
import org.ini4j.Ini;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

abstract public class AbstractNormalizerFactoryItem extends ChunkerFactoryItem {
    public AbstractNormalizerFactoryItem(String chunkerType) {
        super(chunkerType);
    }

    protected List<Pattern> getTypePatterns(Ini.Section description){
        return getTypePatterns("normalizedTypes", description);
    }

    protected List<Pattern> getTypePatterns(String section, Ini.Section description){
        String normalizedTypes = description.get("normalizedTypes");
        List<String> uncompiledTypesPatterns = Arrays.asList(normalizedTypes.split("[;]"));
        List<Pattern> typePatterns = new ArrayList<>();
        for (String uncompiled: uncompiledTypesPatterns)
            typePatterns.add(Pattern.compile(uncompiled));
        return typePatterns;
    }
}
