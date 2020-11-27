package g419.serel.ruleTree


import g419.corpus.structure.Token
import g419.corpus.structure.TokenAttributeIndex
import org.antlr.v4.runtime.misc.ParseCancellationException
import spock.lang.Specification

class PatternMatchTest extends Specification {

    List<Token> tokens;

    def setup() {

        TokenAttributeIndex tai = new TokenAttributeIndex();
        tai.addAttribute("id")
        tai.addAttribute("orth")
        tai.addAttribute("lemma")
        tai.addAttribute("upos")
        tai.addAttribute("xpos")
        tai.addAttribute("misc")
        tai.addAttribute("head")
        tai.addAttribute("deprel")

        List<List<String>> tokensValues = List.of(
                List.of("1", "W", "w", "ADP", "tprep:loc:nwok", "2", "case"),
                List.of("2", "południe", "południ", "NOUN", "subst:sg:loc:m3", "3", "obl"),
                List.of("3", "odbywają", "odbywać", "VERB", "fin:pl:ter:imperf", "0", "root"),
                List.of("4", "się", "się", "PRON", "part", "3", "expl:pv"),
                List.of("5", "koncerty", "koncert", "NOUN", "subst:pl:nom:m3", "3", "nsubj"),
                List.of("6", "jazzowe", "jazzowy", "ADJ", "adj:pl:nom:m3:pos", "5", "amod"),
                List.of("7", ".", ".", "PUNCT", "interp", "3", "punct")
        )


        tokens = new ArrayList<>();

        for (int i = 0; i < tokensValues.size(); i++) {
            List<String> tokenValues = tokensValues.get(i);

            Token token = new Token(tai);
            token.setAttributeValue("id", tokenValues.get(0));
            token.setAttributeValue("orth", tokenValues.get(1));
            token.setAttributeValue("lemma", tokenValues.get(2))
            token.setAttributeValue("upos", tokenValues.get(3))
            token.setAttributeValue("xpos", tokenValues.get(4))
            token.setAttributeValue("head", tokenValues.get(5))
            token.setAttributeValue("deprel", tokenValues.get(6))

            tokens.add(token)
        }
    }

    def "ParseRule should parse single segment"() {
        given:
            PatternMatch result = PatternMatch.parseRule("miasto")
            result.dump()
        expect:
            result != null
        and:
            result.rootNodeMatch.text.equals("miasto")
    }

    def "ParseRule should not parse single invalid segment"() {
        when:
            PatternMatch result = PatternMatch.parseRule("miasto <")
        then:
            thrown ParseCancellationException
    }

    def "ParseRule should accept single char * as a text pattern"() {
        given:
            PatternMatch result = PatternMatch.parseRule(" * ")
        expect:
            result.rootNodeMatch != null
    }

    def "ParseRule should not accept *  mixed with text as a text pattern"() {
        when:
            PatternMatch result = PatternMatch.parseRule(" mias* ")
        then:
            thrown ParseCancellationException
    }

    def "ParseRule should recognize and accept xPos part"() {
        given:
            PatternMatch result = PatternMatch.parseRule("[subst] miasto ")
        expect:
            result.rootNodeMatch.text.equals("miasto")
            result.rootNodeMatch.getXPos().equals("subst")

    }

    def "ParseRule should recognize namedEntity name with role"() {
        given:
            PatternMatch result = PatternMatch.parseRule("miasto   / nam_geo_loc:source ")
        expect:
            result.rootNodeMatch.text.equals("miasto")
            result.rootNodeMatch.namedEntity.equals("nam_geo_loc")
            result.rootNodeMatch.role.equals("source")
    }

    def "ParseRule should recognize xPos with namedEntity name with role"() {
        given:
            PatternMatch result = PatternMatch.parseRule(" [subst] ^stolica / nam_geo_loc:source ")
        expect:
            result.rootNodeMatch.isMatchLemma() == true
            result.rootNodeMatch.text.equals("stolica")
            result.rootNodeMatch.getXPos().equals("subst")
            result.rootNodeMatch.namedEntity.equals("nam_geo_loc")
            result.rootNodeMatch.role.equals("source")
    }


