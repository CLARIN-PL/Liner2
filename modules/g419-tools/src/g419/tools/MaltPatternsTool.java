package g419.tools;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.CommonOptions;
import g419.liner2.api.tools.MaltSentence;
import g419.liner2.api.tools.Maltparser;
import g419.liner2.api.tools.ValueComparator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.maltparser.MaltParserService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by michal on 2/12/15.
 */
public class MaltPatternsTool extends Tool{

    public static final String OPTION_MALT = "m";
    public static final String OPTION_MALT_LONG = "malt";

    public static final String OPTION_PATTERNS = "p";
    public static final String OPTION_PATTERNS_LONG = "patterns";

    public static final String OPTION_SENTENCES = "s";
    public static final String OPTION_SENTENCES_LONG = "sentences";

    private String input_file = null;
    private String input_format = null;
    private String output_file = null;
    private boolean getSentences = false;

    private MaltParserService malt;
    private ArrayList<MaltPattern> patterns = new ArrayList<MaltPattern>();
    private static Pattern relName = Pattern.compile("^--\\(([a-z_]+)\\)-->$");
    HashMap<String, HashMap<String, HashMap<String, Integer>>> results = new HashMap<>();

    public MaltPatternsTool() {
        super("malt");
        this.setDescription("ToDo");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getModelFileOption());

        OptionBuilder.withArgName("malt");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("path to maltparser model");
        OptionBuilder.withLongOpt(OPTION_MALT_LONG);
        OptionBuilder.isRequired();
        this.options.addOption(OptionBuilder.create(OPTION_MALT));

        OptionBuilder.withArgName("patterns");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("path to file with patterns");
        OptionBuilder.withLongOpt(OPTION_PATTERNS_LONG);
        OptionBuilder.isRequired();
        this.options.addOption(OptionBuilder.create(OPTION_PATTERNS));

