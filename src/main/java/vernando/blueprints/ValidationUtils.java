package vernando.blueprints;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validation utilities for file and input validation.
 * All methods are pure functions with no external dependencies.
 */
public class ValidationUtils {

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

    // Additional variants not in the main list
    private static final Set<String> ADDITIONAL_VARIANTS = new HashSet<>(Arrays.asList(
        "jpe", "jfif" // JPEG variants
    ));

    /**
     * Checks if a filename has a supported image file extension.
     *
     * @param filename The filename to check
     * @return true if the file has a supported image extension, false otherwise
     */
    public static boolean isImageFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        String extension = getFileExtension(filename);
        if (extension == null || extension.isEmpty()) {
            return false;
        }

        return SUPPORTED_IMAGE_EXTENSIONS.contains(extension) || ADDITIONAL_VARIANTS.contains(extension);
    }

    /**
     * Checks if a filename has a supported image extension, with dynamic format detection.
     * This version allows passing in dynamically detected formats from ImageIO.
     *
     * @param filename The filename to check
     * @param dynamicFormats Additional formats to check (e.g., from ImageIO.getReaderFormatNames())
     * @return true if the file has a supported image extension, false otherwise
     */
    public static boolean isImageFile(String filename, Set<String> dynamicFormats) {
        if (isImageFile(filename)) {
            return true;
        }

        if (dynamicFormats != null && !dynamicFormats.isEmpty()) {
            String extension = getFileExtension(filename);
            return extension != null && dynamicFormats.contains(extension);
        }

        return false;
    }

    /**
     * Extracts the file extension from a filename.
     *
     * @param filename The filename
     * @return The file extension in lowercase, or null if no extension exists
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }

        String lowerFilename = filename.toLowerCase();
        int lastDotIndex = lowerFilename.lastIndexOf('.');

        // No extension or ends with dot
        if (lastDotIndex == -1 || lastDotIndex == lowerFilename.length() - 1) {
            return null;
        }

        return lowerFilename.substring(lastDotIndex + 1);
    }

    /**
     * Gets the set of all supported image extensions.
     *
     * @return An unmodifiable set of supported extensions
     */
    public static Set<String> getSupportedExtensions() {
        Set<String> allExtensions = new HashSet<>(SUPPORTED_IMAGE_EXTENSIONS);
        allExtensions.addAll(ADDITIONAL_VARIANTS);
        return allExtensions;
    }

    /**
     * Checks if a given extension is supported.
     *
     * @param extension The extension to check (case-insensitive)
     * @return true if the extension is supported, false otherwise
     */
    public static boolean isSupportedExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }

        String lowerExtension = extension.toLowerCase();
        return SUPPORTED_IMAGE_EXTENSIONS.contains(lowerExtension) || ADDITIONAL_VARIANTS.contains(lowerExtension);
    }

    /**
     * Validates that a string is a valid float number.
     *
     * @param value The string to validate
     * @return true if the string can be parsed as a float, false otherwise
     */
    public static boolean isValidFloat(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates that a string is a valid integer number.
     *
     * @param value The string to validate
     * @return true if the string can be parsed as an integer, false otherwise
     */
    public static boolean isValidInteger(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
