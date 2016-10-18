package g419.crete.api.instance.converter;

import edu.berkeley.compbio.jlibsvm.util.SparseVector;
import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.instance.ClusterRankingInstance;

public class ClusterRankingInstanceConverter extends AbstractCreteInstanceConverter<ClusterRankingInstance, SparseVector>{

	public ClusterRankingInstanceConverter(AbstractCreteClassifier<?, SparseVector, ?> cls) {
		super(cls);
	}

	@Override
	public SparseVector convertSingleInstance(ClusterRankingInstance instance) {
		return new SparseVector(5, 0.01f, 1.0f);
	}

	@Override
	public Class<SparseVector> getRepresentationClass() {
		return SparseVector.class;
	}

	
}
