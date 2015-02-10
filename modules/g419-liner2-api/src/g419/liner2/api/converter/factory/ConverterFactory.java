package g419.liner2.api.converter.factory;

import g419.liner2.api.converter.*;
import g419.corpus.Logger;

import java.util.ArrayList;

/**
 * Created by michal on 6/3/14.
 */


public class ConverterFactory {

    private static ArrayList<ConverterFactoryItem> items = new ArrayList<ConverterFactoryItem>(){/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
        add(new AnnotationFilterByRegexFactoryItem());
        add(new AnnotationFlattenFactoryItem());
        add(new AnnotationMappingFactoryItem());
        add(new AnnotationRemoveNestedFactoryItem());
        add(new TokenFeaturesFactoryItem());

    }};

    public static Converter createConverter(String description){
        Logger.log("-> Setting up converter: " + description);
        for (ConverterFactoryItem item : items) {
            if ( item.matchPattern(description) ) {
                Converter converter =  item.getConverter();
                return converter;
            }
        }
        throw new Error(String.format("Converter description '%s' not recognized", description));
    }

    public static Converter createPipe(ArrayList<String> descriptions){
        ArrayList<Converter> pipe = new ArrayList<Converter>();
        for(String desc: descriptions){
            pipe.add(ConverterFactory.createConverter(desc));
        }
        return new PipeConverter(pipe);
    }

    public static void printAvailableConverters(){
        System.out.println("Available converters:");
        for(ConverterFactoryItem item: items){
            // ToDo: wypisuje na poczatku i końca wyrażenia regularnego (^ i $), pozbyć się tego substringiem czy zostawić?
            System.out.println("- " + item.pattern);
        }
    }
}
