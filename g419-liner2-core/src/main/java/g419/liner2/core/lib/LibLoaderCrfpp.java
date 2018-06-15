package g419.liner2.core.lib;

/**
 * Class for dynamic loading crfpp module. Prevent from loading the module multiple times.
 */
public class LibLoaderCrfpp extends LibLoader{

//    private static boolean loaded = false;
//
//    /**
//     * Checks if crfpp module is loaded.
//     * @return
//     */
//    public static boolean isLoaded(){
//        return LibLoaderCrfpp.loaded;
//    }
//
//    /**
//     * Loads crfpp module from given path if not loaded.
//     * @param path
//     */
//    public static void load(String path){
//        if ( !LibLoaderCrfpp.isLoaded() ) {
//            try {
//                System.load(path);
//                LibLoaderCrfpp.loaded = true;
//            } catch (UnsatisfiedLinkError e) {
//                Logger.getLogger(LibLoaderCrfpp.class).error("Cannot load the libCRFPP.so native code.\nIf you are using liner as an imported jar specify correct path as CRFlib parameter in config.\n", e);
//                throw e;
//            }
//        }
//    }

    /**
     * Loads default crfpp module if not loaded.
     */
    public static void load(){
        LibLoader.load("libCRFPP.so");
    }

}
