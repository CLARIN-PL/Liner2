package g419.spatial.tools

import com.google.common.collect.Lists
import g419.liner2.core.tools.FscoreEvaluator
import spock.lang.Specification

class DecisionCollectorTest extends Specification {

    def "DecisionCollector should produce valid confuction matrix"(){
        given:
            DecisionCollector<String> collector = new DecisionCollector<>(KeyGenerator.toStringKey())

        when:
            collector.addGold("item1")
        then:
            collector.getGold().size()==1
            collector.getDecision().size()==0
            collector.containsAsGold("item1") == true
            collector.containsAsGold("item2") == false

        when:
            collector.addAllGold(Lists.newArrayList("item2", "item3", "item4"))
        then:
            collector.getGold().size()==4
            collector.getDecision().size()==0
            collector.containsAsGold("item2") == true
            collector.containsAsGold("item3") == true
            collector.containsAsGold("item4") == true

        when:
            collector.addDecision("item1")
        then:
            collector.getGold().size()==4
            collector.getDecision().size()==1
            collector.containsAsDecision("item1") == true

        when:
            collector.addAllDecision(Lists.newArrayList("item2", "item5", "item6"))
        then:
            collector.getGold().size()==4
            collector.getDecision().size()==4
            collector.containsAsDecision("item2") == true
            collector.containsAsDecision("item5") == true
            collector.containsAsDecision("item6") == true
            collector.containsAsDecision("item3") == false

        when:
            FscoreEvaluator eval = collector.getConfusionMatrix()
        then:
            collector.getTruePositives().sort() == ["item1","item2"]
            collector.getFalsPositives().sort() == ["item5","item6"]
            collector.getFalseNegative().sort() == ["item3","item4"]
            eval.getTruePositiveCount() == 2
            eval.getFalsePositiveCount() == 2
            eval.getFalsePositiveCount() == 2

    }

}
