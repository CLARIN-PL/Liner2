package g419.liner2.core.chunker.factory;

import g419.liner2.core.tools.ParameterException;
import g419.liner2.core.chunker.AnnotationToPropertyChunker;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.ClosestRelationChunker;
import org.ini4j.Ini;

/*
 * @author Jan Kocoń
 */

public class ChunkerFactoryItemClosestRelation extends ChunkerFactoryItem {

  public ChunkerFactoryItemClosestRelation() {
    super("closest-rel");
  }

  @Override
  public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
    String annotationFromPattern = this.getParameterString(description, "annotation_from_pattern");
    if (annotationFromPattern == null){
      throw new ParameterException("annotation_from_pattern can not be null");
    }
    String annotationToPattern = description.get("annotation_to_pattern");
    if (annotationToPattern == null){
      throw new ParameterException("annotation_to_pattern can not be null");
    }
    return new ClosestRelationChunker(annotationFromPattern, annotationToPattern);
  }
}
