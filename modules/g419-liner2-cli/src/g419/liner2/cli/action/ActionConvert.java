package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.converter.Converter;
import g419.liner2.api.converter.ConverterFactory;
import g419.liner2.api.features.TokenFeatureGenerator;

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

        LinerOptions.getGlobal().setDefaultDataFormats("ccl", "ccl");

        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(
			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FILE),
			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT));

        TokenFeatureGenerator gen = null;
        if (!LinerOptions.getGlobal().features.isEmpty()) {
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }
        Converter converter = null;
        if (!LinerOptions.getGlobal().convertersDesciptions.isEmpty()) {
            converter = ConverterFactory.createPipe(LinerOptions.getGlobal().convertersDesciptions);
        }

        Document ps = reader.nextDocument();
        while(ps != null) {
            if(gen != null) {
                gen.generateFeatures(ps);
            }

            if (converter != null) {
                converter.apply(ps);
            }

            String output_format = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FORMAT);
            String output_file = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FILE);
            AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(output_file, output_format);
            writer.writeDocument(ps);
            ps = reader.nextDocument();
        }
	}

}
