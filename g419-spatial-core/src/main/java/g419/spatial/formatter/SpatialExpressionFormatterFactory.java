package g419.spatial.formatter;

public class SpatialExpressionFormatterFactory {

    public static ISpatialExpressionFormatter create(String name){
        switch (name){
            case "tree":
                return new SpatialExpressionFormatterTree();
            case "tsv":
                return new SpatialExpressionFormatterTsv();
            default:
                throw new RuntimeException("Unknow spatial expressions formatter: " + name);
        }
    }

}
