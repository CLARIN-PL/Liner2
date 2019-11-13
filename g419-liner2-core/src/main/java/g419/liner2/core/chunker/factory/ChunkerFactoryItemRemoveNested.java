package g419.liner2.core.chunker.factory;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.RemoveNestedChunker;
import org.ini4j.Ini;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class ChunkerFactoryItemRemoveNested extends ChunkerFactoryItem {

  public static String INI_TYPES = "annotation-types";

  public ChunkerFactoryItemRemoveNested() {
    super("remove-nested");
  }

  @Override
  public Chunker getChunker(final Ini.Section description, final ChunkerManager cm) throws Exception {
    g419.corpus.ConsolePrinter.log("--> RemoveNested chunker");

    final List<Pattern> types = new ArrayList<>();

    if (description.containsKey(INI_TYPES)) {
      for (final String type : description.get(INI_TYPES).split(";")) {
        types.add(Pattern.compile("^" + type.trim() + "$"));
      }
    } else {
      types.add(Pattern.compile("^.+$"));
    }

    return new RemoveNestedChunker(types);
  }

}
