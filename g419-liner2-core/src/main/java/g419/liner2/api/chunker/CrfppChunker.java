package g419.liner2.api.chunker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.chasen.crfpp.Tagger;

import g419.corpus.ConsolePrinter;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.CrfTemplate;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.liner2.api.chunker.interfaces.DeserializableChunkerInterface;
import g419.liner2.api.chunker.interfaces.SerializableChunkerInterface;
import g419.liner2.api.chunker.interfaces.TrainableChunkerInterface;
import g419.liner2.api.converter.AnnotationWrapConverter;
import g419.liner2.api.tools.TemplateFactory;

public class CrfppChunker extends Chunker
	implements TrainableChunkerInterface, DeserializableChunkerInterface, SerializableChunkerInterface {
	
	private File trainingFile = null;
	private PrintWriter trainingFileWriter = null;
	private Tagger tagger = null;
	private Pattern p = Pattern.compile("([IB])-([^#]*)");
    private CrfTemplate template = null;
	private String model_filename = null;
	private int threads = 1;
	private static final int MAX_TOKENS = 1000;
	private List<Pattern> types = null;
    private String trainingDataFileName = null;
	private List<String> usedFeatures;
	private String wrap = null;
	
    public CrfppChunker(List<String> usedFeatures, String wrap) {
		this.types = new ArrayList<Pattern>();
		this.usedFeatures = usedFeatures;
		this.wrap = wrap;
    }
    
    /**
     * 
     * @param threads
     * @param types
     * @param usedFeatures
     * @param wrap Annotation name category used to wrap annotation sequences annotated with given name.
     */
    public CrfppChunker(int threads, List<Pattern> types, List<String> usedFeatures, String wrap){
		this.threads = threads;
		this.types = types;
		this.usedFeatures = usedFeatures;
		this.wrap = wrap;
    }

    /**
     * Reads output from the external CRF tagger. 
     * Transforms the result from IOB format into a list of annotations.
     * @return chunking with annotations
     */

	private synchronized AnnotationSet chunkSentence(Sentence sentence){
		if (sentence.getTokenNumber()>MAX_TOKENS)
			return new AnnotationSet(sentence);
		return this.chunk(sentence);
	}
	
	/**
	 * Chunks the sentence using CRF++ API.
	 * @param sentence
	 * @return set of recognized chunks
	 */
	private AnnotationSet chunk(Sentence sentence){
		Map<Integer, Integer> tokenIndexMapping = new HashMap<Integer, Integer>();
    	Map<Integer, Annotation> annotationsToWrap = new HashMap<Integer, Annotation>();
    	if ( wrap != null ){
    		for ( Annotation an : sentence.getAnnotations(wrap) ){
    			annotationsToWrap.put(an.getBegin(), an);
    		}
    	}
		
		AnnotationSet chunking = new AnnotationSet(sentence);
        HashMap<String, Annotation> annsByType = new HashMap<String, Annotation>();
        // Prepare date and send them to the API
        tagger.clear();
		String val = null;
		int iWrapped = 0;
		for (int i=0; i<sentence.getTokens().size(); i++) {
			Token token = sentence.getTokens().get(i);
			tokenIndexMapping.put(iWrapped++, i);
			
			Annotation anWrap = annotationsToWrap.get(i);
			if ( anWrap != null ){
				i = anWrap.getEnd();
				token = anWrap.getHeadToken();
				Logger.getLogger(this.getClass()).info("Annotation wrapped on chunking: " + anWrap.toString() + " into " + token.getOrth());
			}
			
			StringBuilder oStr = new StringBuilder();
			for (String feature: usedFeatures){
				oStr.append(" ");
				try {
					val = token.getAttributeValue(feature);
				}
				catch (ArrayIndexOutOfBoundsException e){
					System.out.println("Error: Feature used by CRF chunker not in attribute index: " + feature);
					System.exit(1);
				}
				if ( val != null){
					val = val.replaceAll("\\s+", "_");
					val = val.length()==0 ? "NULL" : val;
				}
				oStr.append(val);

			}
			tagger.add(oStr.toString().trim());
		}
		tokenIndexMapping.put(iWrapped++, sentence.getTokens().size());
		tagger.parse();		

		// Reads the output of parsing
        for (int i = 0; i < tagger.size(); ++i) {
            String label = tagger.y2(i);            
            if(label.equals("O")){
                annsByType = new HashMap<String, Annotation>();
            }
            else{
                Matcher m = p.matcher(label);

                while(m.find()){
                    String annType = m.group(2);
                    if(m.group(1).equals("B")){
                    	int from = tokenIndexMapping.get(i);
                    	int to = tokenIndexMapping.get(i+1)-1;
                        Annotation newAnn = new Annotation(from, to, annType, sentence);
                        chunking.addChunk(newAnn);
                        annsByType.put(annType, newAnn);
                       	newAnn.setConfidence(tagger.prob(i));
                    }
                    else if(m.group(1).equals("I")){
                        if(annsByType.containsKey(annType)){
                        	int from = tokenIndexMapping.get(i);
                        	int to = tokenIndexMapping.get(i+1)-1;
                        	for ( int j=from; j<=to; j++ ){
                        		annsByType.get(annType).addToken(j);
                        	}
                        }
                    }
                }
            }
        }

        return chunking;
    }
		            
    @Override
	public void train() throws Exception {
        ConsolePrinter.log("Training CRF classifer using features:\n" + this.template.printFeatures());
    	this.trainingFileWriter.close();
		this.compileTagger();
    }

    @Override
    public void addTrainingData(Document paragraphSet) {
        ConsolePrinter.log("Loading training data for CRF from document:" + paragraphSet.getName());
        
    	// Utwórz tymczasowy plik do zapisu danych treningowych
    	if ( this.trainingFileWriter == null ){
    		try {
                if(trainingDataFileName != null){
                    this.trainingFile = new File(trainingDataFileName);
                }
                else{
                    this.trainingFile = File.createTempFile("crf_iob", ".txt");
					this.trainingFile.deleteOnExit();
                }
                System.out.println("STORE TRAINING DATA IN: " + trainingFile.getAbsolutePath());
				this.trainingFileWriter = new PrintWriter(this.trainingFile);
			} catch (IOException e) {
				e.printStackTrace();
			}			
    	}
    	
    	for (Paragraph paragraph : paragraphSet.getParagraphs())
    		for (Sentence sentence : paragraph.getSentences()) {
    			
    	    	Map<Integer, Annotation> annotationsToWrap = new HashMap<Integer, Annotation>();
    	    	if ( wrap != null ){
    	    		for ( Annotation an : sentence.getAnnotations(wrap) ){
    	    			annotationsToWrap.put(an.getBegin(), an);
    	    		}
    	    	}
    	    	    			
    			List<Token> tokens = sentence.getTokens();    			
    			for (int i = 0; i < tokens.size(); i++) {
    				String oStr = "";    				
    				for (String feature: usedFeatures){
    					Token token = tokens.get(i);
    					Annotation anWrap = annotationsToWrap.get(i);
    					if ( anWrap != null ){
    						i = anWrap.getEnd();
    						token = anWrap.getHeadToken();
    						Logger.getLogger(this.getClass()).info("Annotation wrapped: " + anWrap.toString() + " into " + token.getOrth());
    					}
						try {
							String val = token.getAttributeValue(feature);
							if ( val != null)
								val = val.length()==0 ? "NULL" : val.replaceAll("\\s+", "_");
							oStr += " " + val;
						}
						catch (ArrayIndexOutOfBoundsException e){
							System.out.println("Error: Feature used by CRF chunker not in attribute index: " + feature);
							System.exit(1);
						}						
    				}
                    String tokClass = sentence.getTokenClassLabel(i, this.types);
                    oStr += " " + tokClass;
    				this.trainingFileWriter.write(oStr.trim() + "\n");
    			}
    			this.trainingFileWriter.write("\n");
    		}
    		
    	this.trainingFileWriter.flush();    		
    }


	/**
	 * Kompilacja chunkera.
	 * W przypadku CRF zostaje zamknięty tymczasowy plik z danymi treningowymi, po czym
	 * zostaje uruchomiony crf_learn. Wynikiem przetwarzania jest plik z modelem.
	 */
    private void compileTagger() throws Exception {
    	this.trainingFileWriter.close();
        if(this.template == null){
            throw new Exception("Template for CrfppChunker not set. (required in train mode)");
        }
    	CRFcmd cmd = new CRFcmd();
        File templateFile;
        try {
            templateFile = File.createTempFile("template", ".tpl");
			templateFile.deleteOnExit();
            TemplateFactory.store(this.template, templateFile.getAbsolutePath());
        } catch (Exception e) {
            throw new Exception("Error while creating template for CrfppChunker:\n"+e);
        }
        cmd.file_template = templateFile.getAbsolutePath();
    	cmd.file_model = this.model_filename;
    	cmd.file_iob = this.trainingFile.getAbsolutePath();
    	cmd.threads = this.threads;
    	
        try {
			Process p = Runtime.getRuntime().exec(cmd.get_crf_learn());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line;
            while ((line = input.readLine()) != null) {
                ConsolePrinter.log(line);
            }
            
            boolean wasError = false;
            while ((line = error.readLine()) != null) {
                System.out.println(">> Error: " + line);
                wasError = true;
            }
            if (wasError)
            	throw new Error("There was a problem with training CRF");
            
            this.deserialize(cmd.file_model);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * Wczytuje chunker z modelu binarnego.
     * @param model_filename
     */
	@Override
    public void deserialize(String model_filename){
    	String parameters = String.format("-m %s -v 3 -n 1", model_filename);
    	this.tagger = new Tagger(parameters);
    }

	@Override
	public void serialize(String filename) {
	}

	@Override
	public void close() {
		this.tagger.delete();
		
	}

    public CrfTemplate getTemplate(){
        return template;
    }
	
	/**
	 * Klasa pomocnicza reprezentuje obiekt do generowania wywołania komendy crf
	 */
	class CRFcmd{
		public String file_template = "";
		public String file_iob = "";
		public String file_model = "";
		public int threads = 1;
		
		public String get_crf_learn(){
			String cmd = "crf_learn %s %s %s -f 5 -c 1 -p %d";
			return String.format(cmd, 
					this.file_template, 
					this.file_iob, 
					this.file_model, 
					this.threads);
		}
	}
	
	public void setTemplate(CrfTemplate template) {
		this.template = template;
	}

	public void setModelFilename(String modelFilename) {
		this.model_filename = modelFilename;		
	}

    public void setTrainingDataFilename(String trainingDataFilename) {
        this.trainingDataFileName = trainingDataFilename;
    }

	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				chunkings.put(sentence, this.chunkSentence(sentence));
		return chunkings;
	}

}
