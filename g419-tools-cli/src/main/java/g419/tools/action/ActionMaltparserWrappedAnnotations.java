package g419.tools.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import org.apache.commons.cli.CommandLine;
import org.maltparser.core.exception.MaltChainedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ActionMaltparserWrappedAnnotations extends Action {

    final Logger logger = LoggerFactory.getLogger(getClass());
    String inputFormat;
    String inputName;
    String maltModel;
    List<Pattern> annotationTypePatterns;

    public ActionMaltparserWrappedAnnotations() {
        super("maltparser-wrapped-annotations");
        setDescription("Parse document(s) with wrapped annotation of specified types");
        options.addOption(CommonOptions.getOutputFileNameOption());
        options.addOption(CommonOptions.getInputFileFormatOption());
        options.addOption(CommonOptions.getInputFileNameOption());
        options.addOption(CommonOptions.getMaltparserModelFileOption());
        options.addOption(CommonOptions.getAnnotationTypePatterns());
    }

    @Override
    public void parseOptions(final CommandLine line) throws Exception {
        inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
        inputName = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        maltModel = line.getOptionValue(CommonOptions.OPTION_MALT);
        annotationTypePatterns =
                Arrays.stream(Optional.ofNullable(line.getOptionValues(CommonOptions.OPTION_ANNOTATION_PATTERN)).orElse(new String[]{}))
                        .map(p -> Pattern.compile(p))
                        .collect(Collectors.toList());
    }

    @Override
    public void run() throws Exception {
        final MaltParser malt = new MaltParser(maltModel);
        try (AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputName, inputFormat)) {
            while (reader.hasNext()) {
                reader.nextDocument().getSentences().stream()
                        .map(s -> new MaltSentence(s, annotationTypePatterns))
                        .forEach(s -> {
                            try {
                                malt.parse(s);
                                Arrays.stream(s.getMaltData()).forEach(System.out::println);
                                System.out.println();
                            } catch (MaltChainedException e) {
                                logger.warn("Failed to parse sentence with MaltParser", e);
                            }
                        });
            }
        }
    }

}
