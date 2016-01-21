package g419.spatial.action;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Frame;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.structure.SpatialRelation;
import g419.spatial.tools.SpatialRelationRecognizer;

public class ActionPipe extends Action {
	
	private String inputFilename = null;
	private String inputFormat = null;
	
	private String outputFilename = null;
	private String outputFormat = null;
	
	/**
	 * 
	 */
	public ActionPipe() {
		super("pipe");
		this.setDescription("recognize spatial expressions and add them to the document as a set of frames");
		this.options.addOption(CommonOptions.getInputFileNameOption());		
		this.options.addOption(CommonOptions.getInputFileFormatOption());
		this.options.addOption(CommonOptions.getOutputFileFormatOption());
		this.options.addOption(CommonOptions.getOutputFileNameOption());
	}
	
	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
        this.outputFilename = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.outputFormat = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT);
    }

	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.inputFilename, this.inputFormat);				
		SpatialRelationRecognizer recognizer = new SpatialRelationRecognizer();
		AbstractDocumentWriter writer = null;
				
		if ( this.outputFilename == null ){
			writer = WriterFactory.get().getStreamWriter(System.out, this.outputFormat);
		}
		else{
			writer = WriterFactory.get().getStreamWriter(this.outputFilename, this.outputFormat);
		}
		
		Document document = null;
		while ( ( document = reader.nextDocument() ) != null ){					
			System.out.println("=======================================");
			System.out.println("Document: " + document.getName());
			System.out.println("=======================================");
			
			for (Paragraph paragraph : document.getParagraphs()){
				for (Sentence sentence : paragraph.getSentences()){
					
					List<SpatialRelation> relations = recognizer.recognize(sentence);
					
					for ( SpatialRelation rel : relations ){
						Frame f = SpatialRelationRecognizer.convertSpatialToFrame(rel);
						document.getFrames().add(f);
					}
				}
			}
			writer.writeDocument(document);
		}
		writer.close();
			
		reader.close();
	}
		
}
