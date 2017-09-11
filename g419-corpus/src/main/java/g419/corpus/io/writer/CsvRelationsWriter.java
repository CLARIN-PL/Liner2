package g419.corpus.io.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;

/**
 * Zapisuje dane w formacie csv-relations. 
 * Opis formatu znajduje się na stronie: 
 * http://nlp.pwr.wroc.pl/redmine/projects/inforex-liner/wiki/csv-relations
 * 
 * @author czuk
 *
 */
public class CsvRelationsWriter extends AbstractDocumentWriter {

	private CSVPrinter writer = null;
	
	public CsvRelationsWriter(OutputStream os) throws IOException{
		this.writer = new CSVPrinter(new OutputStreamWriter(os), CSVFormat.DEFAULT);
	}
		
	@Override
	public void writeDocument(Document document) {
		for (Relation relation : document.getRelations().getRelations()){
			try {
				List<String> record = new ArrayList<String>();
				record.add(document.getName());
				record.add(relation.getType());
				this.annotationAttributes(record, relation.getAnnotationFrom());
				this.annotationAttributes(record, relation.getAnnotationTo());
				record.add(relation.getAnnotationFrom().getSentence().toString());
				
				writer.printRecord(record);
				
			} catch (IOException e) {
				e.printStackTrace();
				Logger.getLogger(this.getClass()).error("Writing error", e);
			}
		}
	}

	/**
	 * Put annotation attributes to the list.
	 * @param record
	 * @param an
	 */
	private void annotationAttributes(List<String> record, Annotation an){
		record.add(an.getSentence().getId());
		record.add("" + an.getBegin());
		record.add("" + an.getBegin());
		record.add(an.getType());
		record.add(an.getText());
		record.add(an.getBaseText(false));		
	}
	
	@Override
	public void flush() {
		try {
			this.writer.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void close() {
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
