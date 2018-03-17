package g419.corpus.io.writer.tei;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class TeiPointerManager {

    final Map<Object, String> pointers = Maps.newHashMap();
    final Logger logger = LoggerFactory.getLogger(getClass());

    public String getPointer(final Object object){
        final String pointer = pointers.get(object);
        if ( pointer == null ){
            logger.warn("Object {} not found in the PointerManager", object);
        }
        return pointer;
    }

    public boolean hasPointer(final Object object){
        return pointers.get(object) != null;
    }

    public void addPointer(final Object object, final String pointer){
        if ( pointers.containsKey(object) && !pointers.get(object).equals(pointer)){
            throw new RuntimeException(String.format(
                    "Object already registered in the PointerManager with different pointer: %s != %s", pointer, pointers.get(object).toString()));
        }
        pointers.put(object,pointer);
    }

    public Set<Object> getIndexedObjects(){
        return pointers.keySet();
    }
}