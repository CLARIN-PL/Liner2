package g419.crete.core.classifier.factory;

import weka.filters.AllFilter;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;

import java.util.HashMap;

/**
 * Created by akaczmarek on 17.12.15.
 */
public class WekaFilterFactory {
    private static class FactoryHolder {
        private static final WekaFilterFactory FACTORY = new WekaFilterFactory();
    }
    private HashMap<String, Filter> instances;
    private WekaFilterFactory(){
        instances = new HashMap<>();
    }
    public static WekaFilterFactory getFactory(){return FactoryHolder.FACTORY;}

    public void register(String name, Filter filter){
        instances.put(name, filter);
    }

    public Filter getFilter(String name){
        return instances.getOrDefault(name, new AllFilter());
    }

    static{
        Resample simpleResample = new Resample();
        simpleResample.setBiasToUniformClass(1.0); //TODO: fixme - move to configuration
        simpleResample.setSampleSizePercent(100); //TODO: fixme - move to configuration

        SMOTE smoteResample = new SMOTE();
        smoteResample.setClassValue("0"); //TODO: fixme - move to configuration

        WekaFilterFactory.getFactory().register("resample", simpleResample);
        WekaFilterFactory.getFactory().register("smote", smoteResample);

    }

}
