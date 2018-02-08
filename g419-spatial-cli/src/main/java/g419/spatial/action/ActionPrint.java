package g419.spatial.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.tools.DocumentToSpatialExpressionConverter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.util.List;

public class ActionPrint extends Action {

	private String inputFilename = null;
	private String inputFormat = null;

	/**
	 *
	 */
	public ActionPrint() {
		super("print");
		this.setDescription("Reads spatial expressions from the documents and print them on the screen");
		this.options.addOption(CommonOptions.getInputFileNameOption());		
		this.options.addOption(CommonOptions.getInputFileFormatOption());
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
    }

	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.inputFilename, this.inputFormat);
		DocumentToSpatialExpressionConverter converter = new DocumentToSpatialExpressionConverter();

		Document document = null;
		while ( ( document = reader.nextDocument() ) != null ){					
			System.out.println("Document: " + document.getName());

			List<SpatialExpression> ses = converter.convert(document);
			System.out.println("Number of spatial expressions: " + ses.size());
			System.out.println();
		}
		reader.close();
	}
		
}
