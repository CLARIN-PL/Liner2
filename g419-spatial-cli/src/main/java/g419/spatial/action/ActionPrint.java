package g419.spatial.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.formatter.ISpatialExpressionFormatter;
import g419.spatial.formatter.SpatialExpressionFormatterFactory;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.tools.DocumentToSpatialExpressionConverter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;

import java.util.List;

public class ActionPrint extends Action {

	public static final String OPTION_OUTPUT_ARG = "tree|tsv";

	private String inputFilename;
	private String inputFormat;
	private String output;

	/**
	 *
	 */
	public ActionPrint() {
		super("print");
		this.setDescription("Reads spatial expressions from the documents and print them on the screen");
		this.options.addOption(CommonOptions.getInputFileNameOption());		
		this.options.addOption(CommonOptions.getInputFileFormatOption());
		this.options.addOption(Option.builder(CommonOptions.OPTION_OUTPUT_FORMAT)
				.longOpt(CommonOptions.OPTION_OUTPUT_FORMAT_LONG).hasArg().argName(OPTION_OUTPUT_ARG).required().build());
	}
	
	/**
	 * Parse action options
	 * @param args The array with command line parameters
	 */
	@Override
	public void parseOptions(final CommandLine line) throws Exception {
        inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
        output = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT);
    }

	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.inputFilename, this.inputFormat);
		DocumentToSpatialExpressionConverter converter = new DocumentToSpatialExpressionConverter();
		ISpatialExpressionFormatter formatter = SpatialExpressionFormatterFactory.create(output);
		formatter.getHeader().forEach(System.out::println);
		Document document = null;
		while ( ( document = reader.nextDocument() ) != null ){
			List<SpatialExpression> spatialExpressions = converter.convert(document);
			formatter.format(document, spatialExpressions).forEach(System.out::println);
		}
		reader.close();
	}

}
