package g419.toolbox.sumo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Klasa reprezentuje graph. Elementami grafu są napisy.
 */
public class Graph {

  private Set<String> nodes = null;
  private Map<String, Set<String>> childrens = null;

  public Graph() {
    this.childrens = new HashMap<String, Set<String>>();
    this.nodes = new HashSet<String>();
  }

  public boolean containsClass(String label) {
    return this.nodes.contains(label);
  }

  /**
   * Sprawdza, czy child jest podklasą parent, tj. czy istnieje ścieżka
   * od child do parent w grafie.
   *
   * @param child
   * @param parent
   * @return
   */
  public boolean isSubclassOf(String child, String parent) {
    Set<String> directSubclasses = this.childrens.get(parent);
    if (directSubclasses == null) {
      // Nie ma podklas, więc zwracam false
      return false;
    } else if (directSubclasses.contains(child)) {
      return true;
    }
    for (String directSubclass : directSubclasses) {
      if (this.isSubclassOf(child, directSubclass)) {
        return true;
      }
    }
    return false;
  }

  public void addConnection(String child, String parent) {
    this.nodes.add(child);
    this.nodes.add(parent);
    Set<String> directSubclasses = this.childrens.get(parent);
    if (directSubclasses == null) {
      directSubclasses = new HashSet<String>();
      this.childrens.put(parent, directSubclasses);
    }
    directSubclasses.add(child);
  }

  public Set<String> getSuperclasses(String currentClass) {
    Set<String> classes = new HashSet<String>();
    for (String superclass : this.childrens.keySet()) {
      if (this.childrens.get(superclass).contains(currentClass)) {
        classes.add(superclass);
        classes.addAll(this.getSuperclasses(superclass));
      }
    }
    return classes;
  }

  public Set<String> getSubclasses(String currentClass) {
    Set<String> classes = new HashSet<String>();
    this.getSubclasses(currentClass, classes);
    return classes;
  }

  public void getSubclasses(String currentClass, Set<String> classes) {
    Set<String> directSubclasses = this.childrens.get(currentClass);
    if (directSubclasses != null) {
      for (String cl : directSubclasses) {
        classes.add(cl);
        this.getSubclasses(cl, classes);
      }
    }
  }

}
