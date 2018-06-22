package g419.crete.core.classifier.serialization;

import weka.classifiers.Classifier;

import java.io.*;

public class WekaModelSerializer extends Serializer<Classifier>{

	public WekaModelSerializer(Classifier model) {
		super(model);
	}

	@Override
	public void persist(String path) {
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(this.model);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void load(String path) {
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new FileInputStream(path));
			this.model = (Classifier) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
