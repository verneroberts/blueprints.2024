package vernando.blueprints;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main implements ModInitializer {	
	public static final String MOD_ID = "blueprints";
	public static final String MOD_NAME = "Blueprints";
	public static final String TOOL_ITEM = "Item Frame";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);	
	
	private static boolean visible = false;
	private static KeyBinding keyLaunchConfig;
	private static KeyBinding keyPush;
	private static KeyBinding keyPull;
	
	private String currentWorld = "";
	private String currentDimension = "";	

	@Override
	public void onInitialize() {
		KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "blueprints"));
		keyLaunchConfig = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.blueprints.launch_config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, category));
		keyPull = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.blueprints.pull", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, category));
		keyPush = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.blueprints.push", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, category));

		Settings.LoadSettings();

		// Render-visible mode: inject at BEFORE_TRANSLUCENT where the world depth buffer is active.
		WorldRenderEvents.BEFORE_TRANSLUCENT.register(context -> {
			if (!isVisible() || Settings.getRenderThroughBlocks()) return;

			BlueprintsManager blueprintManager = BlueprintsManager.getInstance();
			if (blueprintManager.blueprints == null) return;

			var camera = MinecraftClient.getInstance().gameRenderer.getCamera();
			MatrixStack matrices = new MatrixStack();

			blueprintManager.blueprints.stream()
				.sorted((a, b) -> Double.compare(
					b.getDistanceFromCamera(camera),
					a.getDistanceFromCamera(camera)))
				.forEach(blueprint -> blueprint.render(matrices, camera, false, true));

			BlueprintsHud.getInstance().render(matrices, camera);
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null || client.options == null) {
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

			if (keyLaunchConfig.wasPressed()) {
				client.setScreen(new BlueprintsConfigScreen(this, blueprintManager.blueprints));
			}

			if (keyPush.wasPressed()) {
				BlueprintsHud.getInstance().push();
			}

			if (keyPull.wasPressed()) {
				BlueprintsHud.getInstance().pull();
			}

			boolean isHoldingPainting = client.player.getMainHandStack().getName().getString().equals(TOOL_ITEM) || client.player.getOffHandStack().getName().getString().equals(TOOL_ITEM);

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