package g419.tools.action;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;

import g419.lib.cli.Action;

/**
 * Created by michal on 11/5/14.
 */
public class ConvertJRipRulesTool extends Action {

    public static final String OPTION_OUTPUT_FILE = "t";
    public static final String OPTION_OUTPUT_FILE_LONG = "output_file";

    public static final String OPTION_INPUT_FILE = "f";
    public static final String OPTION_INPUT_FILE_LONG = "input_file";

    Pattern splitComplexRule = Pattern.compile(".+?\\[-?[0-9]]");
    Pattern complexRuleAtom = Pattern.compile("(.+?)\\[(-?[0-9])]");
    Pattern atomicRuleContext = Pattern.compile("^(.+?)\\+?(-?[0-9])$");

    private String input_file = null;
    private String output_file = null;

    public ConvertJRipRulesTool() {
        super("convertJRipRules");
        options.addOption(Option.builder(OPTION_OUTPUT_FILE).longOpt(OPTION_OUTPUT_FILE_LONG).hasArg().argName("filename")
        					.desc("path to an output file").build());
        options.addOption(Option.builder(OPTION_INPUT_FILE).longOpt(OPTION_INPUT_FILE_LONG).hasArg().argName("filename")
				.desc("path to an input file").build());
    }

    @Override
    public void parseOptions(final CommandLine line) throws Exception {
        this.input_file = line.getOptionValue(OPTION_INPUT_FILE);
        this.output_file = line.getOptionValue(OPTION_OUTPUT_FILE);
    }

    @Override
    public void run() throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(this.input_file));
        BufferedWriter bw = new BufferedWriter(this.output_file != null ? new FileWriter(this.output_file) : new OutputStreamWriter(System.out));
        String rule = br.readLine();
        while(rule != null) {
            if(!rule.isEmpty() && !rule.startsWith("#")){
                rule = rule.substring(0,rule.indexOf("=>"));
                rule = rule.trim();
                bw.write(convertRule(rule)+ "\n");
            }
            rule = br.readLine();
        }
        br.close();
        bw.close();

    }

    public String convertRule(String rule){
        String[] features = rule.split("and");
        StringBuilder template = new StringBuilder();
        for(String feat: features){
            feat = StringUtils.strip( feat ," ()");
            Matcher complexRuleMatcher = splitComplexRule.matcher(feat);
            Matcher ruleMatcher;
            if(complexRuleMatcher.find()){
                do{
                    String atomicFeat =  complexRuleMatcher.group(0);
                    atomicFeat = StringUtils.strip(atomicFeat, "_");
                    ruleMatcher = complexRuleAtom.matcher(atomicFeat);
                    ruleMatcher.find();
                    template.append(String.format("%s:%s/", ruleMatcher.group(1), ruleMatcher.group(2)));
                }
                while(complexRuleMatcher.find());
            }
            else{
                feat = feat.substring(0, feat.indexOf(" = "));
                ruleMatcher = atomicRuleContext.matcher(feat);
                ruleMatcher.find();
                template.append(String.format("%s:%s/", ruleMatcher.group(1), ruleMatcher.group(2)));
            }
        }
        return template.toString();
    }
}
