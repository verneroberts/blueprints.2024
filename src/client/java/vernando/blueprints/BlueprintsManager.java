package vernando.blueprints;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;

public class BlueprintsManager {

    private static BlueprintsManager instance;
    public static BlueprintsManager getInstance() {
        if (instance == null) {
            instance = new BlueprintsManager();
        }
        return instance;
    }

    private List<Blueprint> blueprints = new ArrayList<>();

    public List<Blueprint> getBlueprints() {
        return blueprints;
    }

    private static Set<String> dynamicallySupportedFormats = null;

    /**
     * Get all image formats supported by the current Java ImageIO installation
     */
    private static Set<String> getDynamicallySupportedFormats() {
        if (dynamicallySupportedFormats == null) {
            dynamicallySupportedFormats = new HashSet<>();
            for (String format : ImageIO.getReaderFormatNames()) {
                dynamicallySupportedFormats.add(format.toLowerCase());
            }
            Main.LOGGER.info("Dynamically detected image formats: {}", String.join(", ", dynamicallySupportedFormats));
        }
        return dynamicallySupportedFormats;
    }

    /**
     * Check if a file is a supported image format
     */
    public static boolean isImageFile(String filename) {
        // ValidationUtils.isImageFile already checks static formats first,
        // then falls back to dynamic formats if provided
        return ValidationUtils.isImageFile(filename, getDynamicallySupportedFormats());
    }

    public List<Blueprint> scanFileSystemForImages() {
        String fullPath = Util.getPerWorldDimensionConfigPath();
        Main.LOGGER.info("Scanning for images in {}", fullPath);

        // Release GPU textures held by the previous scan before dropping it.
        blueprints.forEach(Blueprint::close);
        blueprints = new ArrayList<>();

        File folder = new File(fullPath);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null) {
            Main.LOGGER.warn("Could not list files in directory: {}", fullPath);
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
                        blueprints.add(new Blueprint(fullPath + "/" + filename));
                        supportedFiles++;
                        Main.LOGGER.debug("Added image file: {}", filename);
                    } catch (Exception e) {
                        Main.LOGGER.error("Failed to load image file: {}", filename, e);
                    }
                }
            }
        }

        Main.LOGGER.info("Loaded {} image files out of {} total files in {}", supportedFiles, totalFiles, fullPath);
        return blueprints;
    }
}
