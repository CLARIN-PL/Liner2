package g419.crete.api.classifier.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import weka.classifiers.Classifier;

public class WekaModel extends Model<Classifier>{

	public WekaModel(Classifier model) {
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
