package vernando.blueprints;

import java.util.Set;

/**
 * Validation utilities for file and input validation.
 * All methods are pure functions with no external dependencies.
 */
public class ValidationUtils {

    /**
     * Image file extensions that Java's built-in ImageIO can decode.
     * Formats supported by additional installed ImageIO plugins are handled
     * via the dynamic-format overload of {@link #isImageFile(String, Set)}.
     */
    private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = Set.of(
        "png", "jpg", "jpeg", "jpe", "jfif", "gif", "bmp", "wbmp", "tif", "tiff"
    );

    /**
     * Checks if a filename has a supported image file extension.
     *
     * @param filename The filename to check
     * @return true if the file has a supported image extension, false otherwise
     */
    public static boolean isImageFile(String filename) {
        String extension = getFileExtension(filename);
        return extension != null && SUPPORTED_IMAGE_EXTENSIONS.contains(extension);
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
        return SUPPORTED_IMAGE_EXTENSIONS;
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

        return SUPPORTED_IMAGE_EXTENSIONS.contains(extension.toLowerCase());
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
