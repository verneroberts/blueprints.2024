package vernando.blueprints;
import java.io.File;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.FileReader;

public class Settings  {	
	
	private static boolean renderThroughBlocks = false;
	private static int imagesPerRow = 5;

	public static void LoadSettings() {
		String configPath = Util.GetConfigPath();
		try {
			File file = new File(configPath + "/blueprints.json");
			if (file.exists()) {
				// use gson to load file
				Gson gson = new Gson();
				FileReader reader = new FileReader(file);
				JsonObject obj = gson.fromJson(reader, JsonObject.class);
				if (obj != null && obj.has("renderThroughBlocks")) renderThroughBlocks = obj.get("renderThroughBlocks").getAsBoolean();
				if (obj != null && obj.has("imagesPerRow")) imagesPerRow = obj.get("imagesPerRow").getAsInt();
			} else {
				renderThroughBlocks = false;
				SaveSettings();
			}
		}
		catch (Exception e) {
			Main.LOGGER.error("Failed to load config");
			Main.LOGGER.error(e.getMessage());
			return;
		}
	}

	public static void SaveSettings() {
		String configPath = Util.GetConfigPath();
		JsonObject obj = new JsonObject();
		obj.addProperty("renderThroughBlocks", renderThroughBlocks);
		obj.addProperty("imagesPerRow", imagesPerRow);
		String json = obj.toString();
		try {
			java.nio.file.Files.write(java.nio.file.Paths.get(configPath + "/blueprints.json"), json.getBytes());
		}  catch (Exception e) {
			Main.LOGGER.error("Failed to save config");
			Main.LOGGER.error(e.getMessage());
			return;
		}
	}

	public static boolean getRenderThroughBlocks() {
		return renderThroughBlocks;
	}

    public static void setRenderThroughBlocks(boolean b) {
		renderThroughBlocks = b;
		SaveSettings();
    }

    public static void setImagesPerRow(int x) {
        imagesPerRow = x;
		SaveSettings();
    }

    public static int getImagesPerRow() {
        return imagesPerRow;
    }
}