    def "ParseRule should recognize two segments"() {
        given:
            PatternMatch result = PatternMatch.parseRule("jest > stolicą ")
        expect:
            result.rootNodeMatch.text.equals("stolicą")
            result.rootNodeMatch.edgeMatchList.size() == 1;
            result.rootNodeMatch.edgeMatchList.get(0).side.equals("left")
            result.rootNodeMatch.edgeMatchList.get(0).parentNodeMatch == result.rootNodeMatch
            result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.text.equals("jest")
            result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.parentEdgeMatch == result.rootNodeMatch.edgeMatchList.get(0)

    }

    def "ParseRule should recognize depRel of link of two segments"() {
        given:
            PatternMatch result = PatternMatch.parseRule("jest (nmod) > stolicą ")
            NodeMatch nm = result.getALeaf()
        expect:
            result.rootNodeMatch.text.equals("stolicą")
            result.rootNodeMatch.edgeMatchList.size() == 1;
            result.rootNodeMatch.edgeMatchList.get(0).side.equals("left")
            result.rootNodeMatch.edgeMatchList.get(0).parentNodeMatch == result.rootNodeMatch
            result.rootNodeMatch.edgeMatchList.get(0).depRel.equals("nmod")
            result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.text.equals("jest")
            result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.parentEdgeMatch == result.rootNodeMatch.edgeMatchList.get(0)

            nm.edgeMatchList.size() == 0
    }


    def "ParseRule should recognize relation type"() {
        given:
            PatternMatch result = PatternMatch.parseRule(" location ::  */ nam_fac_goe: source <(root) * <( nmod ) * / nam_loc_gpe_city:target ")
            NodeMatch nm = result.getALeaf()
        expect:
            result.relationType.equals("location")
            result.rootNodeMatch.isMatchAnyText() == true
            result.rootNodeMatch.namedEntity.equals("nam_fac_goe");
            result.rootNodeMatch.role.equals("source");
            result.rootNodeMatch.edgeMatchList.size() == 1;
            result.rootNodeMatch.edgeMatchList.get(0).side.equals("right")
            result.rootNodeMatch.edgeMatchList.get(0).parentNodeMatch == result.rootNodeMatch
            result.rootNodeMatch.edgeMatchList.get(0).depRel.equals("root")
            result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.isMatchAnyText() == true
            result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.parentEdgeMatch == result.rootNodeMatch.edgeMatchList.get(0)
            result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.edgeMatchList.size() == 1;
            result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.edgeMatchList.get(0).depRel.equals("nmod");
            result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.edgeMatchList.get(0).parentNodeMatch == result.rootNodeMatch.edgeMatchList.get(0).nodeMatch

            result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.edgeMatchList.get(0).nodeMatch.isMatchAnyText() == true
            result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.edgeMatchList.get(0).nodeMatch.namedEntity.equals("nam_loc_gpe_city")
            result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.edgeMatchList.get(0).nodeMatch.role.equals("target")
            result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.edgeMatchList.get(0).nodeMatch.parentEdgeMatch == result.rootNodeMatch.edgeMatchList.get(0).nodeMatch.edgeMatchList.get(0)

            nm.edgeMatchList.size() == 0

    }

    def "ParseRule should recognize badly formed depRels "() {
        when:
            PatternMatch result = PatternMatch.parseRule(" W > stolicy < * > kraju > *")
        then:
            thrown ParseCancellationException
    }

    def "ParseRule should recognize misspeling in brackets for xPos formed depRels "() {
        when:
            PatternMatch result = PatternMatch.parseRule(" W >[susbst stolicy < * > kraju > *")
        then:
            thrown ParseCancellationException
    }

    def "ParseRule entityName should always have a role  "() {
        when:
            PatternMatch result = PatternMatch.parseRule("Stolica / nam_log_geo < * < kraju")
        then:
            thrown ParseCancellationException
    }

