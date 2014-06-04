package liner2.action;

import liner2.LinerOptions;
import liner2.converter.Converter;
import liner2.converter.ConverterFactory;
import liner2.features.TokenFeatureGenerator;
import liner2.reader.AbstractDocumentReader;
import liner2.reader.ReaderFactory;
import liner2.structure.Document;
import liner2.writer.AbstractDocumentWriter;
import liner2.writer.WriterFactory;

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
