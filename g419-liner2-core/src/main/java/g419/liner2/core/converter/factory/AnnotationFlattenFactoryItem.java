package g419.liner2.core.converter.factory;

import com.google.common.collect.Lists;
import g419.lib.cli.ParameterException;
import g419.liner2.core.converter.AnnotationFlattenConverter;
import g419.liner2.core.converter.Converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class AnnotationFlattenFactoryItem extends ConverterFactoryItem {

  public AnnotationFlattenFactoryItem() {
    super("annotation-flatten:(.*\\.txt)");
  }

  @Override
  public Converter getConverter() throws ParameterException {
    final List<String> categories = Lists.newArrayList();
    final String filename = matcher.group(1);
    final File file = new File(filename);
    if (!file.exists()) {
      throw new ParameterException(String.format("The file %s given as a parameter of annotation-flatten does not exists", filename));
    }

    try (final BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line = br.readLine();
      while (line != null) {
        categories.add(line);
        line = br.readLine();
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    return new AnnotationFlattenConverter(categories);
  }
}
