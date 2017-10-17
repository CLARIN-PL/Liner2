package g419.crete.core.evaluation.factory;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.Reflection;
import g419.crete.core.annotation.mapper.AnnotationMapper;
import g419.crete.core.evaluation.IEvaluator;
import g419.crete.core.evaluation.factory.item.IEvaluatorItem;
import g419.lib.cli.Action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by akaczmarek on 13.12.15.
 */
public class EvaluatorFactory {
    static{
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        List<Action> actions = new ArrayList<Action>();
        try {
            for ( final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses("g419.crete.core.evaluation.factory.item") ) {
                Class<?> cl = loader.loadClass(info.getName());
                Reflection.initialize(cl);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, IEvaluatorItem> evaluators;
    private EvaluatorFactory(){
        evaluators = new HashMap<>();
    }

    private static class FactoryHolder {
        private static final EvaluatorFactory FACTORY = new EvaluatorFactory();
    }
    public static EvaluatorFactory getFactory(){
        return FactoryHolder.FACTORY;
    }

    public IEvaluator getEvaluator(String name, AnnotationMapper mapper) throws NoSuchElementException{
        IEvaluatorItem evaluator = evaluators.get(name);
        if(evaluator == null) throw new NoSuchElementException();
        return evaluator.getEvaluator(mapper);
    }

    public void register(String name, IEvaluatorItem evaluatorItem){
        evaluators.put(name, evaluatorItem);
    }

}
