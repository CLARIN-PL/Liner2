package g419.crete.cli.action;

import g419.lib.cli.CommonOptions;
import g419.lib.cli.action.Action;

public class ActionTrain extends Action {

	public ActionTrain() {
		super("train");
		
		this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileFormatOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getModelFileOption());
	}

	@Override
	public void parseOptions(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
