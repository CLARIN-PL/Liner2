package g419.crete.api.classifier;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.berkeley.compbio.jlibsvm.ImmutableSvmParameter;
import edu.berkeley.compbio.jlibsvm.ImmutableSvmParameterGrid;
import edu.berkeley.compbio.jlibsvm.SVM;
import edu.berkeley.compbio.jlibsvm.binary.BinaryClassificationProblem;
import edu.berkeley.compbio.jlibsvm.binary.BinaryModel;
import edu.berkeley.compbio.jlibsvm.binary.C_SVC;
import edu.berkeley.compbio.jlibsvm.binary.MutableBinaryClassificationProblemImpl;
import edu.berkeley.compbio.jlibsvm.kernel.KernelFunction;
import edu.berkeley.compbio.jlibsvm.kernel.LinearKernel;
import edu.berkeley.compbio.jlibsvm.util.SparseVector;
import g419.crete.api.classifier.model.SvmRankingModel;

public class RankingSvmClassifier extends AbstractCreteClassifier<BinaryModel<Integer, SparseVector>, SparseVector, Integer> {

	private BinaryModel<Integer, SparseVector> rankingModel;
	private SVM<Integer, SparseVector, BinaryClassificationProblem<Integer, SparseVector>> svm;
	private MutableBinaryClassificationProblemImpl<Integer, SparseVector> problem;
	ImmutableSvmParameter<Integer, SparseVector> param;
	
	public RankingSvmClassifier(){
		this.svm = new C_SVC<Integer, SparseVector>();
		
		ImmutableSvmParameterGrid.Builder<Integer, SparseVector> builder = ImmutableSvmParameterGrid.builder();
		builder.nu = 0.5f;
		builder.cache_size = 100;
		builder.eps = 1e-3f;
		builder.p = 0.1f;
		builder.shrinking = true;
		builder.probability = false;
		builder.redistributeUnbalancedC = true;
		builder.kernelSet = new HashSet<KernelFunction<SparseVector>>();
		builder.kernelSet.add(new LinearKernel());
		this.param = builder.build();
	}
	
	@Override
	public List<Integer> classify(List<SparseVector> instances) {
		ArrayList<Integer> labels = new ArrayList<Integer>();
		int argMax = -1;
		float maxLabel = -1.0f;
		
		// Classify (rank) each instance
		for(int i = 0; i < instances.size(); i++){
			SparseVector instance = instances.get(i);
			float instanceLabel = rankingModel.predictValue((SparseVector) instance) * rankingModel.getTrueLabel();
			
			// Mark as not-best
			labels.add(-1);
			
			//TODO: rozstrzygnij remisy
			// Check for ranking argmax
			if(instanceLabel > maxLabel){
				maxLabel = instanceLabel;
				argMax = i;
			}
			
		}
		
		// Set argMax (best ranked instance) as the only chosen (one-hot)
		labels.set(argMax, 1); 
		
		return labels;
	}

	@Override
	public void train() {
		int instanceCount = trainingInstances.size();
		this.problem = new MutableBinaryClassificationProblemImpl<Integer, SparseVector>(Integer.class, instanceCount);
		
		for(int i = 0; i < instanceCount; i++)
			this.problem.addExample(trainingInstances.get(i), trainingInstanceLabels.get(i));
		
		rankingModel = (BinaryModel<Integer, SparseVector>) this.svm.train(this.problem, this.param);
		this.model = new SvmRankingModel(rankingModel);
	}

	@Override
	public Class<SparseVector> getInstanceClass() {
		return SparseVector.class;
	}

	@Override
	public Class<Integer> getLabelClass() {
		return Integer.class;
	}
	
}
