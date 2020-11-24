package g419.serel.ruleTree;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NodeMatch {

    private String text;
    private String namedEntity;
    private String role;
    private String xPos;

    private boolean isMatchAny;
    private boolean isMatchLemma;

    public List<EdgeMatch> edgeMatchList = new ArrayList<>();

    public NodeMatch() {
    }

    public void dumpString() {
        System.out.println("NodeMatch: text = " + text + " xPos=" + xPos + " namedEntity=" + namedEntity + " role=" + role);
        for (final EdgeMatch em : edgeMatchList) {
            em.dumpString();
        }

    }

}
