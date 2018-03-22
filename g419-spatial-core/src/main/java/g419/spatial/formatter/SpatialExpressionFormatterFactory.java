package g419.spatial.formatter;

public final class SpatialExpressionFormatterFactory {

    public static ISpatialExpressionFormatter create(final String name){
        if ( name == null ){
            throw new RuntimeException("Name of spatial expressions formatter cannot be null");
        }
        switch (name){
            case "tree":
                return new SpatialExpressionFormatterTree();
            case "tsv":
                return new SpatialExpressionFormatterTsv();
            default:
                throw new RuntimeException("Unknown spatial expressions formatter: " + name);
        }
    }

}
