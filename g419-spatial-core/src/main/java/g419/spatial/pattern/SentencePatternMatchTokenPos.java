package g419.spatial.pattern;

public class SentencePatternMatchTokenPos extends SentencePatternMatch {

    final String pos;

    public SentencePatternMatchTokenPos(final String pos){
        this.pos = pos;
    }

    @Override
    boolean match(SentencePatternContext context, Integer begin, Integer end) {
        if (context.getSentence().getTokens().get(context.getCurrentPos()).getDisambTag().getPos().equals(pos)){
            context.increaseCurrentPos();
            return true;
        } else {
            return false;
        }
    }
}
