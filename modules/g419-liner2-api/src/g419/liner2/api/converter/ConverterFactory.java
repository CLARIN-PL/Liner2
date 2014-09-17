package g419.liner2.api.converter;

import java.util.ArrayList;

/**
 * Created by michal on 6/3/14.
 */
public class ConverterFactory {

    public static Converter createPipe(ArrayList<String> descriptions){
        ArrayList<Converter> pipe = new ArrayList<Converter>();
        for(String desc: descriptions){
            if(desc.startsWith("annotation-mapping")){
                pipe.add(new AnnotationMappingConverter(desc.split(":")[1]));
            }
            else if(desc.equals("annotation-remove-nested")){
                pipe.add(new AnnotationRemoveNestedConverter());
            }
            else if(desc.startsWith("annotation-filter-by-regex")){
                pipe.add(new AnnotationFilterByRegexConverter(desc.split(":")[1]));
            }
            else if(desc.startsWith("expand-features")){
                pipe.add(new TokenFeaturesConverter(desc.split(":")[1]));
            }
            else if(desc.startsWith("annotation-flatten")){
                pipe.add(new FlattenConverter(desc.split(":")[1]));
            }
            else{
                throw new Error(String.format("Converter description '%s' not recognized", desc));
            }

        }
        return new PipeConverter(pipe);
    }
}
