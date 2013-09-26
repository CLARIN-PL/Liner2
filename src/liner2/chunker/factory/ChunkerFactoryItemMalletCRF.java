package liner2.chunker.factory;

import liner2.Main;
import liner2.chunker.Chunker;
import liner2.features.TokenFeatureGenerator;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;
import liner2.structure.ParagraphSet;
import liner2.tools.CorpusFactory;
import liner2.tools.Template;
import liner2.tools.TemplateFactory;
import liner2.writer.ArffStreamWriter;
import liner2.writer.WriterFactory;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 9/25/13
 * Time: 2:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChunkerFactoryItemMalletCRF extends ChunkerFactoryItem {

    public ChunkerFactoryItemMalletCRF() {
        super("malletCRF:([^:]*)");
    }

    @Override
    public Chunker getChunker(String description, ChunkerManager cm) throws Exception {
        Matcher matcherMalletCRF = this.pattern.matcher(description);
        if (matcherMalletCRF.find()){
            Main.log("--> Mallet CRF Chunker train");
            String iniPath = matcherMalletCRF.group(1);
            String iniDir = new File(iniPath).getParent();

            Ini ini = new Ini(new FileReader(iniPath));
            Ini.Section main = ini.get("main");
            Ini.Section dataDesc = ini.get("data");

            int threads = Integer.parseInt(main.get("threads"));

            String inputFormat = dataDesc.get("format");
            String inputFile = dataDesc.get("source").replace("{INI_PATH}", iniDir);
            String modelFilename = main.get("store");

            ParagraphSet ps;
            if ((inputFormat.equals("iob")) || (inputFormat.equals("ccl")) || (inputFormat.equals("ccl-batch"))) {
                Main.log("--> Training on file=" + inputFile);
                StreamReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
                ps = reader.readParagraphSet();
            }
            else {
                Main.log("--> Training on corpus=" + inputFile);
                ps = CorpusFactory.get().query(inputFile);
            }
            TokenFeatureGenerator gen = new TokenFeatureGenerator(cm.opts.features);
            gen.generateFeatures(ps);

            String templateName = main.get("template");
            Template template = cm.opts.getTemplate(templateName);

//            templa
////            File templateFile = File.createTempFile("template", ".tpl");
////            TemplateFactory.store(template, templateFile.getAbsolutePath(), ps.getAttributeIndex());
//
//            CrfppChunker chunker = new CrfppChunker(threads);
//            chunker.setTemplateFilename(templateFile.getAbsolutePath());
//            chunker.setModelFilename(modelFilename);
//
//            ((TrainableChunkerInterface) chunker).train(ps);
//
//            return chunker;
            return null;
        }
        else
            return null;
    }
}
