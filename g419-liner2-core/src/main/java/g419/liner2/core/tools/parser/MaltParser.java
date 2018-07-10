package g419.liner2.core.tools.parser;

import com.google.common.collect.Maps;
import g419.corpus.structure.Sentence;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MaltParser {

    final static Logger logger = LoggerFactory.getLogger(MaltParser.class);
    private static final HashMap<String, MaltParserService> parsers = Maps.newHashMap();

    final MaltParserService parser;

    public MaltParser(final String modelPath) {
        parser = parsers.computeIfAbsent(modelPath, r -> MaltParser.loadParser(Paths.get(modelPath)));
    }

    public static MaltParserService loadParser(final Path modelPath) {
        try {
            final MaltParserService parser = new MaltParserService();
            parser.initializeParserModel(String.format("-c %s -m parse -w %s", modelPath.toFile().getName(), modelPath.toFile().getParent()));
            return parser;
        } catch (final MaltChainedException e) {
            logger.error("Failed to load MaltParser model {}", modelPath, e);
        }
        return null;
    }

    public DependencyStructure parseTokensToDependencyStructure(final String[] dataForMalt) throws MaltChainedException {
        return parser.parse(dataForMalt);
    }

    public String[] parseTokens(final String[] dataForMalt) throws MaltChainedException {
        return dataForMalt.length == 0 ? new String[0] : parser.parseTokens(dataForMalt);
    }

    public void parse(final MaltSentence sentence) throws MaltChainedException {
        sentence.setMaltDataAndLinks(parseTokens(sentence.getMaltData()));
    }

    public MaltSentence parse(final Sentence sentence, final Map<String, String> pos) {
        try {
            final MaltSentence maltSentence = new MaltSentence(sentence, pos);
            parse(maltSentence);
            return maltSentence;
        } catch (final MaltChainedException ex) {
            throw new RuntimeException(ex);
        }
    }

}

