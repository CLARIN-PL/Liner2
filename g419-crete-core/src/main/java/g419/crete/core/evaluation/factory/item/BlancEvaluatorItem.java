package g419.crete.core.evaluation.factory.item;

import g419.crete.core.annotation.mapper.AnnotationMapper;
import g419.crete.core.evaluation.BlancEvaluator;
import g419.crete.core.evaluation.IEvaluator;
import g419.crete.core.evaluation.factory.EvaluatorFactory;

public class BlancEvaluatorItem implements IEvaluatorItem {

    @Override
    public IEvaluator getEvaluator(AnnotationMapper mapper) {
        return new BlancEvaluator(mapper);
    }

    static{
        EvaluatorFactory.getFactory().register("blanc", new BlancEvaluatorItem());
    }
}

