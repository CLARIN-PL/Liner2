package g419.crete.core.resolver;

import java.util.List;

import edu.berkeley.compbio.jlibsvm.binary.BinaryModel;
import edu.berkeley.compbio.jlibsvm.util.SparseVector;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.crete.core.classifier.AbstractCreteClassifier;
import g419.crete.core.instance.ClusterRankingInstance;
import g419.crete.core.instance.converter.AbstractCreteInstanceConverter;
import g419.crete.core.instance.generator.AbstractCreteInstanceGenerator;

public class SvmClusterRankingResolver extends AbstractCreteResolver<BinaryModel<Integer, SparseVector>, ClusterRankingInstance, SparseVector, Integer>{

	@Override
	public void setUp(
			AbstractCreteClassifier<BinaryModel<Integer, SparseVector>, SparseVector, Integer> clas,
			AbstractCreteInstanceGenerator<ClusterRankingInstance, Integer> gen,
			AbstractCreteInstanceConverter<ClusterRankingInstance, SparseVector> conv) {
		super.setUp(clas, gen, conv);
	}

	
	
	@SuppressWarnings("unchecked")
	@Override public Class<BinaryModel<Integer, SparseVector>> getModelClass() {return (Class<BinaryModel<Integer, SparseVector>>) (new BinaryModel<Integer, SparseVector>()).getClass();}
	@Override public Class<ClusterRankingInstance> getAbstractInstanceClass() {return ClusterRankingInstance.class;}
	@Override public Class<SparseVector> getClassifierInstanceClass() {return SparseVector.class;}
	@Override public Class<Integer> getLabelClass() {return Integer.class;}

	@Override
	protected Document resolveMention(Document document, Annotation mention,
			List<ClusterRankingInstance> instancesForMention) {
		// TODO Auto-generated method stub
		return null;
	}
}