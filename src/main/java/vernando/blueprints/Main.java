package vernando.blueprints;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

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
		keyLaunchConfig = KeyBindingHelper.registerKeyBinding(new KeyBinding("Launch Config", GLFW.GLFW_KEY_O, MOD_NAME));
		keyPull = KeyBindingHelper.registerKeyBinding(new KeyBinding("Pull", GLFW.GLFW_KEY_EQUAL, MOD_NAME));
		keyPush = KeyBindingHelper.registerKeyBinding(new KeyBinding("Push", GLFW.GLFW_KEY_MINUS, MOD_NAME));

		Settings.LoadSettings();
		
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
				MinecraftClient.getInstance().setScreen(new BlueprintsConfigScreen(this, blueprintManager.blueprints));
			}

			if (keyPush.wasPressed()) {
				BlueprintsHud.getInstance().push(KeyBindingHelper);
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

		WorldRenderEvents.END.register(context -> {
			if (visible) {
				BlueprintsManager blueprintManager = BlueprintsManager.getInstance();
				for (Blueprint blueprint : blueprintManager.blueprints) {
					blueprint.render(context, Settings.getRenderThroughBlocks(), true);
				}
				
				BlueprintsHud.getInstance().render(context);
			}
		});
	}	
}