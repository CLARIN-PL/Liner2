package g419.crete.api.evaluation.factory.item;

import g419.crete.api.annotation.mapper.AnnotationMapper;
import g419.crete.api.evaluation.BlancEvaluator;
import g419.crete.api.evaluation.IEvaluator;
import g419.crete.api.evaluation.factory.EvaluatorFactory;

public class BlancEvaluatorItem implements IEvaluatorItem {

    @Override
    public IEvaluator getEvaluator(AnnotationMapper mapper) {
        return new BlancEvaluator(mapper);
    }

    static{
        EvaluatorFactory.getFactory().register("blanc", new BlancEvaluatorItem());
    }
}

