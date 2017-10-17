package g419.crete.core;

import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.crete.core.annotation.AbstractAnnotationSelector;
import g419.crete.core.annotation.AnnotationSelectorFactory;
import g419.crete.core.cluster.AbstractAnnotationClusterer;
import g419.crete.core.cluster.AnnotationClustererFactory;


public class Crete {

	public static final String CLUSTERER_PROPERTY = "clusterer";
	public static final String SELECTOR_PROPERTY = "selector";
	private AbstractAnnotationClusterer clusterer;
	private AbstractAnnotationSelector selector;
	
//	private RankingSvmClassifier classifier;
//	private ClusterRankingInstanceGenerator generator;
//	private ClusterRankingInstanceConverter converter;
//	private List<ClusterRankingInstance> instances;
//	private List<ClusterRankingInstance> instancesTest;
	
	public Crete(){
		//temp
//		instances = new ArrayList<ClusterRankingInstance>();
//		instancesTest = new ArrayList<ClusterRankingInstance>();
//		generator = new ClusterRankingInstanceGenerator();
//		converter = new ClusterRankingInstanceConverter();
//		classifier = new RankingSvmClassifier();
		// old ??
		clusterer = AnnotationClustererFactory.getFactory().getClusterer(CreteOptions.getOptions().properties.getProperty(CLUSTERER_PROPERTY));
		String selectorName = CreteOptions.getOptions().properties.getProperty(SELECTOR_PROPERTY);
		selector = AnnotationSelectorFactory.getFactory().getInitializedSelector(selectorName);
        System.out.println("CRETE created");
    }
    
    public void processDocument(Document document){
    	// Process
    	AnnotationClusterSet relations = clusterer.resolveRelations(document, selector);
    	// Post-process
    	for(Relation relation : relations.getRelationSet(null).getRelations())
    		document.addRelation(relation);
    }

//    public void addDocumentTrain(Document document){
//    	instances.addAll((Collection<? extends ClusterRankingInstance>) generator.generateTrainingInstances(document, selector));
//    	instancesTest.addAll((Collection<? extends ClusterRankingInstance>) generator.generateTestInstances(document, selector));
//    }
//    
//    public void train(){
//    	ArrayList<SparseVector> svInstances = new ArrayList<SparseVector>();
//    	ArrayList<Integer> svLabels = new ArrayList<Integer>();
//    	for(AbstractCreteInstance<SubtractableSparseVector, Integer> instance : instances){
//    		svInstances.add(instance.getFeatures());
//    		svLabels.add(instance.getLabel());
//    	}
//    	
//    	classifier.train(svInstances, svLabels);
//    }
//    
//    public void classify(){
//    	ArrayList<SparseVector> svInstances = new ArrayList<SparseVector>();
//    	ArrayList<Integer> svLabels = new ArrayList<Integer>();
//    	for(AbstractCreteInstance<SubtractableSparseVector, Integer> instance : instancesTest){
//    		svInstances.add(instance.getFeatures());
//    		svLabels.add(instance.getLabel());
//    	}
//    	
//    	List<Integer> cLabels = classifier.classify(svInstances);
//    	
//    	for(int i = 0; i <  svLabels.size(); i++)
//    		System.out.println("Original Label: " + svLabels.get(i) + " ; -- ; Predicted label: " + cLabels.get(i) );
//    	
//    }
//    
}
