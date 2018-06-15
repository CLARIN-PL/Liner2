package g419.liner2.core.lib;

public class LibLoaderPolem extends LibLoader {

//    private static boolean loaded = false;
//
//    /**
//     * Checks if crfpp module is loaded.
//     * @return
//     */
//    public static boolean isLoaded(){
//        return LibLoaderPolem.loaded;
//    }
//
//    /**
//     * Loads crfpp module from given path if not loaded.
//     * @param path
//     */
//    public static void load(String path){
//        if ( !LibLoaderPolem.isLoaded() ) {
//            try {
//                System.load(path);
//                LibLoaderPolem.loaded = true;
//            } catch (UnsatisfiedLinkError e) {
//                Logger.getLogger(LibLoaderPolem.class).error("Cannot load the libCRFPP.so native code.\nIf you are using liner as an imported jar specify correct path as CRFlib parameter in config.\n", e);
//                throw e;
//            }
//        }
//    }

    /**
     * Loads default crfpp module if not loaded.
     */
    public static void load() {
        LibLoader.load("libPolemJava.so");
    }

}
