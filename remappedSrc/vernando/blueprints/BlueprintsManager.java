package vernando.blueprints;
import java.io.File;
import java.util.ArrayList;

public class BlueprintsManager {

    public static BlueprintsManager instance;
    public static BlueprintsManager getInstance() {
        if (instance == null) {
            instance = new BlueprintsManager();
        }
        return instance;
    }

    public ArrayList<Blueprint> blueprints;

    public ArrayList<Blueprint> ScanFileSystemForImages() {
		String fullPath = Util.GetPerWorldDimensionConfigPath();
		Main.LOGGER.info("Scanning for images in " + fullPath);
		blueprints = new ArrayList<Blueprint>();
		File folder = new File(fullPath);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String filename = file.getName();
				if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".tif")) {					
					Blueprint blueprint = new Blueprint(fullPath + "/" + filename);
					blueprints.add(blueprint);
				}
			}
		}
		return blueprints;
	}
}
