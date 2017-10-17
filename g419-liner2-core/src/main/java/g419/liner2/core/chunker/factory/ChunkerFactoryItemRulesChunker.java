package g419.liner2.core.chunker.factory;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.RulesChunker;
import g419.liner2.core.features.tokens.TestRuleFeature;
import org.ini4j.Ini;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by michal on 5/22/15.
 */
public class ChunkerFactoryItemRulesChunker extends ChunkerFactoryItem {
    public ChunkerFactoryItemRulesChunker() {
        super("rules");
    }

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        ArrayList<TestRuleFeature> rules = new ArrayList<>();
        Files.lines(Paths.get(description.get("rules"))).forEach(l -> rules.add(parseRule(l)));
        return new RulesChunker(rules);
    }

    private TestRuleFeature parseRule(String line){
        int colonIdx = line.indexOf(":");
        String name = line.substring(0, colonIdx);
        String rule = line.substring(colonIdx + 1);
        return new TestRuleFeature(name, rule);
    }
}
