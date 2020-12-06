package g419.serel.action


import spock.lang.Specification

class ActionMatchRelationsTest extends Specification {


    def "Simple test "() {
        when:
            String str = "1"
        then:
            !str.isEmpty()
    }


    /*
    def "basic processing of file "() {
        when:
            ActionMatchRelations amr = new ActionMatchRelations();
            amr.setInputFilename("/home/user57/NLPWR/corpora/KPWr/conllnr/documents/00099883.conllu")
            amr.setInputFormat("conllu")
            //amr.setRule(" location:: * /nam_fac_goe :source < * < (nmod) * / namlocgpecity : target ");
            amr.setRule(" * / nam_loc_gpe_city : target ");
            amr.run();

        then:
            amr.result.size() == 4

    }

    def "basic processing of file 2"() {
        when:
            ActionMatchRelations amr = new ActionMatchRelations();
            amr.setInputFilename("/home/user57/NLPWR/corpora/KPWr/conllnr/documents/00099883.conllu")
            amr.setInputFormat("conllu")
            amr.setRule(" * / nam_loc_gpe_city : target < Dominion <Centre ");
            amr.run();
        then:
            amr.result.size() == 2
    }


    def "basic processing of file 3"() {
        when:
            ActionMatchRelations amr = new ActionMatchRelations();
            amr.setInputFilename("/home/user57/NLPWR/corpora/KPWr/conllnr/documents/00099883.conllu")
            amr.setInputFormat("conllu")
            amr.setRule(" * / nam_liv_person : person");
            amr.run();
        then:
            amr.result.size() != null;
    }


    def "batch processing of files - nam_liv_person"() {
        when:
            ActionMatchRelations amr = new ActionMatchRelations();
            amr.setInputFilename("/home/user57/NLPWR/corpora/KPWr/conllnr/index.list1")
            amr.setInputFormat("batch:conllu")
            amr.setRule(" * / nam_liv_person : person");
            amr.run();
        then:
            amr.result.size() != null;
    }

    def "batch processing of files - nam_loc_goe"() {
        when:
            ActionMatchRelations amr = new ActionMatchRelations();
            amr.setInputFilename("/home/user57/NLPWR/corpora/KPWr/conllnr/index.list1")
            amr.setInputFormat("batch:conllu")
            amr.setRule(" * / nam_fac_goe : location");
            amr.run();
        then:
            amr.result.size() != null;
    }
    
     */


}
