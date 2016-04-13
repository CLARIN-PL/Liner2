package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.ParameterException;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Chunking in pipe mode.
 * @author Maciej Janicki, Michał Marcińczuk
 *
 */
public class ActionNormalizerEval2 extends Action{

    private String input_file = null;
    private String input_format = null;
    private String output_file = null;
    private String output_format = null;

	public ActionNormalizerEval2(){
		super("normalizer-eval2");
        this.setDescription("processes data with given model");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileFormatOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getModelFileOption());
	}

	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.output_format = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT, "ccl");
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
	}
	
	/**
	 * Module entry function.
	 */
	public ArrayList<Document> read_documents() throws Exception{
        ArrayList<Document> outputList = new ArrayList<Document>();

	
        AbstractDocumentReader reader = getInputReader();

		


		Document ps = reader.nextDocument();

		while ( ps != null ){
            outputList.add(ps);
			ps = reader.nextDocument();
		}
        reader.close();

        return outputList;
	}


    public void run() throws Exception{

        if ( !LinerOptions.isGlobalOption(LinerOptions.OPTION_USED_CHUNKER) ){
            throw new ParameterException("Parameter 'chunker' in 'main' section of model not set");
        }

        TokenFeatureGenerator gen = null;

        if (!LinerOptions.getGlobal().features.isEmpty()){
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }

		/* Create all defined chunkers. */
        ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
        cm.loadChunkers();

        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());

        ArrayList<Document> d1 = read_documents();

        float tp1 = 0, tp2 = 0, tp3 = 0, fp1 = 0, fp2 = 0, fp3 = 0, fn1 = 0, fn2 = 0, fn3 = 0;

        for (Document d : d1) {
            Set<Annotation> referenceAnnotationSet = new HashSet<>();
            Set<Annotation> chunkerAnnotationSet = new HashSet<>();

            Map<Annotation, Annotation> referenceAnnotationMap = new HashMap<>();
            Map<Annotation, Annotation> chunkerAnnotationMap = new HashMap<>();
            HashMap<Sentence, AnnotationSet> referenceChunks = d.getChunkings();
            //d.removeMetadata("lval");
            d.removeAnnotations();
            gen.generateFeatures(d);
            chunker.prepare(d);
            Set<String> typeSet = new HashSet<>(Arrays.asList("t3_date", "t3_time", "t3_duration"));
            //Set<String> typeSet = new HashSet<>(Arrays.asList("t3_time"));
            HashMap<Sentence, AnnotationSet> chunkings = chunker.chunk(d);
            for (Sentence s : d.getSentences()) {
                referenceAnnotationSet.addAll(referenceChunks.get(s).chunkSet().stream()
                        .filter(p -> typeSet.contains(p.getType()))
                        .collect(Collectors.toSet()));
                referenceAnnotationMap.putAll(referenceChunks.get(s).chunkSet().stream()
                        .filter(p -> typeSet.contains(p.getType()))
                        .collect(Collectors.toMap(k -> k, v -> v)));
                chunkerAnnotationSet.addAll(chunkings.get(s).chunkSet().stream()
                        .filter(p -> typeSet.contains(p.getType()))
                        .collect(Collectors.toSet()));
                chunkerAnnotationMap.putAll(chunkings.get(s).chunkSet().stream()
                        .filter(p -> typeSet.contains(p.getType()))
                        .collect(Collectors.toMap(k -> k, v -> v)));
            }


            //compare only range and class
            Set<Annotation> tpSet = referenceAnnotationSet.stream()
                    .filter(p -> chunkerAnnotationSet.contains(p))
                    .collect(Collectors.toSet());
            Set<Annotation> fpSet = referenceAnnotationSet.stream()
                    .filter(p -> !chunkerAnnotationSet.contains(p))
                    .collect(Collectors.toSet());
            Set<Annotation> fnSet = chunkerAnnotationSet.stream()
                    .filter(p -> !referenceAnnotationSet.contains(p))
                    .collect(Collectors.toSet());
            tp1 += tpSet.size();
            fp1 += fpSet.size();
            fn1 += fnSet.size();

            //compare range and class and lval
            Set<Annotation> tpLVALSet = referenceAnnotationSet.stream()
                    .filter(p -> chunkerAnnotationSet.contains(p)
                            && p.metaDataMatchesKey("lval", chunkerAnnotationMap.get(p)))
                    .collect(Collectors.toSet());
            Set<Annotation> fpLVALSet = referenceAnnotationSet.stream()
                    .filter(p -> !chunkerAnnotationSet.contains(p) ||
                                    (chunkerAnnotationSet.contains(p) &&
                                            !p.metaDataMatchesKey("lval", chunkerAnnotationMap.get(p))
                                    )
                    )
                    .collect(Collectors.toSet());
            Set<Annotation> fnLVALSet = chunkerAnnotationSet.stream()
                    .filter(p -> !referenceAnnotationSet.contains(p) ||
                                    (referenceAnnotationSet.contains(p) &&
                                            !p.metaDataMatchesKey("lval", referenceAnnotationMap.get(p))
                                    )
                    )
                    .collect(Collectors.toSet());


            Set<Annotation> tmpLVALSet = referenceAnnotationSet.stream()
                    .filter(p ->    (chunkerAnnotationSet.contains(p) &&
                                            !p.metaDataMatchesKey("lval", chunkerAnnotationMap.get(p))
                                    )
                    )
                    .collect(Collectors.toSet());


            tp2 += tpLVALSet.size();
            fp2 += fpLVALSet.size();
            fn2 += fnLVALSet.size();



            //compare range and class and and val
            Set<Annotation> tpVALSet = referenceAnnotationSet.stream()
                    .filter(p -> chunkerAnnotationSet.contains(p)
                            && p.metaDataMatchesKey("val", chunkerAnnotationMap.get(p)))
                    .collect(Collectors.toSet());
            Set<Annotation> fpVALSet = referenceAnnotationSet.stream()
                    .filter(p -> !chunkerAnnotationSet.contains(p) ||
                                    (chunkerAnnotationSet.contains(p) &&
                                            !p.metaDataMatchesKey("val", chunkerAnnotationMap.get(p))
                                    )
                    )
                    .collect(Collectors.toSet());
            Set<Annotation> fnVALSet = chunkerAnnotationSet.stream()
                    .filter(p -> !referenceAnnotationSet.contains(p) ||
                                    (referenceAnnotationSet.contains(p) &&
                                            !p.metaDataMatchesKey("val", referenceAnnotationMap.get(p))
                                    )
                    )
                    .collect(Collectors.toSet());
            tp3 += tpVALSet.size();
            fp3 += fpVALSet.size();
            fn3 += fnVALSet.size();

        }

        float p1 = tp1 / (float)(tp1 + fp1);
        float r1 = tp1 / (float)(tp1 + fn1);
        float f1 = 2 * p1 * r1 / (p1 + r1);

        float p2 = tp2 / (float)(tp2 + fp2);
        float r2 = tp2 / (float)(tp2 + fn2);
        float f2 = 2 * p2 * r2 / (p2 + r2);

        float p3 = tp3 / (float)(tp3 + fp3);
        float r3 = tp3 / (float)(tp3 + fn3);
        float f3 = 2 * p3 * r3 / (p3 + r3);

        int i = 0;
    }

    /**
     * Get document writer defined with the -o and -t options.
     * @return
     * @throws Exception
     */
    protected AbstractDocumentWriter getOutputWriter(String prefix) throws Exception{
        AbstractDocumentWriter writer;

        if ( output_format.startsWith("batch:") && !input_format.startsWith("batch:") ) {
            throw new Exception("Output format `batch:` (-o) is valid only for `batch:` input format (-i).");
        }
        if (output_file == null){
            writer = WriterFactory.get().getStreamWriter(System.out, output_format);
        }
        else if (output_format.equals("arff")){
//            ToDo: format w postaci arff:{PLIK Z TEMPLATEM}
            writer = null;
//            CrfTemplate arff_template = LinerOptions.getGlobal().getArffTemplate();
//            writer = WriterFactory.get().getArffWriter(output_file, arff_template);
        }
        else{
            writer = WriterFactory.get().getStreamWriter(prefix+output_file, output_format);
        }
        return writer;
    }

    /**
     * Get document reader defined with the -i and -f options.
     * @return
     * @throws Exception
     */
    protected AbstractDocumentReader getInputReader() throws Exception{
        return ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
    }
		
}
