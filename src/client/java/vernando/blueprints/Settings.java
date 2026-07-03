package vernando.blueprints;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Settings {

	private static boolean renderThroughBlocks = false;
	private static int imagesPerRow = 5;

	public static void loadSettings() {
		String configPath = Util.getConfigPath();
		File file = new File(configPath + "/blueprints.json");
		if (!file.exists()) {
			renderThroughBlocks = false;
			saveSettings();
			return;
		}

		try (FileReader reader = new FileReader(file)) {
			JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
			if (obj != null && obj.has("renderThroughBlocks")) renderThroughBlocks = obj.get("renderThroughBlocks").getAsBoolean();
			if (obj != null && obj.has("imagesPerRow")) imagesPerRow = obj.get("imagesPerRow").getAsInt();
		} catch (Exception e) {
			Main.LOGGER.error("Failed to load settings", e);
		}
	}

	public static void saveSettings() {
		String configPath = Util.getConfigPath();
		JsonObject obj = new JsonObject();
		obj.addProperty("renderThroughBlocks", renderThroughBlocks);
		obj.addProperty("imagesPerRow", imagesPerRow);
		try (FileWriter writer = new FileWriter(configPath + "/blueprints.json")) {
			writer.write(obj.toString());
		} catch (Exception e) {
			Main.LOGGER.error("Failed to save settings", e);
		}
	}

	public static boolean getRenderThroughBlocks() {
		return renderThroughBlocks;
	}

	public static void setRenderThroughBlocks(boolean b) {
		renderThroughBlocks = b;
		saveSettings();
	}

	public static void setImagesPerRow(int x) {
		imagesPerRow = x;
		saveSettings();
	}

	public static int getImagesPerRow() {
		return imagesPerRow;
	}
}
