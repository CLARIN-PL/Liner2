package g419.liner2.api.converter;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.CrfTemplate;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.api.tools.TemplateFactory;

import java.util.LinkedHashSet;

/**
 * Created by michal on 8/20/14.
 */
public class TokenFeaturesConverter extends Converter {

    CrfTemplate template;

    public TokenFeaturesConverter(CrfTemplate template){
        this.template = template;
    }

    @Override
    public void apply(Sentence sentence) {
        Sentence expanded = template.expandAttributes(sentence);
        sentence.setTokens(expanded.getTokens());
        sentence.setAttributeIndex(expanded.getAttributeIndex());
    }

    @Override
    public void finish(Document doc) {
        doc.setAttributeIndex(template.expandAttributeIndex(doc.getAttributeIndex()));
    }
}
