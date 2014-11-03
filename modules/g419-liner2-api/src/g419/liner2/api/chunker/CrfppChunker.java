package g419.liner2.api.chunker;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.CrfTemplate;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.Logger;
import g419.liner2.api.tools.TemplateFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chasen.crfpp.Tagger;

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
	
    public CrfppChunker() {
		this.types = new ArrayList<Pattern>();
    }
    
    public CrfppChunker(int threads, List<Pattern> types){
		this.threads = threads;
		this.types = types;    	
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
		AnnotationSet chunking = new AnnotationSet(sentence);
        HashMap<String, Annotation> annsByType = new HashMap<String, Annotation>();

        // Prepare date and send them to the API
        tagger.clear();
		int numAttrs = sentence.getAttributeIndexLength();
		String val = null;
		for (Token token : sentence.getTokens()) {
			StringBuilder oStr = new StringBuilder();
			for (int i = 0; i < numAttrs; i++){
				oStr.append(" ");
				val = token.getAttributeValue(i);
				if ( val != null){
					val = val.replaceAll("\\s+", "_");
                    val = val.length()==0 ? "NULL" : val;
                                }
				oStr.append(val);
			}
			tagger.add(oStr.toString().trim());
		}
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
                        Annotation newAnn = new Annotation(i, annType, sentence);
                        chunking.addChunk(newAnn);
                        annsByType.put(annType, newAnn);
                    }
                    else if(m.group(1).equals("I")){
                        if(annsByType.containsKey(annType))
                            annsByType.get(annType).addToken(i);
                    }
                }
            }
        }

        return chunking;
    }
		            
    @Override
	public void train() throws Exception {
        Logger.log("Training CRF classifer using features:\n" + this.template.printFeatures());
    	this.trainingFileWriter.close();
		this.compileTagger();
    }

    @Override
    public void addTrainingData(Document paragraphSet) {
        Logger.log("Loading training data for CRF from document:" + paragraphSet.getName());
//        System.out.println(paragraphSet.getAttributeIndex().allAtributes().toString());
    	// Utwórz tymczasowy plik do zapisu danych treningowych
    	if ( this.trainingFileWriter == null ){
    		try {
    			this.trainingFile = new File("crf_iob.txt");
				this.trainingFileWriter = new PrintWriter(this.trainingFile);
			} catch (IOException e) {
				e.printStackTrace();
			}			
    	}
    	
    	for (Paragraph paragraph : paragraphSet.getParagraphs())
    		for (Sentence sentence : paragraph.getSentences()) {
    			int numAttrs = sentence.getAttributeIndexLength();
    			ArrayList<Token> tokens = sentence.getTokens();    			
    			for (int i = 0; i < tokens.size(); i++) {
    				String oStr = "";    				
    				for (int j = 0; j < numAttrs; j++){
//                        System.out.println(tokens.get(i).getOrth() + ": " + tokens.get(i).attrIdx.getName(j));
    					String val = tokens.get(i).getAttributeValue(j);
    					if ( val != null)
    						val = val.length()==0 ? "NULL" : val.replaceAll("\\s+", "_");
    					oStr += " " + val;
    				}
                    String tokClass = sentence.getTokenClassLabel(i, this.types);
                    oStr += " " + tokClass;
//                    System.out.println(oStr);
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
			String line = null;
            while ((line = input.readLine()) != null) {
                Logger.log(line);
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
    	String parameters = String.format("-m %s -v 3 -n 64", model_filename);
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

	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : ps.getParagraphs() )
			for (Sentence sentence : paragraph.getSentences())
				chunkings.put(sentence, this.chunkSentence(sentence));
		return chunkings;
	}

}
