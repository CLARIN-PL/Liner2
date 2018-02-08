package g419.spatial.tools;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.spatial.structure.SpatialExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts set of annotations and relations between annotations into a set of spatial expressions.
 */
public class DocumentToSpatialExpressionConverter {

    /** Annotation type which represents spatial objects */
    private String annotationSpatialObject = "spatial_object3";

    /** Annotation type which represents spatial indicator */
    private String annotationSpatialIndicator = "spatial_indicator3";

    private String annotationRegion = "region3";

    private String annotationPathIndicator = "path_indicator3";

    private String annotationMotionIndicator = "motion_indicator3";

    private String annotationDirection = "direction3";

    private String annotationDistance = "distance3";

    /** */
    private String relationLandmark = "landmark";

    private String relationTrajector = "trajector";

    private String relationArgument = "argument";

    /**
     * Convert document annotations and relations into a set of spatial expression structures.
     * @param document
     * @return
     */
    public List<SpatialExpression> convert(Document document){
        List<SpatialExpression> ses = new ArrayList<>();

        for (Annotation an : document.getAnnotations() ){
            System.out.println(an.getText());
            if ( an.getType().equals(this.annotationSpatialObject) ){
                System.out.println(an.getText());
            }
        }


        return ses;
    }

}
