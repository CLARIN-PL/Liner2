package g419.crete.core.instance.representation;

import edu.berkeley.compbio.jlibsvm.util.SparseVector;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class SubtractableSparseVector extends SparseVector{

	private int capacity;
	
	public static SubtractableSparseVector getOneHotSparseVector(int index, float value, int capacity){
		SubtractableSparseVector sv =  new SubtractableSparseVector(1);
		sv.indexes[0] = index;
		sv.values[0] = value;
		sv.capacity = capacity;
		return sv;
	}
	
	public SubtractableSparseVector(int dimensions) {
		super(dimensions);
	}
	
	public void set(int index, float value){
		int j = Arrays.binarySearch(indexes, index);
        if (j >= 0) 
            values[j] = value;
	}
	
	private int[] shiftIndices(int[] indices, int shift){
		for(int i = 0; i < indices.length; i++) indices[i] += shift;
		return indices;
	}
	
	public void concat(SubtractableSparseVector otherVector){
		float[] otherValues = otherVector.values;
		int[] otherIndices = shiftIndices(otherVector.indexes, this.capacity);
		this.capacity += otherVector.capacity;

		this.values = ArrayUtils.addAll(values, otherValues);
		this.indexes = ArrayUtils.addAll(indexes, otherIndices);
	}
	
	/**
	 * Subtract given vector from current vector
	 * Operation is performed as follows - there can occur one of three cases for each value index:
	 * 1. Both vectors have set value on index i --&gt; resulting vector has value set to their difference
	 * 2. Current vector only has set value on index i --&gt; resulting vector has this value unchanged
	 * 3. Current vector only has set value on index i --&gt; resulting vector has this value with different sign
	 * @param otherVector
	 */
	public void minus(SubtractableSparseVector otherVector){
		ArrayList<Integer> resultIndices = new ArrayList<Integer>();
		ArrayList<Float> resultValues = new ArrayList<Float>();
		
		int indexThis = 0;
		int indexOther = 0;
		
		while(indexThis < this.indexes.length && indexOther < otherVector.indexes.length){
			int curVecIndex = this.indexes[indexThis];
			int othVecIndex = this.indexes[indexOther];
			
			
			if(curVecIndex == othVecIndex){
				// Case 1. Both vectors have value at given index
				// Store index of value
				resultIndices.add(curVecIndex);
				// Store value difference
				resultValues.add(this.get(curVecIndex) - otherVector.get(othVecIndex));
				// Go to next indices
				indexThis++;
				indexOther++;
			}
			else if (curVecIndex < othVecIndex){
				// Case 2. Value exists only in current vector
				// Store index of value
				resultIndices.add(curVecIndex);
				// Store value
				resultValues.add(this.get(curVecIndex));
				// Go to next index in current vector
				indexThis++;
			}
			else{ // curVecIndex > othVecIndex
				// Case 3. Value exists only in other vector
				// Store index of value
				resultIndices.add(othVecIndex);
				// Store value
				resultValues.add(-otherVector.get(othVecIndex));
				// Go to next index in other vector
				indexOther++;
			}
		}
		
		while(indexThis < this.indexes.length){
			// There exists values in current vector on higher indices
			// than highest index of other vector
			int curVecIndex = this.indexes[indexThis];
			// Store index of value
			resultIndices.add(curVecIndex);
			// Store value
			resultValues.add(this.get(curVecIndex));
			// Go to next index in current vector
			indexThis++;
		}
		
		while(indexOther < otherVector.indexes.length){
			// There exists values in other vector on higher indices
			// than highest index of current vector
			int othVecIndex = this.indexes[indexOther];
			// Store index of value
			resultIndices.add(othVecIndex);
			// Store value
			resultValues.add(this.get(othVecIndex));
			// Go to next index in current vector
			indexOther++;
		}
		
		indexes = new int[resultIndices.size()];
        values = new float[indexes.length];

        for (int i = 0; i < resultIndices.size(); i++) {
            indexes[i] = resultIndices.get(i);
            values[i] = resultValues.get(i);
        }
		
	}

}
