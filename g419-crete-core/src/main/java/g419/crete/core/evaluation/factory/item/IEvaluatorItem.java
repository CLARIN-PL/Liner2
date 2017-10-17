package g419.crete.core.evaluation.factory.item;

import g419.crete.core.annotation.mapper.AnnotationMapper;
import g419.crete.core.evaluation.IEvaluator;

public interface IEvaluatorItem {
    IEvaluator getEvaluator(AnnotationMapper mapper);
}