        OptionBuilder.withDescription("create additional output with sentences for all names");
        OptionBuilder.withLongOpt(OPTION_SENTENCES_LONG);
        this.options.addOption(OptionBuilder.create(OPTION_SENTENCES));
    }

    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        String modelPath = line.getOptionValue(OPTION_MALT);
        if(Maltparser.isInitialized(modelPath))
            malt = Maltparser.getParser(modelPath);
        else
            malt = Maltparser.addParser(modelPath);
        getPatterns(line.getOptionValue(OPTION_PATTERNS));

        if(line.hasOption(OPTION_SENTENCES)){
            getSentences = true;
        }
    }

    @Override
    public void run() throws Exception {
        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
        Document ps = reader.nextDocument();

        BufferedWriter sentenceWriter = null;
        if(getSentences){
            sentenceWriter= new BufferedWriter(new FileWriter(this.output_file.replace(".csv", "_sentences.csv")));
        }
        BufferedWriter maltDataWriter= new BufferedWriter(new FileWriter(this.output_file.replace(".csv", ".conll")));
        while ( ps != null ){
            for(Sentence sent: ps.getSentences()){
                MaltSentence maltSent = new MaltSentence(sent, sent.getChunks());
                String [] parsedTokens =  malt.parseTokens(maltSent.getMaltData());
                maltDataWriter.write("# " + sent.toString() + "\n");
                for(String tokData: parsedTokens){
                    maltDataWriter.write(tokData+"\n");
                }
                maltDataWriter.write("\n");
                String [][] splittedData = new String[parsedTokens.length][10];
                for(int i=0; i<parsedTokens.length; i++){
                    splittedData[i] = parsedTokens[i].split("\t");
                }
                HashMap<Annotation, Integer> annIndices = maltSent.getAnnotationIndices();
                if(!annIndices.isEmpty()){

                    for(Annotation ann: annIndices.keySet()){
                        int nameIdx = annIndices.get(ann);
                        boolean foundPatternForName = false;
                        String name = splittedData[nameIdx][2];
                        for(MaltPattern pattern: patterns){
                            String x = pattern.findX(splittedData, nameIdx);
                            if(x != null){
                                HashMap<String, HashMap<String, Integer>> nameResults;
                                if(results.containsKey(name)){
                                    nameResults = results.get(name);
                                }
                                else{
                                    nameResults = new HashMap<>();
                                    results.put(name, nameResults);
                                }
                                HashMap<String, Integer> patternResults;
                                if(nameResults.containsKey(pattern.pattString)){
                                    patternResults = nameResults.get(pattern.pattString);
                                }
                                else{
                                    patternResults = new HashMap<>();
                                    nameResults.put(pattern.pattString, patternResults);
                                }
                                if(patternResults.containsKey(x)){
                                    patternResults.put(x, patternResults.get(x) + 1);
                                }
                                else{
                                    patternResults.put(x, 1);
                                }
                                if(sentenceWriter != null){
                                    foundPatternForName = true;
                                    sentenceWriter.write(name + "\t" + x + "\t" + pattern.pattString + "\t" + sent.toString() + "\n");
                                }
                            }
                        }
                        if(sentenceWriter != null && !foundPatternForName){
                            sentenceWriter.write(name + "\tNOT FOUND\tNOT FOUND\t" + sent.toString() + "\n");
                        }
                    }
                }

            }




            ps = reader.nextDocument();
        }
        if(getSentences){
            sentenceWriter.close();
        }
        reader.close();

        BufferedWriter dictWriter = new BufferedWriter(new FileWriter(this.output_file));
        for(String name: results.keySet()){
            HashMap<String, HashMap<String, Integer>> nameResults = results.get(name);
            for(String patt: nameResults.keySet()){
                dictWriter.write(name+"\t"+patt);
                Map<String, Integer> pattResults = nameResults.get(patt);
                Map<String,Integer> sortedResults = ValueComparator.sortByValues(pattResults, true);
                for(String x: sortedResults.keySet()){
                    dictWriter.write("\t" + x + "\t" + pattResults.get(x));
                }
                dictWriter.write("\n");
            }
        }
        dictWriter.close();
    }

    private void getPatterns(String file) throws IOException {
        Files.lines(Paths.get(file)).filter((line) -> !line.isEmpty()).forEach((patt) -> patterns.add(new MaltPattern(patt)));


    }

    private class MaltPattern{
        boolean foundX = false;
        boolean foundName = false;
        boolean XFirst = false;
        boolean nameFirst = false;
        public String pattString;
        ArrayList<String> relsBeforeX = new ArrayList<String>();
        ArrayList<String> relsAfterX = new ArrayList<String>();
        ArrayList<String> relsBeforeName = new ArrayList<String>();
        ArrayList<String> relsAfterName = new ArrayList<String>();
        ArrayList<String> relsBetween = new ArrayList<String>();
        ArrayList<String> formsBeforeX = new ArrayList<String>();
        ArrayList<String> formsAfterX = new ArrayList<String>();
        ArrayList<String> formsBeforeName = new ArrayList<String>();
        ArrayList<String> formsAfterName = new ArrayList<String>();
        ArrayList<String> formsBetween = new ArrayList<String>();

        MaltPattern(String patternString){
            pattString = patternString;
            ArrayList<String> tempRels = new ArrayList<>();
            ArrayList<String> tempForms = new ArrayList<>();
            for(String atom: patternString.split("\\s+")){
                if(atom.toLowerCase().equals("x")){
                    foundX = true;
                    if(!nameFirst){
                        XFirst = true;
                        relsBeforeX = new ArrayList<>(tempRels);
                        formsBeforeX = new ArrayList<>(tempForms);
                        tempRels = new ArrayList<>();
                        tempForms = new ArrayList<>();
                    }
                    else{
                        relsBetween = new ArrayList<>(tempRels);
                        formsBetween = new ArrayList<>(tempForms);
                        tempRels = new ArrayList<>();
                        tempForms = new ArrayList<>();
                    }
                }
                else if(atom.toLowerCase().equals("name")){
                    foundName = true;
                    if(!XFirst){
                        nameFirst = true;
                        relsBeforeName = new ArrayList<>(tempRels);
                        formsBeforeName = new ArrayList<>(tempForms);
                        tempRels = new ArrayList<>();
                        tempForms = new ArrayList<>();
                    }
                    else{
                        relsBetween = new ArrayList<>(tempRels);
                        formsBetween = new ArrayList<>(tempForms);
                        tempRels = new ArrayList<>();
                        tempForms = new ArrayList<>();
                    }
                }
                else{
                    Matcher m = relName.matcher(atom);
                    if(m.find()){
                        tempRels.add(m.group(1));
                    }
                    else{
                        tempForms.add(atom);
                    }
                }
            }
            if(!tempRels.isEmpty()) {
                if (XFirst) {
                    relsAfterName = new ArrayList<>(tempRels);
                    formsAfterName = new ArrayList<>(tempForms);
                } else if (nameFirst) {
                    relsAfterX = new ArrayList<>(tempRels);
                    formsAfterX = new ArrayList<>(tempForms);
                }
            }
        }

        public String findX(String[][] sentenceData, int nameIdx){
            int xIdx = -1;
            if(!checkBefore(sentenceData, nameIdx, relsBeforeName, formsBeforeName)){
                return null;
            }

            if(!checkAfter(sentenceData, nameIdx, relsAfterName, formsAfterName)){
                return null;
            }

            if(nameFirst){
                xIdx = checkBetweenNameFirst(sentenceData, nameIdx, relsBetween, formsBetween);
                if(xIdx != -1){
                    if(!checkAfter(sentenceData, xIdx, relsAfterX, formsAfterX)){
                        return null;
                    }
                }
            }
            else if(XFirst){
                xIdx = checkBetweenXFirst(sentenceData, nameIdx, relsBetween, formsBetween);
                if(xIdx != -1){
                    if(!checkBefore(sentenceData, xIdx, relsBeforeX, formsBeforeX)){
                        return null;
                    }
                }
            }

            return xIdx == -1 ? null : sentenceData[xIdx][2];
        }
    }

    private int checkBetweenXFirst(String[][] sentenceData, int nameIdx, ArrayList<String> relsBetween, ArrayList<String> formsBetween){
        int currentIdx = nameIdx;
        for(int i=relsBetween.size() - 1; i>0; i--){
            if(sentenceData[currentIdx][9].equals(relsBetween.get(i))){
                currentIdx = Integer.parseInt(sentenceData[currentIdx][8]) - 1;
                if(!sentenceData[currentIdx][3].equals(formsBetween.get(i-1))){
                    return -1;
                }
            }
            else{
                return -1;
            }
        }
        if(sentenceData[currentIdx][9].equals(relsBetween.get(0))) {
            return Integer.parseInt(sentenceData[currentIdx][8]) - 1;
        }
        else{
            return -1;
        }
    }

    private int checkBetweenNameFirst(String[][] sentenceData, int nameIdx, ArrayList<String> relsBetween, ArrayList<String> formsBetween){
        int currentIdx = nameIdx;
        for(int i=0; i<relsBetween.size(); i++){
            int prevLinkIdx = -1;
            for(int j=0; j<sentenceData.length; j++){
                int tmpIdx = Integer.parseInt(sentenceData[j][8]) - 1;
                if(currentIdx == tmpIdx && sentenceData[j][9].equals(relsBetween.get(i))){
                    prevLinkIdx = j;
                    break;
                }
            }
            if(prevLinkIdx == -1){
                return -1;
            }
            else{
                if(i == relsBetween.size() - 1){
                    return prevLinkIdx;
                }
                else{
                    if(sentenceData[prevLinkIdx][3].equals(formsBetween.get(i))){
                        currentIdx = prevLinkIdx;
                    }
                    else{
                        return -1;
                    }
                }
            }
        }
        return currentIdx != nameIdx ? currentIdx : -1;
    }

    private boolean checkAfter(String[][] sentenceData, int idx, ArrayList<String> rels, ArrayList<String> forms){
        int currentIdx = idx;
        for(int i=0; i<rels.size(); i++){
            int prevLinkIdx = -1;
            for(int j=0; j<sentenceData.length; j++){
                if(sentenceData[j][9].equals(rels.get(i))){
                    if(Integer.parseInt(sentenceData[j][8]) - 1 == currentIdx){
                        if(sentenceData[j][3].equals(forms.get(i))) {
                            prevLinkIdx = j;
                            break;
                        }
                    }
                }
            }
            if(prevLinkIdx == -1){
                return false;
            }
            else{
                currentIdx = prevLinkIdx;
            }

        }
        return true;
    }

    private boolean checkBefore(String[][] sentenceData, int idx, ArrayList<String> rels, ArrayList<String> forms){
        int currentIdx = idx;
        for(int i=rels.size()-1; i>=0; i--){
            if(sentenceData[currentIdx][9].equals(rels.get(i))){
                currentIdx = Integer.parseInt(sentenceData[currentIdx][8]) - 1;
                if(!sentenceData[currentIdx][3].equals(forms.get(i))){
                    return false;
                }
            }
            else{
                return false;
            }
        }
        return true;
    }


}
