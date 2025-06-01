package vernando.blueprints;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

public class BlueprintsManager {

    public static BlueprintsManager instance;
    public static BlueprintsManager getInstance() {
        if (instance == null) {
            instance = new BlueprintsManager();
        }
        return instance;
    }

    public ArrayList<Blueprint> blueprints;
    
    // Comprehensive list of image file extensions
    private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
        // Common formats
        "jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif", "webp",
        // RAW formats
        "cr2", "nef", "arw", "dng", "orf", "rw2", "pef", "srw", "raf", "3fr", "fff", "dcr", "kdc", "srf", "mrw", "raw",
        // Adobe formats
        "psd", "psb", "ai", "eps",
        // Vector formats that can be rasterized
        "svg", "svgz",
        // Other formats
        "ico", "cur", "pcx", "ppm", "pbm", "pgm", "pnm", "xbm", "xpm",
        // Windows formats
        "emf", "wmf", "dib",
        // Apple formats
        "icns", "pict", "pct",
        // Scientific/Medical formats
        "dcm", "dicom", "fits", "fts",
        // Animation formats
        "apng", "mng",
        // HDR formats
        "hdr", "exr", "pfm", "rgbe",
        // Compressed formats
        "jp2", "jpx", "j2k", "j2c", "jpc",
        // Legacy formats
        "iff", "lbm", "cut", "dds", "ftx", "g3", "hdf", "img", "jbig", "jng", "koala", "msp", "p7", "ras", "sun", "sgi", "targa", "tga", "wbmp", "xif"
    ));
    
    private static Set<String> dynamicallySupportedFormats = null;
    
    /**
     * Get all image formats supported by the current Java ImageIO installation
     */
    private static Set<String> getDynamicallySupportedFormats() {
        if (dynamicallySupportedFormats == null) {
            dynamicallySupportedFormats = new HashSet<>();
            String[] readerFormats = ImageIO.getReaderFormatNames();
            for (String format : readerFormats) {
                dynamicallySupportedFormats.add(format.toLowerCase());
            }
            Main.LOGGER.info("Dynamically detected image formats: " + String.join(", ", dynamicallySupportedFormats));
        }
        return dynamicallySupportedFormats;
    }
    
    /**
     * Check if a file is a supported image format
     */
    public static boolean isImageFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        String lowerFilename = filename.toLowerCase();
        int lastDotIndex = lowerFilename.lastIndexOf('.');
        
        if (lastDotIndex == -1 || lastDotIndex == lowerFilename.length() - 1) {
            return false; // No extension or ends with dot
        }
        
        String extension = lowerFilename.substring(lastDotIndex + 1);
        
        // Check against our comprehensive static list
        if (SUPPORTED_IMAGE_EXTENSIONS.contains(extension)) {
            return true;
        }
        
        // Check against dynamically detected formats
        Set<String> dynamicFormats = getDynamicallySupportedFormats();
        if (dynamicFormats.contains(extension)) {
            return true;
        }
        
        // Special handling for formats with multiple extensions
        switch (extension) {
            case "jpe":
            case "jfif":
                return true; // JPEG variants
            case "tiff":
                return true; // TIFF variant
            case "svg":
                return true; // SVG (might need special handling)
            default:
                return false;
        }
    }

    public ArrayList<Blueprint> ScanFileSystemForImages() {
        String fullPath = Util.GetPerWorldDimensionConfigPath();
        Main.LOGGER.info("Scanning for images in " + fullPath);
        blueprints = new ArrayList<Blueprint>();
        File folder = new File(fullPath);
        File[] listOfFiles = folder.listFiles();
        
        if (listOfFiles == null) {
            Main.LOGGER.warn("Could not list files in directory: " + fullPath);
            return blueprints;
        }
        
        int supportedFiles = 0;
        int totalFiles = 0;
        
        for (File file : listOfFiles) {
            if (file.isFile()) {
                totalFiles++;
                String filename = file.getName();
                
                if (isImageFile(filename)) {
                    try {
                        Blueprint blueprint = new Blueprint(fullPath + "/" + filename);
                        blueprints.add(blueprint);
                        supportedFiles++;
                        Main.LOGGER.debug("Added image file: " + filename);
                    } catch (Exception e) {
                        Main.LOGGER.error("Failed to load image file: " + filename + " - " + e.getMessage());
                    }
                }
            }
        }
        
        Main.LOGGER.info("Loaded " + supportedFiles + " image files out of " + totalFiles + " total files in " + fullPath);
        return blueprints;
    }
}
