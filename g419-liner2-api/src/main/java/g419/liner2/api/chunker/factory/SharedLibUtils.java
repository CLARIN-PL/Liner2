package g419.liner2.api.chunker.factory;

import g419.liner2.api.Liner2;

import java.io.*;

public class SharedLibUtils {
    static private String crfppPath = null;

    public static String getCrfppLibPath(){
        if (crfppPath == null)
            crfppPath = explodeCrfpp();
        return crfppPath;
    }

    private static String explodeCrfpp(){
        InputStream src = Liner2.class.getClassLoader().getResourceAsStream("libCRFPP.so");
        OutputStream out = null;
        try {
            File target = File.createTempFile("libCRFPP", ".so");
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
            throw new UnsatisfiedLinkError("IOException occured while exploding shared library: "+e.getMessage());
        } finally {
            //todo: exception handling should be safer! ~Filip
            try {
                src.close();
                if (out!=null)
                    out.close();
            } catch (IOException e) {
                throw new UnsatisfiedLinkError("IOException occured while exploding shared library: "+e.getMessage());
            }
        }
    }
}
