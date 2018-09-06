package g419.crete.core.resolver;

import g419.corpus.structure.*;
import g419.crete.core.annotation.AbstractAnnotationSelector;
import g419.crete.core.instance.MentionPairClassificationInstance;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
import weka.clusterers.HierarchicalClusterer;
import weka.core.*;

import java.util.*;
import java.util.stream.Collectors;

public class WekaRandomForestMentionPairClusterClassifyResolver extends AbstractCreteResolver<Classifier, MentionPairClassificationInstance<Double>,  Instance, Double> {

	static class NamedDistanceFunction extends EuclideanDistance {

		HashMap<String, Integer> instanceMapping;
		double[][] distMatrix;

		public NamedDistanceFunction(HashMap<String, Integer> mapping, double[][] dist){
			this.instanceMapping = mapping;
			this.distMatrix = dist;
		}

		@Override
		public double distance(Instance instance1, Instance instance2){
			Integer mapping1 = this.instanceMapping.get(instance1.stringValue(0));
			Integer mapping2 = this.instanceMapping.get(instance2.stringValue(0));
			return this.distMatrix[mapping1][mapping2];
		}

	}

	@Override
	public Document resolveDocument(Document document, AbstractAnnotationSelector selector, AbstractAnnotationSelector singletonSelector){
		System.out.println("Classifying document: "+document.getName());
		List<Annotation> mentions = selector.selectAnnotations(document);
		if(mentions.size() <= 1) return document;
		List<Annotation> singletons = singletonSelector.selectAnnotations(document);
		Collections.sort(mentions, new AnnotationPositionComparator());

		MentionPairClassificationInstance[][] instances = new MentionPairClassificationInstance[mentions.size()][mentions.size()];
		String[][] classifications = new String[mentions.size()][mentions.size()];
		double[][] distances = new double[mentions.size()][mentions.size()];
		double[][] correlations = new double[mentions.size()][mentions.size()];

		int currentMentionIndex = 0;
		for(Annotation mention : mentions) {
			List<MentionPairClassificationInstance<Double>> instancesForMention = this.generator.generateInstancesForMention(document, mention, mentions, singletons);
			int currentInstanceIndex = 0;
			int skip = 0;
			while(currentInstanceIndex <= instancesForMention.size()) {
				if(currentMentionIndex == currentInstanceIndex){
					currentInstanceIndex++;
					skip = 1;
					continue;
				}
//				System.out.println(
//						String.format(
//								"Assigning to instance at coordinates (%d, %d) following instance: %s (mention %s)",
//								currentMentionIndex, currentInstanceIndex,  instancesForMention.get(currentInstanceIndex - skip), mentions.get(currentInstanceIndex)
//						)
//				);
				instances[currentMentionIndex][currentInstanceIndex] = instancesForMention.get(currentInstanceIndex - skip);
				currentInstanceIndex++;
			}
			currentMentionIndex++;
		}

//		System.out.println(mentions);
		for(int i =0; i < mentions.size(); i++) {
			for (int j = 0; j < mentions.size(); j++) {
				if(i == j){
					classifications[i][j] = "" + 1;
					distances[i][j] = 0;
					correlations[i][j] = 1;
				}
				else {
//					System.out.println(instances[i][j]);
//					if(instances[i][j] == null) System.out.println(String.format("Null instance at coordinates (%d, %d)",i, j));
					double classificationResult =  classifier.classify(this.converter.convertSingleInstance(instances[i][j]));
					System.out.println(
						String.format(
								"Classification result for mention pair %s and %s is %s",
								mentions.get(i), mentions.get(j), (classificationResult == 1?"POSITIVE":"NEGATIVE")
						)
					);
					classifications[i][j] = "" + classificationResult;
					distances[i][j] = 1 - classificationResult + 0.1;
					correlations[i][j] = 2 * classificationResult - 1;
				}
			}
		}

		String[] annotationsLabels = mentions.stream().map(m -> m.toString() + m.getTokens().first()).toArray(size -> new String[size]);
		HashMap<String, Integer> mapping = new HashMap<>();

		FastVector fvWekaAttributes = new FastVector(1);
		Attribute attr = new Attribute("mention", (FastVector)null);
		fvWekaAttributes.addElement(attr);

		Instances instancesSingleInstances = new Instances("NamedCoref", fvWekaAttributes, mentions.size());
		List<Instance> instancesSingle = mentions.stream().map(m -> mentionToInstance(m, attr)).collect(Collectors.toList());
		for(int i = 0; i < mentions.size(); i++) instancesSingleInstances.add(instancesSingle.get(i));
		for(int i = 0; i < mentions.size(); i++) mapping.put(instancesSingleInstances.instance(i).stringValue(0), i);
		DistanceFunction namedDistance = new NamedDistanceFunction(mapping, distances);
		DistanceFunction scoreDistance = new NamedDistanceFunction(mapping, correlations);

		int bestClusterNum = 0;
		HashMap<Integer, List<Instance>> bestClusters = new HashMap<>();
		double bestClusterScore = 0.0;

		for(int n = 1; n <= mentions.size(); n++){
//			Clusterer clusterer = createClusterer(instancesSingleInstances, namedDistance, n);
			HashMap<Integer, List<Instance>> clusters = makeClustering(instancesSingleInstances, namedDistance,n);
			double score = scoreClustering(clusters, scoreDistance);
			System.out.println("Result score for " + n + " clusters is: " + score);
			if(score > bestClusterScore){
				bestClusterScore = score;
				bestClusterNum = n;
				bestClusters = clusters;
			}
		}
		printClustering(bestClusters);

		List<List<Annotation>> annotationClusters = bestClusters.entrySet().stream()
				.map(entry -> entry.getValue())
				.map(list -> list.stream()
						.map(instance -> mentions.get(mapping.get(instance.stringValue(0))))
						.collect(Collectors.toList()))
				.collect(Collectors.toList());



		RelationSet relationSetBestCluser = createRelationSet(annotationClusters);
		document.setRelations(relationSetBestCluser);


		return document;
	}

