package vernando.imageref;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import java.io.File;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {	
	public static final String MOD_ID = "image-ref";
	public static final String MOD_NAME = "Blueprints";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);	
	
	private ArrayList<Blueprint> blueprints;	
	private static boolean visible = false;
	private static KeyBinding keyLaunchConfig;

	private String currentWorld = "";
	private String currentDimension = "";

	private ArrayList<Blueprint> ScanFileSystemForImages(String worldString, String dimension) {
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
		Config.init(MOD_ID, Config.class);

		keyLaunchConfig = KeyBindingHelper.registerKeyBinding(new KeyBinding("Launch Config", GLFW.GLFW_KEY_O, "Image Ref"));
		
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
				ScanFileSystemForImages(world, dimension);
			}

			if (blueprints == null) {
				return;
			}

			if (keyLaunchConfig.wasPressed()) {
				MinecraftClient.getInstance().setScreen(new MainConfigScreen(blueprints));
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
			Boolean renderThroughBlocks = Config.renderThroughBlocks;

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
}