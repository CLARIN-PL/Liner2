package g419.liner2.api.converter;


import g419.corpus.structure.Annotation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    public void apply(LinkedHashSet<Annotation> sentenceAnnotations) {
        LinkedHashMap<Annotation, String> toUpdate = new LinkedHashMap<Annotation, String>();
        for(Annotation ann: sentenceAnnotations){
            for(Pattern patt: channelsMapping.keySet()){
                if(patt.matcher(ann.getType()).find()){
                    toUpdate.put(ann, (channelsMapping.get(patt)));

                }

            }
        }
        for(Annotation ann: toUpdate.keySet()){
            sentenceAnnotations.remove(ann);
            ann.setType(toUpdate.get(ann));
            sentenceAnnotations.add(ann);
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
