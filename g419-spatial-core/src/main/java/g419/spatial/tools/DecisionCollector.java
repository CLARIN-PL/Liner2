package g419.spatial.tools;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.liner2.core.tools.FscoreEvaluator;
import g419.spatial.structure.SpatialExpression;

public class DecisionCollector<T> {

    private final KeyGenerator<T> keyGenerator;
    private final Map<String, T> gold = Maps.newHashMap();
    private final Map<String, T> decision = Maps.newHashMap();

	public DecisionCollector(final KeyGenerator<T> keyGenerator){
        this.keyGenerator = keyGenerator;
    }

	public void addGold(final T item){
		gold.put(keyGenerator.generateKey(item), item);
	}

	public void addAllGold(final Collection<T> items){
        items.stream().forEach(this::addGold);
	}

	public Collection<T> getGold(){
	    return gold.values();
    }

	public void addDecision(final T item){
		decision.put(keyGenerator.generateKey(item), item);
	}

    public void addAllDecision(final Collection<T> items){
        items.stream().forEach(this::addDecision);
    }

    public Collection<T> getDecision(){
        return decision.values();
    }

    public boolean containsAsGold(final T item){
		return this.gold.containsKey(keyGenerator.generateKey(item));
	}

	public boolean containsAsDecision(final T item){
		return this.decision.containsKey(keyGenerator.generateKey(item));
	}
	
	public FscoreEvaluator getConfusionMatrix(){
		FscoreEvaluator eval = new FscoreEvaluator();
		eval.addTruePositive(getTruePositives().size());
		eval.addFalsePositive(getFalsPositives().size());
		eval.addFalseNegative(getFalseNegative().size());
		return eval;
	}

	public List<T> getTruePositives(){
        return decision.entrySet().stream()
                .filter(p->gold.containsKey(p.getKey())).map(p->p.getValue()).collect(Collectors.toList());
    }

    public List<T> getFalsPositives(){
	    return decision.entrySet().stream()
                .filter(p->!gold.containsKey(p.getKey())).map(p->p.getValue()).collect(Collectors.toList());
    }

    public List<T> getFalseNegative(){
	    return gold.entrySet().stream()
                .filter(p->!decision.containsKey(p.getKey())).map(p->p.getValue()).collect(Collectors.toList());
    }
}
