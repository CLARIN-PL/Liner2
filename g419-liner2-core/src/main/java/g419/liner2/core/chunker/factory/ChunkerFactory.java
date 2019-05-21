package g419.liner2.core.chunker.factory;


import com.google.common.reflect.ClassPath;
import g419.corpus.ConsolePrinter;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.ensemble.CascadeChunker;
import g419.liner2.core.chunker.ensemble.MajorityVotingChunker;
import g419.liner2.core.chunker.ensemble.UnionChunker;
import g419.liner2.core.normalizer.factory.AbstractNormalizerFactoryItem;
import org.ini4j.Ini;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


public class ChunkerFactory {

  private static final String CHUNKER_TYPE = "type";
  private static ChunkerFactory factory = null;

  private ArrayList<ChunkerFactoryItem> items = new ArrayList<ChunkerFactoryItem>();

  private ChunkerFactory() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
    this.items.addAll(this.findFactoryItems("g419.liner2.core.chunker.factory"));
    this.items.addAll(this.findNormalisationFactoryItems("g419.liner2.core.normalizer.factory"));
  }

  /**
   * Zwraca listę obiektów dziedziczących po klasie ChunkerFactoryItem znajdujących się we wskazanym pakiecie.
   *
   * @param packageName Nazwa pakietu, w którym będa wyszukiwane klasy.
   * @return lista obiektów będących rozszerzeniem klasy ChunkerFactoryItem.
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   * @throws SecurityException
   * @throws IOException
   */
  public List<ChunkerFactoryItem> findFactoryItems(String packageName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    List<ChunkerFactoryItem> items = new ArrayList<ChunkerFactoryItem>();
    for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses(packageName)) {
      Class<?> cl = loader.loadClass(info.getName());
      if (cl.getSuperclass() == ChunkerFactoryItem.class) {
        items.add((ChunkerFactoryItem) cl.getConstructor().newInstance(new Object[] {}));
      }
    }
    return items;
  }

  public List<ChunkerFactoryItem> findNormalisationFactoryItems(String packageName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    List<ChunkerFactoryItem> items = new ArrayList<ChunkerFactoryItem>();
    for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses(packageName)) {
      Class<?> cl = loader.loadClass(info.getName());
      if (cl.getSuperclass() == AbstractNormalizerFactoryItem.class) {
        items.add((ChunkerFactoryItem) cl.getConstructor().newInstance(new Object[] {}));
      }
    }
    return items;
  }

  /**
   * Get current ChunkerFactory. If the factory does not exist then create it.
   *
   * @return
   * @throws IOException
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws ClassNotFoundException
   */
  private static ChunkerFactory get() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
    if (ChunkerFactory.factory == null) {
      ChunkerFactory.factory = new ChunkerFactory();
    }
    return ChunkerFactory.factory;
  }

  /**
   * Get human-readable description of chunker commands.
   *
   * @return
   * @throws IOException
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws ClassNotFoundException
   */
  public static String getDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
    StringBuilder sb = new StringBuilder();
    for (ChunkerFactoryItem item : ChunkerFactory.get().items) {
      sb.append("  " + item.getType() + "\n");
    }
    return sb.toString();
  }

  /**
   * Creates a chunker according to the description
   *
   * @param description
   * @return
   * @throws Exception
   */
  public static Chunker createChunker(Ini.Section description, ChunkerManager cm) throws Exception {
    ConsolePrinter.log("-> Setting up chunker: " + description.getName());
    for (ChunkerFactoryItem item : ChunkerFactory.get().items) {
      if (item.getType().equals(description.get(CHUNKER_TYPE))) {
        Chunker chunker = item.getChunker(description, cm);
        chunker.setDescription(description);
        return chunker;
      }
    }
    throw new Error(String.format("Chunker description '%s' not recognized", description.get(CHUNKER_TYPE)));
  }

  /**
   * Create chunker pipe according to given description. The chunker names
   * must be provided in the list of chunker description passed to the
   * constructor.
   * <p>
   * Example: c1 --- getGlobal single chunker named `c1`
   *
   * @param description
   * @return
   */
  public static Chunker getChunkerPipe(String description, ChunkerManager cm) {
    return getChunkerUnionPipe(description.split("\\+"), cm);
  }

  private static Chunker getChunkerUnionPipe(String[] chunkerNames, ChunkerManager cm) {
    if (chunkerNames.length == 1) {
      return getChunkerVotingPipe(chunkerNames[0].split("\\*"), cm);
    } else {
      ArrayList<Chunker> chunkers = new ArrayList<Chunker>();
      for (String name : chunkerNames) {
        chunkers.add(getChunkerVotingPipe(name.split("\\*"), cm));
      }
      return new UnionChunker(chunkers);
    }
  }

  private static Chunker getChunkerVotingPipe(String[] chunkerNames, ChunkerManager cm) {
    if (chunkerNames.length == 1) {
      return getChunkerCascadePipe(chunkerNames[0].split(">"), cm);
    } else {
      ArrayList<Chunker> chunkers = new ArrayList<Chunker>();
      for (String name : chunkerNames) {
        chunkers.add(getChunkerCascadePipe(name.split(">"), cm));
      }
      return new MajorityVotingChunker(chunkers);
    }
  }

  private static Chunker getChunkerCascadePipe(String[] chunkerNames, ChunkerManager cm) {
    if (chunkerNames.length == 1) {
      return cm.getChunkerByName(chunkerNames[0]);
    } else {
      ArrayList<Chunker> chunkers = new ArrayList<Chunker>();
      for (String name : chunkerNames) {
        chunkers.add(cm.getChunkerByName(name));
      }
      return new CascadeChunker(chunkers);
    }
  }
}
