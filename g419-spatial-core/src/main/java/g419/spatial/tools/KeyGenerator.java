package g419.spatial.tools;

public interface KeyGenerator<T> {

  String generateKey(T element);

  static KeyGenerator<Object> toStringKey() {
    return new KeyGenerator<Object>() {
      @Override
      public String generateKey(Object element) {
        return element.toString();
      }
    };
  }
}
