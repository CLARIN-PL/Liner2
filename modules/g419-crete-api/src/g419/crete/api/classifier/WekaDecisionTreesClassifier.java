package g419.crete.api.classifier;

import g419.crete.api.classifier.model.Model;
import g419.crete.api.classifier.model.WekaModel;

import java.awt.BorderLayout;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48graft;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;


public class WekaDecisionTreesClassifier extends WekaClassifier<Classifier, Integer>{
		
	public WekaDecisionTreesClassifier(List<String> features) {
		super(features);
	}

	@Override
	public List<Integer> classify(List<Instance> instances) {
		return null;
	}

	@Override
	public void train() {
		Instances tInstances = instances;
		for(Instance instance : trainingInstances) tInstances.add(instance);
		tInstances.setClass(attributes.get(attributes.size() - 1));
		
		try {
			NumericToNominal numToNom =  new NumericToNominal();
			numToNom.setInputFormat(tInstances);
			numToNom.setAttributeIndicesArray(new int[]{attributes.size() - 1});
			numToNom.setInputFormat(tInstances);
			tInstances = Filter.useFilter(tInstances, numToNom);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		J48graft j48 = new J48graft();
		
		Classifier cModel = (Classifier) j48;
		try {
			cModel.buildClassifier(tInstances);
			displayDebugInfo(cModel, tInstances);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.model = new WekaModel(cModel);
	}
	
	private void displayDebugInfo(Classifier cModel, Instances tInstances) throws Exception{
		Evaluation eTest = new Evaluation(tInstances);
		eTest.evaluateModel(cModel, tInstances);
		String strSummary = eTest.toSummaryString();
		System.out.println(strSummary);
		System.out.println(cModel.toString());
		// Get the confusion matrix
		double[][] cmMatrix = eTest.confusionMatrix();
		System.out.println(cmMatrix);
	}
	
	private void display(J48graft tree) throws Exception{
		 final javax.swing.JFrame jf = new javax.swing.JFrame("Weka Classifier Tree Visualizer: J48");
	     jf.setSize(1920,1080);
	     jf.getContentPane().setLayout(new BorderLayout());
	     TreeVisualizer tv = new TreeVisualizer(null, tree.graph(), new PlaceNode2());
	     jf.getContentPane().add(tv, BorderLayout.CENTER);
	     jf.addWindowListener(new java.awt.event.WindowAdapter() {
	       public void windowClosing(java.awt.event.WindowEvent e) {
	         jf.dispose();
	       }
	     });

	     jf.setVisible(true);
	     tv.fitToScreen();
	}

	@Override public Class<Integer> getLabelClass() {return Integer.class;}
	
}
