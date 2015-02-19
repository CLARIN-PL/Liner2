package g419.crete.api.classifier;

import g419.crete.api.classifier.model.WekaModel;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48graft;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;


public class WekaDecisionTreesClassifier extends WekaClassifier<Classifier, Integer>{
		
	public WekaDecisionTreesClassifier(List<String> features) {
		super(features);
	}

	@Override
	public List<Integer> classify(List<Instance> instances) {
		
		Instances clasInst = new Instances(this.instances); 
		for(Instance instance : instances) clasInst.add(instance);
		clasInst.setClass(attributes.get(attributes.size() - 1));
		
		ArrayList<Integer> labels = new ArrayList<Integer>();
		
		Classifier cls = this.model.getModel();
		
		for(int i = 0; i < clasInst.numInstances(); i++){
			Instance instance = clasInst.instance(i);
			try {
				labels.add((int)Math.round(cls.classifyInstance(instance)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return labels;
	}

	@Override
	public void train() {
//		try {
//			crossValidate(10);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		Instances tInstances = instances;
		for(Instance instance : trainingInstances) tInstances.add(instance);
		tInstances.setClass(attributes.get(attributes.size() - 1));
		
		try {
			NumericToNominal numToNom =  new NumericToNominal();
			numToNom.setInputFormat(tInstances);
			numToNom.setAttributeIndicesArray(new int[]{attributes.size() - 1});
			numToNom.setInputFormat(tInstances);
			
			tInstances = Filter.useFilter(tInstances, numToNom);
			
			SMOTE smote = new SMOTE();
			smote.setInputFormat(tInstances);
			smote.setClassValue("2");
			
			tInstances = Filter.useFilter(tInstances, smote);
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
	
	public void crossValidate(int folds) throws Exception{
		Instances randData = new Instances(this.instances); 
		for(Instance instance : trainingInstances) randData.add(instance);
		randData.setClass(attributes.get(attributes.size() - 1));
		
		Random rand = new Random();   // create seeded number generator
//		Instances randData = new Instances(this.instances);   // create copy of original data
		
		
		try {
			NumericToNominal numToNom =  new NumericToNominal();
			numToNom.setInputFormat(randData);
			numToNom.setAttributeIndicesArray(new int[]{attributes.size() - 1});
			numToNom.setInputFormat(randData);
			randData = Filter.useFilter(randData, numToNom);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
		SMOTE smote = new SMOTE();
		smote.setInputFormat(randData);
		smote.setClassValue("2");
		randData = Filter.useFilter(randData, smote);
		
//		Resample resample = new Resample();
//		resample.setInputFormat(randData);
//		resample.setBiasToUniformClass(1.0);
//		
//		randData  = Filter.useFilter(randData, resample);
//		
		
		randData.randomize(rand);
		
		randData.stratify(folds);
		
		Evaluation eval = new Evaluation(randData);
		   
		for (int n = 0; n < folds; n++) {
		   Instances train = randData.trainCV(folds, n);
		   Instances test = randData.testCV(folds, n);
		   Evaluation evalLocal = new Evaluation(test);
		   
		   J48graft j48 = new J48graft();
		   Classifier cls = (Classifier) j48;
		   
		   	Classifier clsCopy = Classifier.makeCopy(cls);
	        clsCopy.buildClassifier(train);
	        eval.evaluateModel(clsCopy, test);
		    evalLocal.evaluateModel(clsCopy, test);
		    System.out.println(evalLocal.toSummaryString());
		    System.out.println(evalLocal.toMatrixString());
		    System.out.println(clsCopy);
		    
		    this.model = new  WekaModel(clsCopy);
		 }
		
		System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));
		System.out.println(eval.toMatrixString());
		
	}
	
	private void displayDebugInfo(Classifier cModel, Instances tInstances) throws Exception{
		Evaluation eTest = new Evaluation(tInstances);
		eTest.evaluateModel(cModel, tInstances);
		String strSummary = eTest.toSummaryString();
		System.out.println(strSummary);
//		System.out.println(cModel.toString());
		// Get the confusion matrix
		 System.out.println(eTest.toMatrixString());
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