    def "ParseRule - there should be always root node   "() {
        when:
            PatternMatch result = PatternMatch.parseRule("Stolica  < * > kraju")
        then:
            thrown ParseCancellationException
    }

    def "getSentenceBranchMatchingUpPatternBranchFromNode zwraca podgałąź tokenów zdania "() {
        when:
            PatternMatch pattern = PatternMatch.parseRule("W > południe")
            NodeMatch startNodeMatch = pattern.getALeaf();
            int startTokenIndex = 0;
            List<Integer> result = pattern.getSentenceBranchMatchingUpPatternBranchFromNode(startNodeMatch, tokens, startTokenIndex)
        then:
            result.size() == 2;
    }

    def "getSentenceBranchMatchingUpPatternBranchFromNode nie zwraca podgałęzi tokenów zdania gdy zaczynamy od złego punktu"() {
        when:
            PatternMatch pattern = PatternMatch.parseRule("W > południe")
            NodeMatch startNodeMatch = pattern.getALeaf();
            int startTokenIndex = 1;
            List<Integer> result = pattern.getSentenceBranchMatchingUpPatternBranchFromNode(startNodeMatch, tokens, startTokenIndex)
        then:
            result.size() == 0;
    }


    def "getSentenceBranchMatchingUpPatternBranchFromNode nie zwraca odwrotnej podgałęzi tokenów zdania "() {
        when:
            PatternMatch pattern = PatternMatch.parseRule("W < południe")
            NodeMatch startNodeMatch = pattern.getALeaf();
            int startTokenIndex = 0;
            List<Integer> result = pattern.getSentenceBranchMatchingUpPatternBranchFromNode(startNodeMatch, tokens, startTokenIndex)
        then:
            result.size() == 0;
    }

    def "getSentenceBranchMatchingUpPatternBranchFromNode zwraca podgałęź tokenów zdania gdy wyszukiwanie tylko po xpos"() {
        when:
            PatternMatch pattern = PatternMatch.parseRule("[subst] * > [fin] *")
            NodeMatch startNodeMatch = pattern.getALeaf();
            int startTokenIndex = sTI;
            List<Integer> result = pattern.getSentenceBranchMatchingUpPatternBranchFromNode(startNodeMatch, tokens, startTokenIndex)
        then:
            result.size() == rSize;
        where:
            sTI || rSize
            0   || 0
            1   || 2
            2   || 0
            3   || 0
            4   || 2
            5   || 0
            6   || 0

    }

    def "getSentenceBranchMatchingUpPatternBranchFromNode zwraca podgałęzie większe niż 2 elementy "() {
        when:
            PatternMatch pattern = PatternMatch.parseRule("jazzowe > koncerty > odbywają")
            NodeMatch startNodeMatch = pattern.getALeaf();
            int startTokenIndex = 5;
            List<Integer> result = pattern.getSentenceBranchMatchingUpPatternBranchFromNode(startNodeMatch, tokens, startTokenIndex)
        then:
            result.size() == 3;
    }


    def "getSentenceBranchMatchingUpPatternBranchFromNode zwraca podgałęzie większe niż 2 elementy i wyszukiwane po lemma"() {
        when:
            PatternMatch pattern = PatternMatch.parseRule("^jazzowy > ^koncert > ^odbywać")
            NodeMatch startNodeMatch = pattern.getALeaf();
            int startTokenIndex = 5;
            List<Integer> result = pattern.getSentenceBranchMatchingUpPatternBranchFromNode(startNodeMatch, tokens, startTokenIndex)
        then:
            result.size() == 3;
    }

    def "getSentenceBranchMatchingUpPatternBranchFromNode nie zwraca nic gdy pattern wychodzi poza zdanie"() {
        when:
            PatternMatch pattern = PatternMatch.parseRule("^jazzowy > ^koncert > ^odbywać > się")
            NodeMatch startNodeMatch = pattern.getALeaf();
            int startTokenIndex = 5;
            List<Integer> result = pattern.getSentenceBranchMatchingUpPatternBranchFromNode(startNodeMatch, tokens, startTokenIndex)
        then:
            result.size() == 0;
    }


}

