package liner2.action;

import liner2.structure.ParagraphSet;

import liner2.reader.StreamReader;
import liner2.reader.ReaderFactory;

import liner2.writer.StreamWriter;
import liner2.writer.WriterFactory;

import liner2.LinerOptions;

public class ActionConvert extends Action {
	
	@Override
	public void run() throws Exception {
		// TODO Auto-generated method stub
		// Wczytuje wskazany dokument, a następnie zapisuje go pod wskazaną nazwą.
		// Parametry:
		// -i format wejściowy: iob, xces
		// -o format wyjściowy: iob, ccl
		// -f (--file) plik wejściowy, jeżeli brak to czyta z stdin
		// -t (--target) plik wyjściowy, jeżeli brak, to na stdout
		
		//System.out.println("Konwersja z "+LinerOptions.get().inputFormat+" do "+LinerOptions.get().outputFormat);
		
		StreamReader reader = ReaderFactory.get()
			.getStreamReader(LinerOptions.get());
		ParagraphSet ps = reader.readParagraphSet();
		
		StreamWriter writer = WriterFactory.get()
			.getStreamWriter(LinerOptions.get());
		writer.writeParagraphSet(ps);
	}

}