	private RelationSet  createRelationSet(List<List<Annotation>> corefClusterList){
		RelationSet relationSet = new RelationSet();

		for(List<Annotation> corefCluster : corefClusterList){
			if(corefCluster.size() > 1){
				for(int i = 1; i < corefCluster.size(); i++){
					Annotation from = corefCluster.get(i);
					Annotation to = corefCluster.get(i-1);
					relationSet.addRelation(new Relation(from, to, Relation.COREFERENCE));
				}
			}
		}

		return relationSet;
	}

	private void printClustering(HashMap<Integer, List<Instance>> clusters){
		for(Map.Entry<Integer, List<Instance>> cluster : clusters.entrySet()){
			System.out.println(
					String.join(
							", ",
							cluster.getValue().stream().map(instance -> instance.stringValue(0)).collect(Collectors.toList())
					)
			);
		}
	}

	private HashMap<Integer, List<Instance>> makeClustering(Instances instancesSingleInstances, DistanceFunction namedDistance, int numberOfClusters){
		HierarchicalClusterer clusterer = new HierarchicalClusterer();
		clusterer.setDistanceFunction(namedDistance);
		clusterer.setNumClusters(numberOfClusters);
		try {
			clusterer.buildClusterer(instancesSingleInstances);
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
		 return createClustering(clusterer, instancesSingleInstances);
	}

	private HashMap<Integer, List<Instance>> createClustering(Clusterer clusterer, Instances instances){
		HashMap<Integer, List<Instance>> clusters = new HashMap<>();
		for(int i = 0; i <instances.numInstances(); i++){
			try {
				Integer clusterId = clusterer.clusterInstance(instances.instance(i));
				List<Instance> clusterInstances = clusters.getOrDefault(clusterId, new ArrayList<>());
				clusterInstances.add(instances.instance(i));
				clusters.put(clusterId, clusterInstances);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return clusters;
	}

	private double scoreClustering(HashMap<Integer, List<Instance>> clusters, DistanceFunction distanceFunction){
		int ccount = 0;
		double clusteringScore = 0;
		for(Map.Entry<Integer, List<Instance>> cluster : clusters.entrySet()){
			double clusterScore = 0;
			for(Instance instance1 : cluster.getValue()){
				for(Instance instance2 : cluster.getValue()) {
					if(!instance1.equals(instance2)) clusterScore += distanceFunction.distance(instance1, instance2);
				}
			}
			clusteringScore += clusterScore;
			ccount++;
//			if(verbose) {
//				System.out.println(
//						String.join(
//								", ",
//								cluster.getValue().stream().map(instance -> instance.stringValue(0)).collect(Collectors.toList())
//						)
//				);
//			}
		}
//		if(verbose) {
//			System.out.println(ccount);
//			System.out.println(clusteringScore);
//		}
		return clusteringScore;
	}

	private Instance mentionToInstance(Annotation mention, Attribute attr){
		Instance instance = new Instance(1);
		instance.setValue(attr, mention.toString() + mention.getTokens().first());
		return instance;
	}


	@Override
	protected Document resolveMention(Document document, Annotation mention, List<MentionPairClassificationInstance<Double>> instancesForMention) {
		System.err.println("Call to undefined function resolveMention in WekaRandomForestMentionPariClusterClassifyResolver");
		return null;
	}

	@Override public Class<Classifier> getModelClass() {return Classifier.class;}
	@Override public Class<MentionPairClassificationInstance<Double>> getAbstractInstanceClass() {
		MentionPairClassificationInstance<Integer> inst = new MentionPairClassificationInstance<>();
		return (Class<MentionPairClassificationInstance<Double>>) inst.getClass();
	}
	@Override public Class<Instance> getClassifierInstanceClass() {return Instance.class;}
	@Override public Class<Double> getLabelClass() {return Double.class;}

}
