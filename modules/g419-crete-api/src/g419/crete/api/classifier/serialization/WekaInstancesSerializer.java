package g419.crete.api.classifier.serialization;

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class WekaInstancesSerializer extends Serializer<Instances> {

	public WekaInstancesSerializer(Instances model) {
		super(model);
	}

	@Override
	public void persist(String path) {
		try {
			ArffSaver saver = new ArffSaver();
			saver.setInstances(model);
			saver.setFile(new File(path));
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void load(String path) {
		try{
			ArffLoader loader = new ArffLoader();
			loader.setFile(new File(path));
			//TODO: finish
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
