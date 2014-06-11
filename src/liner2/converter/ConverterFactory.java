package liner2.converter;

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
            else{
                throw new Error(String.format("Converter description '%s' not recognized", desc));
            }

        }
        return new PipeConverter(pipe);
    }
}
