package g419.crete.api.classifier;

import java.util.List;

import edu.berkeley.compbio.jlibsvm.binary.BinaryModel;
import edu.berkeley.compbio.jlibsvm.util.SparseVector;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.crete.api.instance.ClusterRankingInstance;
import g419.crete.api.instance.converter.AbstractCreteInstanceConverter;
import g419.crete.api.instance.generator.AbstractCreteInstanceGenerator;

public class SvmClusterRankingResolver extends AbstractCreteResolver<BinaryModel<Integer, SparseVector>, ClusterRankingInstance, SparseVector, Integer>{

	@Override
	public void setUp(
			AbstractCreteClassifier<BinaryModel<Integer, SparseVector>, SparseVector, Integer> clas,
			AbstractCreteInstanceGenerator<ClusterRankingInstance, Integer> gen,
			AbstractCreteInstanceConverter<ClusterRankingInstance, SparseVector> conv) {
		super.setUp(clas, gen, conv);
	}

	@Override
	protected List<ClusterRankingInstance> extractClassifiedAsCorrect(List<ClusterRankingInstance> instances, List<Integer> labels) {
		
		return null;
	}

	@Override
	protected Document resolveMention(Document document, Annotation mention) {
		
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override public Class<BinaryModel<Integer, SparseVector>> getModelClass() {return (Class<BinaryModel<Integer, SparseVector>>) (new BinaryModel<Integer, SparseVector>()).getClass();}
	@Override public Class<ClusterRankingInstance> getAbstractInstanceClass() {return ClusterRankingInstance.class;}
	@Override public Class<SparseVector> getClassifierInstanceClass() {return SparseVector.class;}
	@Override public Class<Integer> getLabelClass() {return Integer.class;}
}