package g419.liner2.api.normalizer.timex.entities;

import g419.liner2.api.normalizer.timex.constraints.EntityConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractEntity implements TimexEntity{
    public static String overwrite(String unfilled, String filling, char toReplace){
        if (unfilled == null)
            return unfilled;
        return new String(overwrite(unfilled.toCharArray(), filling.toCharArray(), toReplace));
    }

    public static char[] overwrite(char[] unfilled, char[] filling, char toReplace){
        char[] out = Arrays.copyOf(unfilled, unfilled.length);
        for (int i=0; i<out.length && i<filling.length; i++) {
            if (out[i]==toReplace)
                out[i] = filling[i];
        }
        return out;
    }

    protected static String withDefault(String str, String defVal){
        if (str==null)
            return defVal;
        return str;
    }

    protected static boolean isEmptyShift(String rest){
        return Pattern.compile("[+][0-]+").matcher(rest).matches();
    }

    boolean isSpecified(){
        return true;
    }

    public static boolean isFullySpecified(String timex, char... unspecifiedCharacters){
        for (char c: unspecifiedCharacters) {
            String cStr = ""+c;
            if (timex.contains(cStr))
                return false;
        }
        return true;
    }

    public static void throwUp(boolean when, String message){
        if (when) {
            //todo: dedicated exception
            RuntimeException up = new RuntimeException(message);
            throw up;
        }
    }

    @Override
    public boolean isFullySpecified(){
        return isSpecified() && isFullySpecified(toTimex(), 'x');
    }

    boolean isKnown(){
        return true;
    }

    @Override
    public boolean isFullyKnown() {
        return isFullySpecified() && isKnown();
    }

    @Override
    public void assertConstraintsSatisfied(Collection<? extends EntityConstraint> constraints){
        List<EntityConstraint> unsatisfied = new ArrayList<>();
        for (EntityConstraint constraint: constraints){
            if (!constraint.isSatisfied(this))
                unsatisfied.add(constraint);
        }
        if (unsatisfied.isEmpty())
            return;
        String msg = "Following constraints were not satisfied for entity"+this+": ";
        for (EntityConstraint constraint: unsatisfied)
            msg += constraint.getDescription()+"; ";
        msg = msg.substring(0, msg.length()-2);
        throwUp(!unsatisfied.isEmpty(), msg);
    }
}
