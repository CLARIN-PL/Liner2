package g419.crete.api.classifier.model;

import edu.berkeley.compbio.jlibsvm.binary.BinaryModel;
import edu.berkeley.compbio.jlibsvm.util.SparseVector;

public class SvmRankingModel extends Model<BinaryModel<Integer, SparseVector>>{

	public SvmRankingModel(BinaryModel<Integer, SparseVector> model) {
		super(model);
	}

	@Override
	public void persist(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BinaryModel<Integer, SparseVector> load(String path) {
		// TODO Auto-generated method stub
		return null;
	}

}
