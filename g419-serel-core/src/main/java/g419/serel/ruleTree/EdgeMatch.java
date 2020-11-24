package g419.serel.ruleTree;


import lombok.Data;

@Data
public class EdgeMatch {

  private String side;
  private String depRel;


  public NodeMatch nodeMatch;

  public EdgeMatch() {
  }

  public void dumpString() {
    System.out.println("EdgeMatch side=" + side + " depRel=" + depRel);
    nodeMatch.dumpString();
  }
}
