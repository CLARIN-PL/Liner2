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
		// -i format wejściowy: iob, ccl
		// -o format wyjściowy: iob, ccl
		// -f (--file) plik wejściowy, jeżeli brak to czyta z stdin
		// -t (--target) plik wyjściowy, jeżeli brak, to na stdout
		
		StreamReader reader = ReaderFactory.get().getStreamReader(
			LinerOptions.get().getOption(LinerOptions.OPTION_INPUT_FILE),
			LinerOptions.get().getOption(LinerOptions.OPTION_INPUT_FORMAT));
		ParagraphSet ps = reader.readParagraphSet();
		
		StreamWriter writer = WriterFactory.get().getStreamWriter(
			LinerOptions.get().getOption(LinerOptions.OPTION_OUTPUT_FILE),
			LinerOptions.get().getOption(LinerOptions.OPTION_OUTPUT_FORMAT));
		writer.writeParagraphSet(ps);
	}

}
