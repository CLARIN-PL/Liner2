package liner2.converter;

import liner2.structure.Annotation;
import liner2.structure.AnnotationSet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by michal on 6/3/14.
 */
public class AnnotationMappingConverter extends Converter{
    HashMap<Pattern, String> channelsMapping;
    public AnnotationMappingConverter(String mappingFile){
        try {
            parseMapping(mappingFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void apply(AnnotationSet sentenceAnnotations) {
        for(Annotation ann: sentenceAnnotations.chunkSet()){
            for(Pattern patt: channelsMapping.keySet()){
                if(patt.matcher(ann.getType()).find()){
                    ann.setType(channelsMapping.get(patt));
                }
                else{
                }

            }
        }
    }

    private void parseMapping(String mappingFile) throws IOException {
        channelsMapping = new HashMap<Pattern, String>();
        BufferedReader br = new BufferedReader(new FileReader(mappingFile));
        try {
            String line = br.readLine();
            while (line != null) {
                String [] pattern_val = line.split(" -> ");
                channelsMapping.put(Pattern.compile("^"+pattern_val[0]+"$"), pattern_val[1]);
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
        }
    }
}
