package g419.crete.api.instance;

import g419.crete.api.structure.IHaveFeatures;

import java.util.List;



/**
 * Klasa opisująca instancję problemu klasyfikacji koreferencji
 * Abstrakcyjna klasa musi uwzględniać możliwość klasyfikacji w trybach:
 * - Para anotacji
 * - Para anotacja - klaster
 * - 
 * @author Adam Kaczmarek<adamjankaczmarek@gmail.com>
 *
 */
public abstract class AbstractCreteInstance<LabelType> {

//	protected LabelType POSITIVE_LABEL;
//	protected LabelType NEGATIVE_LABEL;
//	
	protected LabelType label;
	protected List<String> featureNames;
	
	public AbstractCreteInstance(){
		this.label = null;
	}
	
	public AbstractCreteInstance(LabelType l){
		this.label = l;
	}
	
	public AbstractCreteInstance(LabelType l, List<String> features){
		this.label = l;
		this.featureNames = features;
	}
	
	
	public LabelType getLabel(){
		return label;
	}
	
//	public boolean hasPositiveLabel(){
//		return POSITIVE_LABEL.equals(label);
//	}
//	
//	public LabelType getPositiveLabel(){return POSITIVE_LABEL;}
//	public LabelType getNegativeLabel(){return NEGATIVE_LABEL;}
//	
	// Populate features
	protected abstract void extractFeatures();
	
	//
	public abstract List<IHaveFeatures<?>> getComponents();
}  
