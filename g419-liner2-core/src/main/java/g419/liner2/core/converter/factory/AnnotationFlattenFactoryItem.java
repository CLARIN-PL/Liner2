package g419.liner2.core.converter.factory;

import g419.liner2.core.converter.AnnotationFlattenConverter;
import g419.liner2.core.converter.Converter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by michal on 9/25/14.
 */
public class AnnotationFlattenFactoryItem extends ConverterFactoryItem {

    public AnnotationFlattenFactoryItem(){ super("annotation-flatten:(.*\\.txt)");}

    @Override
    public Converter getConverter() {
        ArrayList<String> categories = new ArrayList<String>();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(matcher.group(1)));
            String line = br.readLine();
            while (line != null) {
                categories.add(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    return new AnnotationFlattenConverter(categories);
    }
}
