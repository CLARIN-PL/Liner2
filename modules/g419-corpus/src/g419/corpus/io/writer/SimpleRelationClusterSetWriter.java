package g419.corpus.io.writer;

import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.Document;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class SimpleRelationClusterSetWriter extends AbstractDocumentWriter {

	private BufferedWriter writer;
	
	public SimpleRelationClusterSetWriter(OutputStream os){
		this.writer = new BufferedWriter(new OutputStreamWriter(os));
	}
	
	
	@Override
	public void writeDocument(Document document) {
		for(AnnotationCluster cluster : AnnotationClusterSet.fromRelationSet(document.getRelations()).getClusters())
			try {
				writer.write(cluster.toString() + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
