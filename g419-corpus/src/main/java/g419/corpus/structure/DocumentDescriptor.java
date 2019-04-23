package g419.corpus.structure;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DocumentDescriptor {
  protected final Map<String, String> description;
  protected final Map<String, String> metadata;

  public DocumentDescriptor() {
    description = new HashMap<>();
    metadata = new HashMap<>();
    //as default DCT=now() if not in INI
    setDescription("date", (new SimpleDateFormat("yyyy-MM-dd")).format(Calendar.getInstance().getTime()));

  }

  public Map<String, String> getDescription() {
    return description;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public String getDescription(final String key) {
    return description.get(key);
  }

  public String getMetadata(final String key) {
    return metadata.get(key);
  }

  public void setDescription(final String key, final String value) {
    description.put(key, value);
  }

  public void setMetadata(final String key, final String value) {
    metadata.put(key, value);
  }

  @Override
  public String toString() {
    return "DocumentDescriptor{" +
        "description=" + description +
        ", metadata=" + metadata +
        '}';
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DocumentDescriptor)) {
      return false;
    }

    final DocumentDescriptor that = (DocumentDescriptor) o;

    if (!description.equals(that.description)) {
      return false;
    }
    if (!metadata.equals(that.metadata)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = description.hashCode();
    result = 31 * result + metadata.hashCode();
    return result;
  }

  @Override
  public DocumentDescriptor clone() {
    final DocumentDescriptor desc = new DocumentDescriptor();
    for (final Map.Entry<String, String> entry : description.entrySet()) {
      desc.setDescription(entry.getKey(), entry.getValue());
    }
    for (final Map.Entry<String, String> entry : metadata.entrySet()) {
      desc.setMetadata(entry.getKey(), entry.getValue());
    }
    return desc;
  }
}
