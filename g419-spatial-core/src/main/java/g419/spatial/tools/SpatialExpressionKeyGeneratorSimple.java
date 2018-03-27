package g419.spatial.tools;

import g419.spatial.structure.SpatialExpression;

public class SpatialExpressionKeyGeneratorSimple implements KeyGenerator<SpatialExpression> {

    @Override
    public String generateKey(final SpatialExpression element) {
        return String.format("doc:%s_sent:%s_tr-so:%s_si:%s_lm-so:%s",
                Nuller.resolve(() -> element.getSpatialIndicator().getSentence().getDocument().getName()).orElse(""),
                Nuller.resolve(() -> element.getSpatialIndicator().getSentence().getId()).orElse(""),
                Nuller.resolve(() -> element.getTrajector().getSpatialObject().getHead().toString()).orElse(""),
                Nuller.resolve(() -> element.getSpatialIndicator().getHead().toString()).orElse(""),
                Nuller.resolve(() -> element.getLandmark().getSpatialObject().getHead().toString()).orElse(""));
    }
}
