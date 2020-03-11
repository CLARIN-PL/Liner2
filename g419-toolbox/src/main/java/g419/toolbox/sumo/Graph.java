package g419.toolbox.sumo;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {

  private final Set<String> nodes;
  private final Map<String, Set<String>> childrens;

  public Graph() {
    childrens = Maps.newHashMap();
    nodes = Sets.newHashSet();
  }

  public Set<String> getNodes() {
    return nodes;
  }

  public boolean containsClass(final String label) {
    return nodes.contains(label);
  }

  public boolean isSubclassOf(final String child, final String parent) {
    final Set<String> directSubclasses = childrens.get(parent);
    if (directSubclasses == null) {
      return false;
    } else if (directSubclasses.contains(child)) {
      return true;
    }
    for (final String directSubclass : directSubclasses) {
      if (isSubclassOf(child, directSubclass)) {
        return true;
      }
    }
    return false;
  }

  public void addConnection(final String child, final String parent) {
    nodes.add(child);
    nodes.add(parent);
    Set<String> directSubclasses = childrens.get(parent);
    if (directSubclasses == null) {
      directSubclasses = new HashSet<>();
      childrens.put(parent, directSubclasses);
    }
    directSubclasses.add(child);
  }

  public Set<String> getSuperclasses(final String currentClass) {
    final Set<String> classes = new HashSet<>();
    for (final String superclass : childrens.keySet()) {
      if (childrens.get(superclass).contains(currentClass)) {
        classes.add(superclass);
        classes.addAll(getSuperclasses(superclass));
      }
    }
    return classes;
  }

  public Set<String> getSubclasses(final String currentClass) {
    final Set<String> classes = new HashSet<>();
    getSubclasses(currentClass, classes);
    return classes;
  }

  public void getSubclasses(final String currentClass, final Set<String> classes) {
    final Set<String> directSubclasses = childrens.get(currentClass);
    if (directSubclasses != null) {
      for (final String cl : directSubclasses) {
        classes.add(cl);
        getSubclasses(cl, classes);
      }
    }
  }

}
