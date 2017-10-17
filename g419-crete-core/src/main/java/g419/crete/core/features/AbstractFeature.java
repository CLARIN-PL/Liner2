package g419.crete.core.features;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFeature<InputType, ReturnType>{
	protected ReturnType value;
	public ReturnType getValue(){return this.value;}
	
	// ---------------------------- DEFAULT IMPLEMENTATIONS --> ADVISED TO CHANGE IN CONCRETE CLASS -------------------------------
	public int getSize(){return 0;};
	public List<ReturnType> getAllValues(){return new ArrayList<ReturnType>();}

	// ---------------------------- ABSTRACT METHODS ------------------------------------------------------------------------------------------------------
	public abstract void generateFeature(InputType input);
	public abstract String getName();
	
	public abstract Class<InputType> getInputTypeClass();
	public abstract Class<ReturnType> getReturnTypeClass();
}