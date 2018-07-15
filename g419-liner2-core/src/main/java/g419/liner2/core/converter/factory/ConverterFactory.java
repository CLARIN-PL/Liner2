package g419.liner2.core.converter.factory;

import g419.corpus.ConsolePrinter;
import g419.lib.cli.ParameterException;
import g419.liner2.core.converter.Converter;
import g419.liner2.core.converter.PipeConverter;

import java.util.ArrayList;

public class ConverterFactory {

    private static final ArrayList<ConverterFactoryItem> items = new ArrayList<ConverterFactoryItem>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            add(new AnnotationFilterByGroupRegexFactoryItem());
            add(new AnnotationFilterByTypeRegexFactoryItem());
            add(new AnnotationFlattenFactoryItem());
            add(new AnnotationMappingFactoryItem());
            add(new AnnotationRemoveNestedFactoryItem());
            add(new TokenFeaturesFactoryItem());
            add(new AnnotaionWrapFactoryItem());
            add(new AnnotationSplitNonadjacentFactoryItem());
        }
    };

    public static Converter createConverter(final String description) throws ParameterException {
        ConsolePrinter.log("-> Setting up converter: " + description);
        for (final ConverterFactoryItem item : items) {
            if (item.matchPattern(description)) {
                final Converter converter = item.getConverter();
                return converter;
            }
        }
        throw new Error(String.format("Converter description '%s' not recognized", description));
    }

    public static Converter createPipe(final ArrayList<String> descriptions) throws ParameterException {
        final ArrayList<Converter> pipe = new ArrayList<>();
        for (final String desc : descriptions) {
            pipe.add(ConverterFactory.createConverter(desc));
        }
        return new PipeConverter(pipe);
    }

    public static void printAvailableConverters() {
        System.out.println("Available converters:");
        for (final ConverterFactoryItem item : items) {
            // ToDo: wypisuje na poczatku i końca wyrażenia regularnego (^ i $), pozbyć się tego substringiem czy zostawić?
            System.out.println("- " + item.pattern);
        }
    }
}
