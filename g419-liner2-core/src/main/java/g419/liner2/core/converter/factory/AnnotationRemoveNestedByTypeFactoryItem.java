package g419.liner2.core.converter.factory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import g419.lib.cli.ParameterException;
import g419.liner2.core.converter.AnnotationRemoveNestedByTypeConverter;
import g419.liner2.core.converter.Converter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotationRemoveNestedByTypeFactoryItem extends ConverterFactoryItem {

  public AnnotationRemoveNestedByTypeFactoryItem() {
    super("annotation-remove-nested-by-type:(.*\\.txt)");
  }


  @Override
  public Converter getConverter() throws ParameterException {
    final Set<String> typesUnconditioned = Sets.newHashSet();
    final Map<String, Set<String>> typesConditionedByOuterType = Maps.newHashMap();
    final File file = Paths.get(matcher.group(1)).toFile();

    if (!file.exists()) {
      throw new ParameterException(String.format("The file %s given as a parameter of annotation-remove-nested-by-type does not exists", file.getName()));
    }

    try {
      FileUtils.readLines(file, "utf8").forEach(line -> parseLine(line, typesUnconditioned, typesConditionedByOuterType));
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
    typesUnconditioned.forEach(typesUnconditioned::remove);

    return new AnnotationRemoveNestedByTypeConverter(typesUnconditioned, typesConditionedByOuterType);
  }

  private void parseLine(final String line, final Set<String> typesUnconditioned, final Map<String, Set<String>> typesConditionedByOuterType) {
    final String[] cols = line.trim().split("( )+");
    if (cols.length == 1) {
      typesUnconditioned.add(cols[0]);
    } else if (cols.length > 1) {
      final String type = cols[0];
      typesConditionedByOuterType.computeIfAbsent(type, k -> Sets.newHashSet()).addAll(
          Arrays.stream(cols)
              .filter(c -> !Objects.equals(c, type))
              .collect(Collectors.toSet()));
    }
  }
}
