package g419.lib.cli;

import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa pomocnicza do wyszukiwania klas będących podklasą klasy Action.
 * 
 * @author Michał Marcińczuk
 *
 */
public class ActionFinder {

	/**
	 * Zwraca listę instancji klas dziedziczących po klasie Action znajdujących się we wskazanym pakiecie.
	 * @param packageName Nazwa pakietu, w którym będa wyszukiwane klasy.
	 * @return lista instancji obiektów będących rozszerzeniem klasy Action.
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IOException
	 */
	public static List<Action> find(String packageName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException{
    	ClassLoader loader = Thread.currentThread().getContextClassLoader();    	
    	List<Action> actions = new ArrayList<Action>();
    	for ( final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses(packageName) ){    		
    		Class<?> cl = loader.loadClass(info.getName());
    		if ( cl.getSuperclass() == Action.class ){    		    			
    			actions.add((Action) cl.getConstructor().newInstance(new Object[]{}));
    		}
    	}
		return actions;
	}

}