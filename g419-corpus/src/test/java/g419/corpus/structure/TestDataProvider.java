package g419.corpus.structure;

public class TestDataProvider {

    /**
     * Tworzy przykładowe zdanie "Ala ma kota.".
     * @return
     */
    public static Sentence sentence_Ala_ma_kota(TokenAttributeIndex index){
        Sentence sentence = new Sentence(index);
        sentence.setId("1");
        if ( index.getIndex("orth") == -1){
            index.addAttribute("orth");
        }
        sentence.addToken(token_Ala(index));
        sentence.addToken(token_ma(index));
        sentence.addToken(token_kota(index));
        sentence.addToken(token_dot(index));
        return sentence;
    }

    public static TokenAttributeIndex getTokenAttributeIndex() {
        TokenAttributeIndex index = new TokenAttributeIndex();
        if (index.getIndex("orth") == -1) {
            index.addAttribute("orth");
        }
        return index;
    }

    public static Token token_Ala(TokenAttributeIndex index){
        return new Token("Ala", tag_Ala() , index);
    }

    public static Token token_token_with_props(TokenAttributeIndex index){
        Token t =  new Token("Ala", tag_Ala() , index);
        t.getProps().put("PlWN:url_0", "http://plwordnet.pwr.wroc.pl/wordnet/synset/81394");
        t.getProps().put("sense:ukb:syns_id", "6246");
        t.getProps().put("sense:ukb:syns_rank", "6247/92.4444902716");
        t.getProps().put("sense:ukb:unitsstr", "użytkownik.1(15:os)");
        return t;
    }

    public static Token token_Ala_dubble(TokenAttributeIndex index){
        Token t =  new Token("Ala", tag_Ala() , index);
        t.addTag(tag_Ala_diff_ctag());
        return t;
    }

    public static Token token_ma(TokenAttributeIndex index){
        return new Token("ma", tag_miec(), index);
    }

    public static Token token_kota(TokenAttributeIndex index){
        return new Token("kota", tag_kot(), index);
    }

    public static Token token_dot(TokenAttributeIndex index){
        return new Token(".", tag_dot(), index);
    }

    public static Tag tag_Ala(){
        return new Tag("Ala", "subst:sg:nom:f", true);
    }

    public static Tag tag_Ala_diff_ctag(){
        return new Tag("Ala", "adj:adjp", true);
    }

    public static Tag tag_miec(){
        return  new Tag("mieć", "fin:sg:ter:imperf", true);
    }

    public static Tag tag_kot(){
        return new Tag("kot", "subst:sg:gen:m2", true);
    }

    public static Tag tag_with_null_base(){
        return new Tag(null, "subst:sg:gen:m2", true);
    }

    public static Tag tag_with_null_ctag(){
        return new Tag("kot", null, true);
    }

    public static Tag tag_with_disamb_false(){
        return new Tag("kot", "subst:sg:gen:m2", false);
    }

    public static Tag tag_dot(){
        return  new Tag(".", "interp", true);
    }
}
