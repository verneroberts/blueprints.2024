package vernando.blueprints;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main implements ModInitializer {	
	public static final String MOD_ID = "blueprints";
	public static final String MOD_NAME = "Blueprints";
	public static final String TOOL_ITEM = "Item Frame";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);	
	
	private static boolean visible = false;
	private static KeyMapping keyLaunchConfig;
	private static KeyMapping keyPush;
	private static KeyMapping keyPull;
	
	private String currentWorld = "";
	private String currentDimension = "";	

	@Override
	public void onInitialize() {
		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "blueprints"));
		keyLaunchConfig = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.blueprints.launch_config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, category));
		keyPull = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.blueprints.pull", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, category));
		keyPush = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.blueprints.push", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, category));

		Settings.LoadSettings();

		// Render-visible mode: depth-tested, renders only where not occluded by blocks.
		LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(context -> {
			if (!isVisible() || Settings.getRenderThroughBlocks()) return;

			BlueprintsManager blueprintManager = BlueprintsManager.getInstance();
			if (blueprintManager.blueprints == null) return;

			var camera = Minecraft.getInstance().gameRenderer.getMainCamera();

			blueprintManager.blueprints.stream()
				.sorted((a, b) -> Double.compare(
					b.getDistanceFromCamera(camera),
					a.getDistanceFromCamera(camera)))
				.forEach(blueprint -> blueprint.renderWorld(camera, false));

			BlueprintsHud.getInstance().render(new PoseStack(), camera);
		});

		// Render-all mode: no depth testing, renders through blocks.
		LevelRenderEvents.END_MAIN.register(context -> {
			if (!isVisible() || !Settings.getRenderThroughBlocks()) return;

			BlueprintsManager blueprintManager = BlueprintsManager.getInstance();
			if (blueprintManager.blueprints == null) return;

			var camera = Minecraft.getInstance().gameRenderer.getMainCamera();

			blueprintManager.blueprints.stream()
				.sorted((a, b) -> Double.compare(
					b.getDistanceFromCamera(camera),
					a.getDistanceFromCamera(camera)))
				.forEach(blueprint -> blueprint.renderWorld(camera, true));

			BlueprintsHud.getInstance().render(new PoseStack(), camera);
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null || client.options == null) {
				return;
			}

			BlueprintsManager blueprintManager = BlueprintsManager.getInstance();
			
			String world = Util.getWorldOrServerName();
			String dimension = Util.getDimensionName();
			if (!world.equals(currentWorld) || !dimension.equals(currentDimension)) {
				LOGGER.info("World changed to " + world + " dimension " + dimension);
				currentWorld = world;
				currentDimension = dimension;
				blueprintManager.ScanFileSystemForImages();
			}

			if (blueprintManager.blueprints == null) {
				return;
			}

			if (keyLaunchConfig.consumeClick()) {
				client.setScreen(new BlueprintsConfigScreen(this, blueprintManager.blueprints));
			}

			if (keyPush.consumeClick()) {
				BlueprintsHud.getInstance().push();
			}

			if (keyPull.consumeClick()) {
				BlueprintsHud.getInstance().pull();
			}

			boolean isHoldingPainting = client.player.getMainHandItem().getHoverName().getString().equals(TOOL_ITEM) || client.player.getOffhandItem().getHoverName().getString().equals(TOOL_ITEM);

			if (isHoldingPainting) {
				if (visible == false) {
					BlueprintsHud.getInstance().enable();					
					visible = true;
				}
			} else {
				if (visible == true) {
					visible = false;
					BlueprintsHud.getInstance().disable();
				}				
				return;
			}

		});
	}

	public static boolean isVisible() {
		return visible;
	}
}