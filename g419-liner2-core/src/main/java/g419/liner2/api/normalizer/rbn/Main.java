package g419.liner2.api.normalizer.rbn;

public class Main {
    /**
     * No overbloated CLI:<br>
     * <strong>PROG &lt;expression&gt; &lt;rulesFile&gt; </strong><br>
     * <em>expression</em> is text that should be normalized.<br>
     * <em>rulesFile</em> is path to JSON file containing rule definitions.<br>
     */
    public static void main(String[] args){
        try {
            //todo: be more verbose
            if (args.length!=2) throw new IllegalArgumentException("That is not how you use this program!");
            RuleSet ruleSet = RuleSetLoader.getInstance().load(args[1]);
            String expression = args[0];
            String result = ruleSet.normalize(expression);
            if (result!=null)
                System.out.println("Normalization of '"+expression+"' yields: '"+result+"'");
            else
                System.out.println("Expression '"+expression+"' couldn't be normalized.");
        } catch (Throwable t) {
            System.err.println("Something went wrong:");
            t.printStackTrace();
        }
    }
}
