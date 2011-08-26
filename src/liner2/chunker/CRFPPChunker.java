package liner2.chunker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liner2.structure.Chunking;
import liner2.structure.Chunk;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
import liner2.structure.Token;

import org.chasen.crfpp.Tagger;

public class CRFPPChunker extends Chunker 
	implements TrainableChunkerInterface, DeserializableChunkerInterface, SerializableChunkerInterface {
	
	private File trainingFile = null;
	private PrintWriter trainingFileWriter = null;
	private Tagger tagger = null;
	private Pattern p = Pattern.compile("([IB])-(.*)");
	private String template_filename = null;
	private String model_filename = null;
	private int threads = 1;

	
    public CRFPPChunker(int threads) {
		this.threads = threads;
    }

    /**
     * Reads output from the external CRF tagger. 
     * Transforms the result from IOB format into a list of annotations.
     * @param cSeq --- text to tag
     * @return chunking with annotations
     */

	@Override
	public Chunking chunkSentence(Sentence sentence){
		this.sendDataToTagger(sentence);
		return this.readTaggerOutput(sentence);
	}
	
	private void sendDataToTagger(Sentence sentence){
//      String[] tokens = cSeq.toString().split("\\s");
		//
//		        if ( tokens.length != RegexLineTagParser.currentSentenceFeatures.size() ){
//		        	String text = "Incompatible number of tokens.\n";
//		        	text += "Sentence: >>>" + cSeq + "<<<";
//		        	text += "tokens.length=" + tokens.length + " \n";
//		        	text += "RegexLineTagParser.currentSentenceFeatures.size()=" + RegexLineTagParser.currentSentenceFeatures.size();
//		        	RegexLineTagParser.printFeatures();
//		        	throw new Error(text);
//		        }
//		        
//		        tagger.clear();
//		        
//		        for (int i = 0; i < tokens.length; i++) {
//		            String oStr = "";
//		            for ( int j=0; j<RegexLineTagParser.currentSentenceFeatures.get(i).length; j++)            
//		            	oStr += " " + RegexLineTagParser.currentSentenceFeatures.get(i)[j];          
//		                        
//		            tagger.add(oStr.trim());
//		        }
//		        
//		        tagger.parse();  		
	}
	
	private Chunking readTaggerOutput(Sentence sentence){
        Chunking chunking = new Chunking(sentence);
//        String line = "";
//        String type = null;
//        int from = 0;
//
//        for (int i = 0; i < tagger.size(); ++i) {     
//            Matcher m = p.matcher(tagger.y2(i));
//                                  
//            if ( type != null && ( !m.matches() || m.group(1).equals("B") ) ){
//            	chunking.add(ChunkFactory.createChunk(from, line.length(), type));
//            	type = null;
//            	from = 0;
//            }
//            
//            if ( m.matches() && m.group(1).toString().equals("B") ){
//            	from = line.length()==0 ? 0 : line.length()+1;
//            	type = m.group(2);
//            }
//            
//            line += (line.length()>0 ? " " : "" ) +  tagger.x(i, 0);
//        }    
//
//        if ( type != null)
//        	chunking.add(ChunkFactory.createChunk(from, line.length(), type));

        return chunking;
    }
	
	            
    @Override
	public void train(ParagraphSet paragraphSet) {
		this.prepareTrainingData(paragraphSet);
		this.compileTagger();
    }

    private void prepareTrainingData(ParagraphSet paragraphSet) {
//      public void handle(String[] toks, String[] whitespaces, String[] tags) {
    	// Utwórz tymczasowy plik do zapisu danych treningowych
    	if ( this.trainingFileWriter == null ){
    		try {
				//this.temporary_iob = File.createTempFile("crf", ".iob");
				//this.temporary_iob.deleteOnExit();
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
    				for (int j = 0; j < numAttrs; j++)
    					oStr += " " + tokens.get(i).getAttributeValue(j);
    				Chunk chunk = sentence.getChunkAt(i);
    				if (chunk == null)
    					oStr += " O";
    				else {
    					if (chunk.getBegin() == i)
    						oStr += " B-";
    					else
    						oStr += " I-";
    					oStr += chunk.getType();
    				}
    				this.trainingFileWriter.write(oStr.trim() + "\n");
    			}
    			this.trainingFileWriter.write("\n");
    		}
    		
//        for (int i = 0; i < toks.length; i++) {
//            String oStr = ""; // orth
//            for ( int j=0; j<RegexLineTagParser.currentSentenceFeatures.get(i).length; j++)            
//            	oStr += " " + RegexLineTagParser.currentSentenceFeatures.get(i)[j];
//            oStr += " " + tags[i];
//            this.trainingFileWriter.write(oStr.trim() + "\n");
//        }        
//        this.trainingFileWriter.write("\n");    	
    }
    
    /**
     * Kompilacja chunkera.
     * W przypadku CRF zostaje zamknięty tymczasowy plik z danymi treningowymi, po czym
     * zostaje uruchomiony crf_learn. Wynikiem przetwarzania jest plik z modelem.
     */
    private void compileTagger() {
    	this.trainingFileWriter.close();
    	
    	CRFcmd cmd = new CRFcmd();
    	cmd.file_template = this.template_filename;
    	cmd.file_model = this.model_filename;
    	cmd.file_iob = this.trainingFile.getAbsolutePath();
    	cmd.threads = this.threads;
    	
    	System.out.println();
    	
        try {
			Process p = Runtime.getRuntime().exec(cmd.get_crf_learn());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		this.tagger.delete();
		
	}
		
	
	/**
	 * Load external library.
	 */
	static {
		try {
			System.loadLibrary("CRFPP");
		  } catch (UnsatisfiedLinkError e) {
		    System.err.println("Cannot load the libCRFPP.so native code.\nRun: java -Djava.library.path=./lib -jar liner.jar ..." + e);
		    System.exit(1);
		  }
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
			String cmd = String.format("crf_learn %s %s %s -f 5 -c 1 -p %d", this.file_template, this.file_iob, this.file_model, this.threads);
			//cmd += " -m 20";
			return cmd;
		}
	}
	
	public void setTemplateFilename(String templateFilename) {
		this.template_filename = templateFilename;		
	}

	public void setModelFilename(String modelFilename) {
		this.model_filename = modelFilename;		
	}

}
