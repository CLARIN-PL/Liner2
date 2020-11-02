package g419.serel.structure;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RuleMatchingRelations {

    String relationType;
    List<String> tokens;
    int targetIndex;
    String targetEntityName;

    int sourceIndex;
    String sourceEntityName;

    Map tokenCases;

}
