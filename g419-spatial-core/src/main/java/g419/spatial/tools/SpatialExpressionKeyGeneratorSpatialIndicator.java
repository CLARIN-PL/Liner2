package g419.spatial.tools;

import g419.spatial.structure.SpatialExpression;

public class SpatialExpressionKeyGeneratorSpatialIndicator implements KeyGenerator<SpatialExpression> {

  @Override
  public String generateKey(final SpatialExpression element) {
    return String.format("doc:%s_sent:si:%s",
        Nuller.resolve(() -> element.getSpatialIndicator().getSentence().getDocument().getName()).orElse(""),
        Nuller.resolve(() -> element.getSpatialIndicator().getSentence().getId()).orElse(""),
        Nuller.resolve(() -> element.getSpatialIndicator().getHead().toString()).orElse(""));
  }
}
