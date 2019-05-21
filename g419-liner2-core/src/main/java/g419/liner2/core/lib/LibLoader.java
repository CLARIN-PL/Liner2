package g419.liner2.core.lib;

import com.google.common.collect.Sets;
import g419.liner2.core.Liner2;

import java.io.*;
import java.util.Set;

public abstract class LibLoader {

  private static Set<String> loadedLibraries = Sets.newHashSet();

  public static void load(String libName) {
    if (!LibLoader.loadedLibraries.contains(libName)) {
      String libPath = explodeLib(libName);
      System.load(libPath);
      LibLoader.loadedLibraries.add(libName);
    }
  }

  private static String explodeLib(String libName) {
    InputStream src = Liner2.class.getClassLoader().getResourceAsStream(libName);
    OutputStream out = null;
    try {
      File target = File.createTempFile("lib", ".so");
      target.deleteOnExit();
      out = new FileOutputStream(target);

      byte[] buffer = new byte[1024];
      int len = src.read(buffer);
      while (len != -1) {
        out.write(buffer, 0, len);
        len = src.read(buffer);
      }

      return target.getAbsolutePath();
    } catch (IOException e) {
      throw new UnsatisfiedLinkError("IOException occured while exploding shared library: " + e.getMessage());
    } finally {
      try {
        src.close();
        if (out != null) {
          out.close();
        }
      } catch (IOException e) {
        throw new UnsatisfiedLinkError("IOException occured while exploding shared library: " + e.getMessage());
      }
    }
  }

}
