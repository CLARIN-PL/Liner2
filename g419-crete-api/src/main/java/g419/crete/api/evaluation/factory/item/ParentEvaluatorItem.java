package g419.crete.api.evaluation.factory.item;

import g419.crete.api.CreteOptions;
import g419.crete.api.annotation.AnnotationSelectorFactory;
import g419.crete.api.annotation.mapper.AnnotationMapper;
import g419.crete.api.evaluation.IEvaluator;
import g419.crete.api.evaluation.ParentEvaluator;
import g419.crete.api.evaluation.factory.EvaluatorFactory;

/**
 * Created by akaczmarek on 13.12.15.
 */
public class ParentEvaluatorItem implements IEvaluatorItem {

    public static final String IDENTIFYING_SELECTOR = "identifying_selector";
    public static final String REFERENCING_SELECTOR = "referencing_selector";

    @Override
    public IEvaluator getEvaluator(AnnotationMapper mapper) {


        return new ParentEvaluator(
                AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(IDENTIFYING_SELECTOR)),
                AnnotationSelectorFactory.getFactory().getInitializedSelector(CreteOptions.getOptions().getProperties().getProperty(REFERENCING_SELECTOR)),
                mapper
            );
    }

    static{
        EvaluatorFactory.getFactory().register("parent", new ParentEvaluatorItem());
    }
}
