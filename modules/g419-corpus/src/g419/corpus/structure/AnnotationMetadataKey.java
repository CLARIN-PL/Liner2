package g419.corpus.structure;

/**
 * Created by michal on 2/3/15.
 */
public enum AnnotationMetadataKey {
    LVAL("lemma", "lval");


    AnnotationMetadataKey(String cclKey, String modelKey) {
        this.cclKey = cclKey;
        this.modelKey = modelKey;
    }

    public final String cclKey;
    public final String modelKey;

    private static final AnnotationMetadataKey[] vals = AnnotationMetadataKey.values();

    public static AnnotationMetadataKey findForCclKey(String cclKey){
        for (AnnotationMetadataKey amk: vals)
            if (amk.cclKey.equals(cclKey))
                return amk;
        return null;
    }

    public static AnnotationMetadataKey findForModelKey(String modelKey){
        for (AnnotationMetadataKey amk: vals)
            if (amk.modelKey.equals(modelKey))
                return amk;
        return null;
    }
}
