package g419.tools.action;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.xml.sax.SAXException;

import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.parser.CclSaxParser;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Tag;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;

public class ActionLemmatizerData extends Action {
	
	private String inputFilename = null;
	
	public ActionLemmatizerData() {
		super("lemmatizer-data");
		this.setDescription("generuje dane do lematyzacjia dla anotacji");
        this.options.addOption(CommonOptions.getInputFileNameOption());
	}
	
	/**
	 * Parse action options
	 * @param args The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);        
        this.inputFilename = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE_LONG);
    }

	@Override
	public void run() throws Exception {		
		InputStream stream = new FileInputStream(this.inputFilename);
		if ( this.inputFilename.endsWith(".gz")){
			stream = new GZIPInputStream(stream);
		}
		TokenAttributeIndex index = new TokenAttributeIndex();
		index.addAttribute("orth");
		index.addAttribute("base");
		index.addAttribute("ctag");
		
		new CclSaxParserStream(this.inputFilename, stream, index);
		stream.close();
	}

	
	private class CclSaxParserStream extends CclSaxParser {
		
		public CclSaxParserStream(String uri, InputStream is, TokenAttributeIndex attributeIndex)
				throws DataFormatException, ParserConfigurationException, SAXException, IOException {
			super(uri, is, attributeIndex);
		}

		@Override
		public void onParagraphRead(){
			for ( Paragraph p : this.paragraphs ){
				for ( Sentence s : p.getSentences() ){
					for ( Annotation an : s.getChunks() ){
						StringBuilder orth = new StringBuilder();
						StringBuilder base = new StringBuilder();
						StringBuilder ctag = new StringBuilder();
						StringBuilder space = new StringBuilder();
						for ( Token t : an.getTokenTokens()){
							orth.append(t.getOrth() + " ");
							boolean first = true;
							for ( Tag tag : t.getDisambTags() ){
								if (!first){
									base.append("|");
									ctag.append("|");
								}
								base.append(tag.getBase());
								ctag.append(tag.getCtag());
								first = false;
							}							
							base.append(" ");
							ctag.append(" ");
							space.append((t.getNoSpaceAfter() ? "False" : "True") + " ");
						}
						String line = "%s\t%s\t%s\t%s\t%s\t%s";
						System.out.println(String.format(line, 
								base.toString().trim(), orth.toString().trim(), base.toString().trim(), 
								ctag.toString().trim(), space.toString().trim(), an.getType()));
					}
				}
			}
			// Po przetworzeniu zapomnij o paragrafie
			this.paragraphs.clear();
		}
		
	}
	
}
