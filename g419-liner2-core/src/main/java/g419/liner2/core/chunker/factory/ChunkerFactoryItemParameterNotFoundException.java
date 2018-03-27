package g419.liner2.core.chunker.factory;

import org.ini4j.Ini;

public class ChunkerFactoryItemParameterNotFoundException extends Exception {

    public ChunkerFactoryItemParameterNotFoundException(final Ini.Section section, final String name){
        super(String.format("Parameter %s not found in %s", name, section.toString()));
    }

}
