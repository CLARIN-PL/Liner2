package liner2.action;

import liner2.features.TokenFeatureGenerator;
import liner2.structure.ParagraphSet;

import liner2.reader.StreamReader;
import liner2.reader.ReaderFactory;

import liner2.tools.Template;
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
			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FILE),
			LinerOptions.getGlobal().getOption(LinerOptions.OPTION_INPUT_FORMAT));
		ParagraphSet ps = reader.readParagraphSet();

        if (!LinerOptions.getGlobal().features.isEmpty()){
            TokenFeatureGenerator gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
            gen.generateFeatures(ps);
        }

        String output_format = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FORMAT);
        String output_file = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_OUTPUT_FILE);
        StreamWriter writer;
        if (output_format.equals("arff")){
            Template arff_template = LinerOptions.getGlobal().getArffTemplate();
            writer = WriterFactory.get().getArffWriter(output_file, arff_template);
        }
        else{
            writer = WriterFactory.get().getStreamWriter(output_file, output_format);
        }
		writer.writeParagraphSet(ps);
	}

}
