package vernando.blueprints;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
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
		keyLaunchConfig = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.blueprints.launch_config", InputConstants.Type.KEYBOARD, InputConstants.KEY_O, category));
		keyPull = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.blueprints.pull", InputConstants.Type.KEYBOARD, InputConstants.KEY_MINUS, category));
		keyPush = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.blueprints.push", InputConstants.Type.KEYBOARD, InputConstants.KEY_EQUALS, category));

		Settings.loadSettings();

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
				client.gui.setScreen(new BlueprintsConfigScreen(blueprintManager.getBlueprints()));
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
	 * Called by LevelRendererMixin after the level frame graph has fully executed
	 * (terrain, clouds, weather), so the depth buffer is complete: render-visible
	 * mode depth-tests against everything including clouds, and render-all mode
	 * draws on top of everything.
	 */
	public static void renderAfterLevel() {
		if (!isVisible()) {
			return;
		}

		boolean renderThroughBlocks = Settings.getRenderThroughBlocks();
		Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();

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
