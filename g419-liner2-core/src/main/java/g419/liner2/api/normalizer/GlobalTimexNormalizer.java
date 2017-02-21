package g419.liner2.api.normalizer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.liner2.api.normalizer.timex.TimexUtils;
import g419.liner2.api.normalizer.timex.entities.TimexEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

public class GlobalTimexNormalizer extends NormalizingChunker{
    static Logger logger = LoggerFactory.getLogger(GlobalTimexNormalizer.class);

    protected boolean doUpdate = true;

    public GlobalTimexNormalizer(List<Pattern> normalizedChunkTypes) {
        //todo: I suppose this may be somehow used for other global normalizations basing on local ones, but this is future work
        super(normalizedChunkTypes);
    }

    public GlobalTimexNormalizer(List<Pattern> normalizedChunkTypes, boolean doUpdate) {
        super(normalizedChunkTypes);
        this.doUpdate = doUpdate;
    }

    protected TimexEntity lastDate = null;

    @Override
    public void normalize(Annotation annotation) {
        if (shouldNormalize(annotation)) {
            String lval = annotation.getMetadata("lval");
            if (lval != null) {
                TimexEntity fromAnnotation = TimexUtils.parse(lval);
                logger.debug("LVAL = "+fromAnnotation+"; lastDate = "+lastDate);
                if (lastDate != null) {
                    TimexEntity result = fromAnnotation.fill(lastDate);
//                    if (annotation.getType().equals("t3_time")) // doesn't work, TP=3
//                        result.year = "xxxx";
                    logger.debug("VAL = "+result);
                    annotation.setMetadata("val", result.toTimex());
                    if (doUpdate && result.isFullySpecified()) {
                        lastDate = result;
                        logger.debug("Last date is result = "+lastDate);
                    }
                } else if (doUpdate) {
                    lastDate = fromAnnotation;
                    logger.debug("Setting previous from annotation = "+lastDate);
                }

            }
        }
    }

    public void onNewDocument(Document document){
        String dateStr = document.getDocumentDescriptor().getDescription().get("date");
        if (dateStr != null)
            lastDate = TimexUtils.parse(dateStr.trim());
        logger.debug("New document, previous = "+lastDate);

    }

    @Override
    public void onDocumentEnd(Document document) {

        lastDate = null;
    }

    @Override
    public String toString() {
        return "GlobalTimexNormalizer{" +
                "doUpdate=" + doUpdate +
                '}';
    }
}
