package vernando.imageref;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction.Axis;

import java.io.File;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageRef implements ModInitializer {	
	public static final String MOD_ID = "image-ref";
	public static final String MOD_NAME = "Image Ref";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);	
	
	private ReferenceImage activeReferenceImage;
	private ArrayList<ReferenceImage> referenceImages;
	
	private static boolean visible = false;

	private float thumbnailDisplayTimer = 20f;

	private static KeyBinding keyNudgeLeft;
	private static KeyBinding keyNudgeRight;
	private static KeyBinding keyNudgeUp;
	private static KeyBinding keyNudgeDown;
	private static KeyBinding keyScaleXUp;
	private static KeyBinding keyScaleXDown;
	private static KeyBinding keyScaleYUp;
	private static KeyBinding keyScaleYDown;
	private static KeyBinding keyNudgeMultiply;
	private static KeyBinding keyRenderThroughBlocks;
	private static KeyBinding keySetPositionToPlayer;
	private static KeyBinding keyCycleNextImage;
	private static KeyBinding keyCycleOrientation;
	private static KeyBinding keyToggleAlpha;

	private String currentWorld = "";
	private String currentDimension = "";

	private void ScanFileSystemForImages(String worldString, String dimension) {
		worldString = worldString.replace(":", "_").trim();
		String fullPath = "config/" + MOD_ID + "/" + worldString + "/" + dimension;

		LOGGER.info("Scanning for images in " + fullPath);
		referenceImages = new ArrayList<ReferenceImage>();
		File folder = new File(fullPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String filename = file.getName();
				if (filename.endsWith(".jpg") || filename.endsWith(".png")) {					
					ReferenceImage referenceImage = new ReferenceImage(fullPath + "/" + filename);
					referenceImages.add(referenceImage);
				}
			}
		}
		activeReferenceImage = referenceImages.size() > 0 ? referenceImages.get(0) : null;
	}

	@Override
	public void onInitialize() {
		Config.init(MOD_ID, Config.class);

		keyNudgeDown = KeyBindingHelper.registerKeyBinding(new KeyBinding("Nudge Down", GLFW.GLFW_KEY_KP_2, "Image Ref"));
		keyNudgeLeft = KeyBindingHelper.registerKeyBinding(new KeyBinding("Nudge Left", GLFW.GLFW_KEY_KP_4, "Image Ref"));
		keyNudgeRight = KeyBindingHelper.registerKeyBinding(new KeyBinding("Nudge Right", GLFW.GLFW_KEY_KP_6, "Image Ref"));
		keyNudgeUp = KeyBindingHelper.registerKeyBinding(new KeyBinding("Nudge Up", GLFW.GLFW_KEY_KP_8, "Image Ref"));
		keyScaleXUp = KeyBindingHelper.registerKeyBinding(new KeyBinding("Scale X Up", GLFW.GLFW_KEY_KP_7, "Image Ref"));
		keyScaleXDown = KeyBindingHelper.registerKeyBinding(new KeyBinding("Scale X Down", GLFW.GLFW_KEY_KP_9, "Image Ref"));
		keyScaleYUp = KeyBindingHelper.registerKeyBinding(new KeyBinding("Scale Y up", GLFW.GLFW_KEY_KP_1, "Image Ref"));
		keyScaleYDown = KeyBindingHelper.registerKeyBinding(new KeyBinding("Scale Y down", GLFW.GLFW_KEY_KP_3, "Image Ref"));
		keyNudgeMultiply = KeyBindingHelper.registerKeyBinding(new KeyBinding("Nudge Embiggen Modifier", GLFW.GLFW_KEY_KP_5, "Image Ref"));
		keyRenderThroughBlocks = KeyBindingHelper.registerKeyBinding(new KeyBinding("Render Through Blocks", GLFW.GLFW_KEY_KP_0, "Image Ref"));
		keySetPositionToPlayer = KeyBindingHelper.registerKeyBinding(new KeyBinding("Set Position To Player", GLFW.GLFW_KEY_KP_DECIMAL, "Image Ref"));		
		keyCycleNextImage = KeyBindingHelper.registerKeyBinding(new KeyBinding("Cycle Next Image", GLFW.GLFW_KEY_KP_MULTIPLY, "Image Ref"));
		keyCycleOrientation = KeyBindingHelper.registerKeyBinding(new KeyBinding("Cycle Orientation", GLFW.GLFW_KEY_KP_DIVIDE, "Image Ref"));
		keyToggleAlpha = KeyBindingHelper.registerKeyBinding(new KeyBinding("Toggle Alpha", GLFW.GLFW_KEY_KP_SUBTRACT, "Image Ref"));		
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}
			if (client.world == null) {
				return;
			}
			if (client.options == null) {
				return;
			}
			
			String world = getWorldOrServerName();
			String dimension = client.world.getDimensionKey().getValue().toString().split(":")[1];
			if (!world.equals(currentWorld) || !dimension.equals(currentDimension)) {
				LOGGER.info("World changed to " + world + " dimension " + dimension);
				currentWorld = world;
				currentDimension = dimension;
				ScanFileSystemForImages(world, dimension);
			}

			if (referenceImages == null) {
				return;
			}

			if (activeReferenceImage == null && referenceImages.size() > 0) {
				activeReferenceImage = referenceImages.get(0);
				thumbnailDisplayTimer = 1000f;
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

			// work out the direction the player is facing and use that as the reference when tweaking the position
			float yaw = client.getCameraEntity().getYaw();
			yaw = fixYaw(yaw);
			float pitch = client.getCameraEntity().getPitch();
			Direction directionFacing = getDirection(yaw, pitch);

			Boolean multiply = keyNudgeMultiply.isPressed();
			while (keyNudgeDown.wasPressed()) {				
				activeReferenceImage.NudgePosition(Direction.DOWN, multiply);
			}
			while (keyNudgeUp.wasPressed()) {
				activeReferenceImage.NudgePosition(Direction.UP, multiply);
			}
			while (keyNudgeLeft.wasPressed()) {
				switch (directionFacing) {
					case NORTH:
						activeReferenceImage.NudgePosition(Direction.WEST, multiply);
						break;
					case EAST:
						activeReferenceImage.NudgePosition(Direction.NORTH, multiply);
						break;
					case SOUTH:
						activeReferenceImage.NudgePosition(Direction.EAST, multiply);
						break;
					case WEST:
						activeReferenceImage.NudgePosition(Direction.SOUTH, multiply);
						break;
					default:
						break;
				}
			}
			while (keyNudgeRight.wasPressed()) {
				switch (directionFacing) {
					case NORTH:
						activeReferenceImage.NudgePosition(Direction.EAST, multiply);
						break;
					case EAST:
						activeReferenceImage.NudgePosition(Direction.SOUTH, multiply);
						break;
					case SOUTH:
						activeReferenceImage.NudgePosition(Direction.WEST, multiply);
						break;
					case WEST:
						activeReferenceImage.NudgePosition(Direction.NORTH, multiply);
						break;
					default:
						break;
				}
			}
			while (keyScaleXUp.wasPressed()) {
				activeReferenceImage.NudgeScale(Axis.X, 1, multiply);
			}
			while (keyScaleXDown.wasPressed()) {
				activeReferenceImage.NudgeScale(Axis.X, -1, multiply);
			}
			while (keyScaleYUp.wasPressed()) {
				activeReferenceImage.NudgeScale(Axis.Y, 1, multiply);
			}
			while (keyScaleYDown.wasPressed()) {
				activeReferenceImage.NudgeScale(Axis.Y, -1, multiply);
			}

			while (keyRenderThroughBlocks.wasPressed()) {
				Config.renderThroughBlocks = !Config.renderThroughBlocks;
				client.player.sendMessage(Text.of("[" + MOD_NAME + "] Render Through Blocks: " + Config.renderThroughBlocks), false);
			}
			while (keySetPositionToPlayer.wasPressed()) {
				activeReferenceImage.SetPosition((float) client.player.getX(), (float) client.player.getY(), (float) client.player.getZ());
				client.player.sendMessage(Text.of("[" + MOD_NAME + "] Changed position to current player position: " + client.player.getX() + ", " + client.player.getY() + ", " + client.player.getZ()), false);
			}			
			while (keyCycleNextImage.wasPressed()) {
				int index = referenceImages.indexOf(activeReferenceImage);
				index++;
				if (index >= referenceImages.size()) {
					index = 0;
				}
				if (referenceImages.size() == 0) {
					activeReferenceImage = null;
					return;
				}
				activeReferenceImage = referenceImages.get(index);			
				thumbnailDisplayTimer = 20f;	
			}
			while (keyCycleOrientation.wasPressed()) {
				// rotate clockwise based on the direction the player is facing
				switch (directionFacing) {
					case NORTH:
						activeReferenceImage.NudgeRotation(Axis.Y, multiply);
						break;
					case EAST:
						activeReferenceImage.NudgeRotation(Axis.X, multiply);
						break;
					case SOUTH:
						activeReferenceImage.NudgeRotation(Axis.Y, multiply);
						break;
					case WEST:
						activeReferenceImage.NudgeRotation(Axis.X, multiply);
						break;
					case UP: 
						activeReferenceImage.NudgeRotation(Axis.Z, multiply);
						break;
					case DOWN:
						activeReferenceImage.NudgeRotation(Axis.Z, multiply);
						break;				
				}
			}

			while (keyToggleAlpha.wasPressed()) {
				activeReferenceImage.ToggleAlpha();
			}
		});
		

		WorldRenderEvents.END.register(context -> {
			Boolean renderThroughBlocks = Config.renderThroughBlocks;

			if (visible) {
				for (ReferenceImage referenceImage : referenceImages) {
					referenceImage.render(context, renderThroughBlocks);
				}
			}
		});

		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {

			if (!visible) {
				return;
			}

			if (activeReferenceImage != null && thumbnailDisplayTimer > 0) {
				thumbnailDisplayTimer -= tickDelta;
				activeReferenceImage.renderThumbnail(drawContext);
			}
		});
	}

	private float fixYaw(float yaw) {
		// no idea why, but yaw seems to just grow when the player spins in the same direction
		while (yaw < -180) {
			yaw += 360;
		}
		while (yaw > 180) {
			yaw -= 360;
		}
		return yaw;
	}

	enum Direction {
		NORTH, EAST, SOUTH, WEST, UP, DOWN
	}

	private Direction getDirection(float yaw, float pitch) {		
		if (pitch >= 45) {
			return Direction.UP;
		}
		if (pitch <= -45) {
			return Direction.DOWN;
		}
		if (yaw > -45 && yaw < 45) {
			return Direction.SOUTH;
		}
		if (yaw > 45 && yaw < 135) {
			return Direction.WEST;
		}
		if (yaw > 135 || yaw < -135) {
			return Direction.NORTH;
		}
		return Direction.EAST;
	}

	public String getWorldOrServerName() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.isInSingleplayer())
        {
			return client.getServer().getSaveProperties().getLevelName();
        }
        else
        {
			return client.getCurrentServerEntry().address;
        }

	}
}