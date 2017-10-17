package g419.crete.core.trainer;

import edu.berkeley.compbio.jlibsvm.binary.BinaryModel;
import edu.berkeley.compbio.jlibsvm.util.SparseVector;
import g419.crete.core.instance.ClusterRankingInstance;

public class SvmClusterRankingTrainer extends AbstractCreteTrainer<BinaryModel<Integer, SparseVector>, ClusterRankingInstance, SparseVector, Integer>{

	@SuppressWarnings("unchecked")
	@Override public Class<BinaryModel<Integer, SparseVector>> getModelClass() {return (Class<BinaryModel<Integer, SparseVector>>) (new BinaryModel<Integer, SparseVector>()).getClass();}
	@Override public Class<ClusterRankingInstance> getAbstractInstanceClass() {return ClusterRankingInstance.class;}
	@Override public Class<SparseVector> getClassifierInstanceClass() {return SparseVector.class;}
	@Override public Class<Integer> getLabelClass() {return Integer.class;}
}
