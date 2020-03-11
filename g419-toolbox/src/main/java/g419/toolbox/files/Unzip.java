package g419.toolbox.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzip {

  public static void unzip(final String zipFile, final String outputFolder) {
    final byte[] buffer = new byte[1024];
    try {
      final File folder = new File(outputFolder);
      if (!folder.exists()) {
        folder.mkdir();
      }

      final ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
      ZipEntry ze = zis.getNextEntry();
      while (ze != null) {
        if (ze.isDirectory()) {
          ze = zis.getNextEntry();
          continue;
        }
        final String fileName = ze.getName();
        final File newFile = new File(outputFolder + File.separator + fileName);
        newFile.getParentFile().mkdirs();
        final FileOutputStream fos = new FileOutputStream(newFile);
        int len;
        while ((len = zis.read(buffer)) > 0) {
          fos.write(buffer, 0, len);
        }
        fos.close();
        ze = zis.getNextEntry();
      }
      zis.closeEntry();
      zis.close();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

}
