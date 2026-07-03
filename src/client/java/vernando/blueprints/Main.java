package vernando.blueprints;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main implements ModInitializer {
	public static final String MOD_ID = "blueprints";
	public static final String MOD_NAME = "Blueprints";
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

		Settings.loadSettings();

		// Render-visible mode: depth-tested, renders only where not occluded by blocks.
		LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(context -> renderBlueprints(false));

		// Render-all mode: no depth testing, renders through blocks.
		LevelRenderEvents.END_MAIN.register(context -> renderBlueprints(true));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null || client.options == null) {
				return;
			}

			BlueprintsManager blueprintManager = BlueprintsManager.getInstance();

			String world = Util.getWorldOrServerName();
			String dimension = Util.getDimensionName();
			if (!world.equals(currentWorld) || !dimension.equals(currentDimension)) {
				LOGGER.info("World changed to {} dimension {}", world, dimension);
				currentWorld = world;
				currentDimension = dimension;
				blueprintManager.scanFileSystemForImages();
			}

			if (keyLaunchConfig.consumeClick()) {
				client.setScreen(new BlueprintsConfigScreen(blueprintManager.getBlueprints()));
			}

			if (keyPush.consumeClick()) {
				BlueprintsHud.getInstance().push();
			}

			if (keyPull.consumeClick()) {
				BlueprintsHud.getInstance().pull();
			}

			boolean isHoldingItemFrame = client.player.getMainHandItem().is(Items.ITEM_FRAME)
					|| client.player.getOffhandItem().is(Items.ITEM_FRAME);

			if (isHoldingItemFrame) {
				if (!visible) {
					visible = true;
					BlueprintsHud.getInstance().enable();
				}
			} else {
				if (visible) {
					visible = false;
					BlueprintsHud.getInstance().disable();
				}
			}
		});
	}

	/**
	 * Render all blueprints back-to-front (translucency needs far-to-near draw order).
	 * Each render event calls this with its own mode; only the mode matching the
	 * current setting actually draws.
	 */
	private void renderBlueprints(boolean renderThroughBlocks) {
		if (!isVisible() || Settings.getRenderThroughBlocks() != renderThroughBlocks) {
			return;
		}

		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

		List<Blueprint> sorted = new ArrayList<>(BlueprintsManager.getInstance().getBlueprints());
		sorted.sort((a, b) -> Double.compare(
			b.getDistanceFromCamera(camera),
			a.getDistanceFromCamera(camera)));
		sorted.forEach(blueprint -> blueprint.renderWorld(camera, renderThroughBlocks));
	}

	public static boolean isVisible() {
		return visible;
	}
}
