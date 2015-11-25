package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;

import java.util.*;
import java.util.regex.Pattern;

public class ActionValidateNormalizationData extends Action {
    private String input_file = null;
    private String input_format = null;
    protected List<Pattern> types;
    protected String metaKey = null;

    public ActionValidateNormalizationData() {
        super("normalizer-validate");
        this.setDescription("Read all annotation and their metadata and look for errors.");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(OptionBuilder.withArgName("types").hasArg().
                        withLongOpt("types").
                        withDescription("Annotation types that will be analyzed (separated with ;)").
                        create("t")
        );
        this.options.addOption(OptionBuilder.withArgName("metaKey").hasArg().
                        withLongOpt("metaKey").
                        withDescription("Metadata key that will be analysed (just pass 'lemma' here for now)").
                        create("m")
        );
    }

    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        if (!line.hasOption("t")) {
            //todo: specialized exception
            Exception up = new RuntimeException("You must specify annotation types!");
            throw up;
        }
        if (!line.hasOption("m")) {
            //todo: specialized exception
            Exception up = new RuntimeException("You must specify metadata key!");
            throw up;
        }
        types = new ArrayList<>();
        for (String type: line.getOptionValue("t").split("[;]"))
            types.add(Pattern.compile(type));
        metaKey = line.getOptionValue("m");
    }

    protected static class Tuple {
        final public String base;
        final public String type;
        final public String value;
        final public String documentName;
        final public String fullSentence;

        public Tuple(String base, String type, String value, String documentName, String fullSentence) {
            this.base = base;
            this.type = type;
            this.value = value;
            this.documentName = documentName;
            this.fullSentence = fullSentence;
        }

        public String getKey(){
            return base+"|"+type;
        }
    }

    @Override
    public void run() throws Exception {
        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
        Document document;
        Map<String, List<Tuple>> tuples = new HashMap<>();
        while ((document=reader.nextDocument())!=null){
            for (Sentence sentence: document.getSentences())
                for (Annotation annotation: sentence.getAnnotations(types)) {
                    Tuple tuple = new Tuple(
                            annotation.getBaseText(),
                            annotation.getType(),
                            annotation.getMetadata().get(metaKey),
                            document.getName(),
                            sentence.toString()
                    );
                    if (!tuples.containsKey(tuple.getKey()))
                        tuples.put(tuple.getKey(), new ArrayList<Tuple>());
                    tuples.get(tuple.getKey()).add(tuple);
                }
            //System.gc();
        }
        println("Size:", tuples.size());
        while (!tuples.isEmpty()) {
            String key = tuples.keySet().iterator().next();
            List<Tuple> matching = tuples.get(key);
            tuples.remove(key);
            //System.gc();
            compareWithEachOther(key, matching);
        }
    }

    protected static String quote(String txt){
        return "\""+txt+"\"";
    }

    protected void compareWithEachOther(String key, List<Tuple> tuples){
        println("Comparing", tuples.size(),"tuples for key", key);
        Set<String> vals = new HashSet<>();
        for (Tuple t: tuples)
            vals.add(t.value);
        if (vals.size()!=1) {
            Tuple example = tuples.iterator().next();
            println("Possible error!");
            println("Type:", example.type);
            println("Base:", example.base);
            println("Values:");
            for (Tuple t: tuples){
                println("\t", t.value);
                println("\t\tdocument:", t.documentName);
                println("\t\tsentence context:", t.fullSentence);
            }
        }
    }

    protected void println(Object... args){
        for (Object a: args)
            System.out.print(a.toString()+" ");
        System.out.println();
    }
}
