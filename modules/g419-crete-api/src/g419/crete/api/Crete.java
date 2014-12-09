package g419.crete.api;


public class Crete {

    private CreteOptions opts;

    public Crete(String ini){
        opts = new CreteOptions();
        opts.parseModelIni(ini);
        
        System.out.println("CRETE created");
    }

}
