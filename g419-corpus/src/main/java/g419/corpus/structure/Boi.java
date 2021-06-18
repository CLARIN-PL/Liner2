package g419.corpus.structure;

public class Boi {

  public int startId;
  public int endId;
  public String label;

  Boi(int _startId, String _label) {
    this.startId = _startId;
    this.label = _label;
  }


  public boolean isOkForTacred() {
    if (label.endsWith("_full"))
      return false;

    if (label.endsWith("_first"))
      return false;

    if (label.endsWith("_last"))
      return false;

    if (label.endsWith("_second"))
      return false;

    if (label.endsWith("_add"))
      return false;

    return true;
  }


}
