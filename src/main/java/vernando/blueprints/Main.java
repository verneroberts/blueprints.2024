package vernando.blueprints;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import java.io.FileReader;
import java.io.IOException;

public class Main implements ModInitializer {	
	public static final String MOD_ID = "blueprints";
	public static final String MOD_NAME = "Blueprints";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);	
	
	private ArrayList<Blueprint> blueprints;	
	private static boolean visible = false;
	private static KeyBinding keyLaunchConfig;

	private String currentWorld = "";
	private String currentDimension = "";
	private boolean renderThroughBlocks;

	public ArrayList<Blueprint> ScanFileSystemForImages() {
		String fullPath = Util.GetPerWorldDimensionConfigPath();
		LOGGER.info("Scanning for images in " + fullPath);
		blueprints = new ArrayList<Blueprint>();
		File folder = new File(fullPath);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String filename = file.getName();
				if (filename.endsWith(".jpg") || filename.endsWith(".png")) {					
					Blueprint blueprint = new Blueprint(fullPath + "/" + filename);
					blueprints.add(blueprint);
				}
			}
		}
		return blueprints;
	}

	@Override
	public void onInitialize() {
		keyLaunchConfig = KeyBindingHelper.registerKeyBinding(new KeyBinding("Launch Config", GLFW.GLFW_KEY_O, "Image Ref"));

		LoadSettings();
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null || client.options == null) {
				return;
			}
			
			String world = Util.getWorldOrServerName();
			String dimension = Util.getDimensionName();
			if (!world.equals(currentWorld) || !dimension.equals(currentDimension)) {
				LOGGER.info("World changed to " + world + " dimension " + dimension);
				currentWorld = world;
				currentDimension = dimension;
				ScanFileSystemForImages();
			}

			if (blueprints == null) {
				return;
			}

			if (keyLaunchConfig.wasPressed()) {
				MinecraftClient.getInstance().setScreen(new MainConfigScreen(blueprints, this));
			}

			boolean isHoldingPainting = client.player.getMainHandStack().getName().getString().equals("Painting") || client.player.getOffHandStack().getName().getString().equals("Painting");

			if (isHoldingPainting) {
				if (visible == false) {
					visible = true;
				}
			} else {
				if (visible == true) {
					visible = false;
				}				
				return;
			}

		});			

		WorldRenderEvents.END.register(context -> {
			if (visible) {
				for (Blueprint blueprint : blueprints) {
					blueprint.render(context, renderThroughBlocks);
				}
			}
		});

		// HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
		// 	if (!visible) {
		// 		return;
		// 	}
		// });
	}

	private void LoadSettings() {
		String configPath = Util.GetConfigPath();
		try {
			File file = new File(configPath + "/blueprints.json");
			if (file.exists()) {
				// use gson to load file
				Gson gson = new Gson();
				FileReader reader = new FileReader(file);
				JsonObject obj = gson.fromJson(reader, JsonObject.class);
				if (obj != null && obj.has("renderThroughBlocks")) renderThroughBlocks = obj.get("renderThroughBlocks").getAsBoolean();
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

	public void SaveSettings() {
		String configPath = Util.GetConfigPath();
		JsonObject obj = new JsonObject();
		obj.addProperty("renderThroughBlocks", renderThroughBlocks);
		String json = obj.toString();
		try {
			java.nio.file.Files.write(java.nio.file.Paths.get(configPath + "/blueprints.json"), json.getBytes());
		}  catch (Exception e) {
			Main.LOGGER.error("Failed to save config");
			Main.LOGGER.error(e.getMessage());
			return;
		}
	}

	public boolean getRenderThroughBlocks() {
		return renderThroughBlocks;
	}

    public void setRenderThroughBlocks(boolean b) {
		renderThroughBlocks = b;
    }
}