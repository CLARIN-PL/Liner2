package g419.corpus.structure;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RelationDesc {

    public static final String REL_STRING_DESC_SEPARATOR = ":";
    public static final String REL_STRING_DESC_ENTRY_END = "#";

    Sentence sentence;

    String type;
    int fromTokenIndex;
    String fromType;
    int toTokenIndex;
    String toType;

    public String toString() {
        StringBuffer relString = new StringBuffer(this.getType() + REL_STRING_DESC_SEPARATOR);
        relString.append((this.getFromTokenIndex()) + REL_STRING_DESC_SEPARATOR);
        relString.append(this.getFromType() + REL_STRING_DESC_SEPARATOR);
        relString.append((this.getToTokenIndex()) + REL_STRING_DESC_SEPARATOR);
        relString.append(this.getToType() );
        //relString.append(REL_STRING_DESC_ENTRY_END);

        return relString.toString();

    }

    static public RelationDesc from(Relation r) {
        RelationDesc relationDesc = RelationDesc.builder()
                .type(r.getType())
                .fromTokenIndex(r.getAnnotationFrom().getTokens().first() + 1)
                .fromType(r.getAnnotationFrom().getType())
                .toTokenIndex(r.getAnnotationTo().getTokens().first() + 1)
                .toType(r.getAnnotationTo().getType()).build();

        relationDesc.setSentence(r.getAnnotationFrom().getSentence());

        return relationDesc;
    }

    static public RelationDesc from(String s) {
        String[] parts = s.split(":");
        RelationDesc relationDesc = RelationDesc.builder()
                .type(parts[0])
                .fromTokenIndex(Integer.valueOf(parts[1]))
                .fromType(parts[2])
                .toTokenIndex(Integer.valueOf(parts[3]))
                .toType(parts[4]).build();

        return relationDesc;
    }


}
