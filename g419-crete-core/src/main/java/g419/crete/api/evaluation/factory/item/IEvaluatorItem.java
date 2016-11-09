package g419.crete.api.evaluation.factory.item;

import g419.crete.api.annotation.mapper.AnnotationMapper;
import g419.crete.api.evaluation.IEvaluator;

public interface IEvaluatorItem {
    IEvaluator getEvaluator(AnnotationMapper mapper);
}
