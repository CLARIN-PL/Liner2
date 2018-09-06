package g419.corpus.format;

import java.util.regex.Pattern;

public class Iob {

    final public static String IOB_HEADER_PREFIX = "-DOCSTART CONFIG FEATURES";

    final public static String IOB_FILE_PREFIX = "-DOCSTART FILE";

    final public static String IOB_COLUMN_SEPARATOR = "\t";

    final public static Pattern IOB_LABEL_PATTERN = Pattern.compile("([IB])-([^#]*)");

}
