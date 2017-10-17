//package g419.crete.api.instance.generator;
//
//import g419.corpus.structure.Annotation;
//import g419.corpus.structure.AnnotationClusterSet;
//import g419.corpus.structure.AnnotationPositionComparator;
//import g419.corpus.structure.Document;
//import g419.crete.core.annotation.AbstractAnnotationSelector;
//import g419.crete.core.annotation.AnnotationSelectorFactory;
//import g419.crete.core.instance.LogisticMentionPairClassificationInstance;
//import g419.crete.core.instance.MentionPairClassificationInstance;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * Created by akaczmarek on 30.11.15.
// */
//public class LogisticMentionPairInstanceGenerator extends AbstractCreteInstanceGenerator<LogisticMentionPairClassificationInstance, Float> {
//
//    public static final Float POSITIVE_LABEL = 1.0f;
//    public static final Float NEGATIVE_LABEL = -1.0f;
//
//    private final boolean training;
//    private final AbstractAnnotationSelector namedEntitySelector = AnnotationSelectorFactory.getFactory().getInitializedSelector("named_entity_selector");
//
//    public LogisticMentionPairInstanceGenerator(boolean train){
//        this.training = false;
//    }
//
//    @Override
//    public List<LogisticMentionPairClassificationInstance> generateInstances(Document document, AbstractAnnotationSelector mentionSelector, AbstractAnnotationSelector singletonSelector) {
//        List<LogisticMentionPairClassificationInstance> instances = new ArrayList<>();
//        for(Annotation mention : mentionSelector.selectAnnotations(document))
//            instances.addAll(generateInstancesForMention(document, mention, null, singletonSelector.selectAnnotations(document)));
//
//        return instances;
//    }
//
//    @Override
//    public List<LogisticMentionPairClassificationInstance> generateInstancesForMention(Document document, Annotation mention, List<Annotation> allMentionsClassified, List<Annotation> singletons) {
//        AnnotationPositionComparator comparator = new AnnotationPositionComparator();
//        AnnotationClusterSet clusters = AnnotationClusterSet.fromRelationSet(document.getRelations());
//
//        List<LogisticMentionPairClassificationInstance> negativeInstances  = namedEntitySelector.selectAnnotations(document)
////				document.getAnnotations()
//                .parallelStream()
////				.filter(annotation -> comparator.compare(annotation, mention) < 0)
//                .filter(annotation -> !annotation.equals(mention))
//                .filter(annotation -> !clusters.inSameCluster(mention, annotation))
//                .sorted(comparator)
//                        // Apply limit only to training data
//                .limit(training ? 2: 1000)
//                .map(antecedent ->
//                                new LogisticMentionPairClassificationInstance(
//                                        mention,
//                                        antecedent,
//                                        NEGATIVE_LABEL,
//                                        this.featureNames
//                                )
//                )
//                .collect(Collectors.toList());
//
//        List<LogisticMentionPairClassificationInstance> positiveInstances = namedEntitySelector.selectAnnotations(document)
////				document.getAnnotations()
//                .parallelStream()
////				.filter(annotation -> training || comparator.compare(annotation, mention) < 0)
//                .filter(annotation -> !annotation.equals(mention))
//                .filter(annotation -> clusters.inSameCluster(mention, annotation))
//                .sorted(comparator)
//                        // Apply limit only to training data
//                .limit(training ? 10: 1000)
//                .map(antecedent ->
//                                new LogisticMentionPairClassificationInstance(
//                                        mention,
//                                        antecedent,
//                                        POSITIVE_LABEL,
//                                        this.featureNames
//                                )
//                )
//                .collect(Collectors.toList());
//
//        List<LogisticMentionPairClassificationInstance> instances = new ArrayList<>();
//        instances.addAll(positiveInstances);
//        instances.addAll(negativeInstances);
//
//        return instances;
//    }
//}
