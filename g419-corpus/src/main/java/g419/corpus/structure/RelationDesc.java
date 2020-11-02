package g419.corpus.structure;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RelationDesc {

    public static final String REL_STRING_DESC_SEPARATOR = ":";
    public static final String REL_STRING_DESC_ENTRY_END = "#";

    String type;
    int annFromTokenIndex;
    String annFromType;
    int annToTokenIndex;
    String annToType;

    public String toString() {
        StringBuffer relString = new StringBuffer(this.getType() + REL_STRING_DESC_SEPARATOR);
        relString.append((this.getAnnFromTokenIndex()) + REL_STRING_DESC_SEPARATOR);
        relString.append(this.getAnnFromType() + REL_STRING_DESC_SEPARATOR);
        relString.append((this.getAnnToTokenIndex()) + REL_STRING_DESC_SEPARATOR);
        relString.append(this.getAnnToType() + REL_STRING_DESC_SEPARATOR);
        relString.append(REL_STRING_DESC_ENTRY_END);

        return relString.toString();

    }

    static public RelationDesc from(Relation r) {
        RelationDesc relationDesc = RelationDesc.builder()
                .type(r.getType())
                .annFromTokenIndex(r.getAnnotationFrom().getTokens().first() + 1)
                .annFromType(r.getAnnotationFrom().getType())
                .annToTokenIndex(r.getAnnotationTo().getTokens().first() + 1)
                .annToType(r.getAnnotationTo().getType()).build();

        return relationDesc;
    }

    static public RelationDesc from(String s) {
        String[] parts = s.split(":");
        RelationDesc relationDesc = RelationDesc.builder()
                .type(parts[0])
                .annFromTokenIndex(Integer.valueOf(parts[1]))
                .annFromType(parts[2])
                .annToTokenIndex(Integer.valueOf(parts[3]))
                .annToType(parts[4]).build();

        return relationDesc;
    }


}
