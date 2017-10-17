package g419.liner2.core.converter.factory;

import g419.corpus.structure.CrfTemplate;
import g419.liner2.core.converter.Converter;
import g419.liner2.core.converter.TokenFeaturesConverter;
import g419.liner2.core.tools.TemplateFactory;

/**
 * Created by michal on 9/25/14.
 */
public class TokenFeaturesFactoryItem extends ConverterFactoryItem {

    public TokenFeaturesFactoryItem(){ super("expand-features:(.*\\.txt)");}
    @Override
    public Converter getConverter() {
        CrfTemplate template = null;
        try {
            template = TemplateFactory.parseTemplate(matcher.group(1));
        } catch (Exception e) {
            System.out.println(e);
        }
        return new TokenFeaturesConverter(template);
    }
}
