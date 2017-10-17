package g419.liner2.core.tools;

import g419.liner2.core.chunker.factory.SharedLibUtils;
import org.apache.log4j.Logger;

/**
 * Class for dynamic loading crfpp module. Prevent from loading the module multiple times.
 */
public class CrfppLoader {

    private static boolean loaded = false;

    /**
     * Checks if crfpp module is loaded.
     * @return
     */
    public static boolean isLoaded(){
        return CrfppLoader.loaded;
    }

    /**
     * Loads crfpp module from given path if not loaded.
     * @param path
     */
    public static void load(String path){
        if ( !CrfppLoader.isLoaded() ) {
            try {
                System.load(path);
                CrfppLoader.loaded = true;
            } catch (UnsatisfiedLinkError e) {
                Logger.getLogger(CrfppLoader.class).error("Cannot load the libCRFPP.so native code.\nIf you are using liner as an imported jar specify correct path as CRFlib parameter in config.\n", e);
                throw e;
            }
        }
    }

    /**
     * Loads default crfpp module if not loaded.
     */
    public static void load(){
        if ( !CrfppLoader.isLoaded() ) {
            CrfppLoader.load(SharedLibUtils.getCrfppLibPath());
        }
    }

}
