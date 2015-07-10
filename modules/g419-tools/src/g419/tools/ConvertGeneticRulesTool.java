package g419.tools;

import g419.liner2.api.LinerOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

/**
 * Created by michal on 5/7/15.
 */
public class ConvertGeneticRulesTool extends Tool{

    public static final String OPTION_RULES = "r";
    public static final String OPTION_RULES_LONG = "rules";

    public static final String OPTION_FEATURES = "f";
    public static final String OPTION_FEATURES_LONG = "features";

    public static final String OPTION_TEMPLATE = "t";
    public static final String OPTION_TEMPLATE_LONG = "template";

    private File rules_file, features_file, template_file;
    LinkedHashMap<String, String> features = new LinkedHashMap<>();
    ArrayList<String> featureNames;
    Pattern featurePattern = Pattern.compile("(\\(([0-9]+)\\)\\[([-0-9]+)\\](!=|=)\"(.*?)\";)");
    private LinkedHashMap<String, TreeSet<Integer>> atomicTemplates;
    private LinkedHashSet<String> complexTemplates;
    int to_complex = 0 ;


    public ConvertGeneticRulesTool() {
        super("convertGeneticRules");
        options.addOption(OptionBuilder
                .withArgName("filename").hasArg()
                .withDescription("path to an output file")
                .withLongOpt(OPTION_RULES_LONG)
                .isRequired()
                .create(OPTION_RULES));
        options.addOption(OptionBuilder
                .withArgName("filename").hasArg()
                .withDescription("path to an output file")
                .withLongOpt(OPTION_FEATURES_LONG)
                .isRequired()
                .create(OPTION_FEATURES));
        options.addOption(OptionBuilder
                .withArgName("filename").hasArg()
                .withDescription("path to an output file")
                .withLongOpt(OPTION_TEMPLATE_LONG)
                .isRequired()
                .create(OPTION_TEMPLATE));
    }

    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        rules_file = new File(line.getOptionValue(OPTION_RULES));
        if(!rules_file.exists()){
            throw new DataFormatException("Rules file does not exist: " + rules_file.getAbsolutePath());
        }
        features_file = new File(line.getOptionValue(OPTION_FEATURES));
        parseFeatures(features_file);
        featureNames = new ArrayList<>(features.keySet());
        template_file = new File(line.getOptionValue(OPTION_TEMPLATE));
        parseTemplate(template_file);


    }

    private void parseTemplate(File file) throws IOException, DataFormatException {
        if(!template_file.exists()){
            throw new DataFormatException("Template file does not exist: " + template_file.getAbsolutePath());
        }
        complexTemplates = new LinkedHashSet<>();
        atomicTemplates = new LinkedHashMap<>();
        for(String tempalate: LinerOptions.getGlobal().parseLines(file)){
            if(tempalate.contains("/")){
                complexTemplates.add(tempalate);
            }
            else{
                String[] values = tempalate.split(":");
                TreeSet<Integer> indexes = new TreeSet<>();
                for(int i=1; i<values.length; i++){
                    indexes.add(Integer.parseInt(values[i]));
                }
                atomicTemplates.put(values[0], indexes);
            }
        }
    }

    private void parseFeatures(File file) throws IOException {
        for(String feature: LinerOptions.getGlobal().parseLines(file)){
            String[] splitted = feature.split(":");
            String featureName;
            if(splitted.length > 2 && splitted[1].length() == 1) {
                featureName = splitted[0]+splitted[1];
            }
            else {
                featureName = splitted[0];
            }
            features.put(featureName, feature);
        }
    }

    int i = 0;

    @Override
    public void run() throws Exception {
        BufferedReader rulesReader = new BufferedReader(new FileReader(rules_file));
        String line = rulesReader.readLine();

        while(line != null){
            parseRule(line);
            i++;
            line = rulesReader.readLine();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(features_file.getAbsolutePath().replace(".txt", "_converted.txt")));
        for(String feature: features.values()){
            writer.write(feature + "\n");
        }
        writer.close();

        File newTemplate = new File(template_file.getAbsolutePath().replace(".txt", "_converted.txt"));
        writer = new BufferedWriter(new FileWriter(newTemplate));
        for(String atomicTemplate: atomicTemplates.keySet()){
            writer.write(atomicTemplate);
            for(int offset: atomicTemplates.get(atomicTemplate)){
                writer.write(":" + offset);
            }
            writer.write("\n");
        }
//        System.out.println("COMPELX SIZE:" + complexTemplates.size());
//        System.out.println("RULES SIZE:" + rules.size());
        for(String complexTemplate: complexTemplates){
            writer.write(complexTemplate + "\n");
        }
        writer.close();
    }

    HashMap<String, Integer> rules = new HashMap<>();

    private void parseRule(String rule){
//        if(rules.keySet().contains(rule)){
//            System.out.println(i + " POWTARZA: " + rule + " | " + rules.get(rule));
//        }
        rules.put(rule, i);
        ArrayList<String> newTemplateFeatures = new ArrayList<>();
        String operator, value, ruleAsFeature = null;
        int featureIdx, offset = 0;
        Matcher m = featurePattern.matcher(rule);
        while(m.find()){
                featureIdx = Integer.parseInt(m.group(2));
                offset = Integer.parseInt(m.group(3));
                operator = m.group(4);
                value = m.group(5);
                ruleAsFeature = String.format("(%s)[%d]%s\"%s\";", featureNames.get(featureIdx), offset, operator, value);
                newTemplateFeatures.add(ruleAsFeature);
        }
//        if(!String.valueOf(rule.charAt(rule.lastIndexOf(" ") + 1)).equals("B")
//                && !String.valueOf(rule.charAt(rule.lastIndexOf(" ") + 1)).equals("I")
//                && !String.valueOf(rule.charAt(rule.lastIndexOf(" ") + 1)).equals("O")){
//            System.out.println(String.valueOf(rule.charAt(rule.lastIndexOf(" ") + 1)));
//        }
        String joined = String.join("", newTemplateFeatures) + rule.substring(rule.lastIndexOf(" ") + 1);
//        System.out.println(joined);
        String featurename = "testRule-" + joined.replace(":", "_").replace("/", "_");
        System.out.println(featurename + ":" + joined);
        features.put(featurename, featurename + ":" + joined);
//        if(complexTemplates.contains(featurename + ":0")){
//            System.out.println("CONTAINS: " + featurename + " | RULE: " + rule);
//        }
        complexTemplates.add(featurename + ":0");
//        if(newTemplateFeatures.size() == 1){
//            if (atomicTemplates.containsKey(featureName)) {
//                atomicTemplates.get(featureName).add(offset);
//            } else {
//                TreeSet<Integer> offsets = new TreeSet<Integer>();
//                offsets.add(offset);
//                atomicTemplates.put(featureName, offsets);
//            }
//        }
//        else if(newTemplateFeatures.size() > 5 &&String.join("/", newTemplateFeatures).contains("test-")){
//            System.out.println("VERY COMPLEX: " + String.join("/", newTemplateFeatures));
//            to_complex++;
//            complexTemplates.add(String.join("/", newTemplateFeatures));
//        }
//        else{
//            complexTemplates.add(String.join("/", newTemplateFeatures));
//        }

    }

    private String convertFeature(int featureIdx, String operator, String value) {
        if (operator.equals("=")) {
            return featureNames.get(featureIdx);
        } else if (operator.equals("!=")) {
            Pattern p = Pattern.compile("[^\\p{L}0-9]");
            Matcher m = p.matcher(value);
            String newFeatureName = "test-" + featureNames.get(featureIdx) + "-" + m.replaceAll("_");
            if (!featureNames.contains(newFeatureName)) {
                featureNames.add(newFeatureName);
                features.put(newFeatureName, newFeatureName + ":" + String.join(":", featureNames.get(featureIdx), "equal", value));
            } else {
            }
            return newFeatureName;
        }
        return ""; // unreacheble
    }


}
