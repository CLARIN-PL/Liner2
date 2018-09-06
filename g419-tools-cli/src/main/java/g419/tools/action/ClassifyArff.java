package g419.tools.action;

import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import org.apache.commons.cli.CommandLine;

/**
 * 
 * @author czuk
 *
 */
public class ClassifyArff extends Action {

    private String input_file = null;

    public ClassifyArff() {
        super("classify-arff");
        this.setDescription("Generuje wektor cech dla wskazanych anotacji");

        this.options.addOption(CommonOptions.getInputFileNameOption());
    }

    @Override
    public void parseOptions(final CommandLine line) throws Exception {
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    }

    @Override
    public void run() throws Exception {
    }

}
