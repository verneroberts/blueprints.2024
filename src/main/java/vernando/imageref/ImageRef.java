package vernando.imageref;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.systems.RenderSystem;

public class ImageRef implements ModInitializer {	
	public static final String MOD_ID = "image-ref";
	public static final String MOD_NAME = "Vernando's Image Ref";
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

	private void ScanFileSystemForImages() {
		LOGGER.info("Scanning for images in config/" + MOD_ID);
		referenceImages = new ArrayList<ReferenceImage>();
		File folder = new File("config/" + MOD_ID);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String filename = file.getName();
				if (filename.endsWith(".jpg") || filename.endsWith(".png")) {					
					ReferenceImage referenceImage = new ReferenceImage();
					referenceImage.LoadConfig();					
					//generate id from timestamp
					String id = Long.toString(System.currentTimeMillis());
					referenceImage.registerTexture(MinecraftClient.getInstance(), "config/" + MOD_ID + "/" + filename, id);
					referenceImages.add(referenceImage);
				}
			}
		}
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

			if (referenceImages == null) {
				ScanFileSystemForImages();
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
			Direction direction = getDirection(yaw);			
			
			float multiplier = keyNudgeMultiply.isPressed() ? 9.9f : 0f;
			while (keyNudgeDown.wasPressed()) {
				activeReferenceImage.positionY -= 0.1 + multiplier;
			}
			while (keyNudgeUp.wasPressed()) {
				activeReferenceImage.positionY += 0.1 + multiplier;
			}
			while (keyNudgeLeft.wasPressed()) {
				if (direction == Direction.NORTH) {
					activeReferenceImage.positionX -= 0.1 + multiplier;
				} else if (direction == Direction.EAST) {
					activeReferenceImage.positionZ -= 0.1 + multiplier;
				} else if (direction == Direction.SOUTH) {
					activeReferenceImage.positionX += 0.1 + multiplier;
				} else if (direction == Direction.WEST) {
					activeReferenceImage.positionZ += 0.1 + multiplier;
				}		
			}
			while (keyNudgeRight.wasPressed()) {
				if (direction == Direction.NORTH) {
					activeReferenceImage.positionX += 0.1 + multiplier;
				} else if (direction == Direction.EAST) {
					activeReferenceImage.positionZ += 0.1 + multiplier;
				} else if (direction == Direction.SOUTH) {
					activeReferenceImage.positionX -= 0.1 + multiplier;
				} else if (direction == Direction.WEST) {
					activeReferenceImage.positionZ -= 0.1 + multiplier;
				}
			}
			while (keyScaleXUp.wasPressed()) {
				activeReferenceImage.scaleX -= 0.1 + multiplier;				
			}
			while (keyScaleXDown.wasPressed()) {
				activeReferenceImage.scaleX += 0.1 + multiplier;
			}
			while (keyScaleYUp.wasPressed()) {
				activeReferenceImage.scaleY -= 0.1 + multiplier;
			}
			while (keyScaleYDown.wasPressed()) {
				activeReferenceImage.scaleY += 0.1 + multiplier;
			}

			while (keyRenderThroughBlocks.wasPressed()) {
				Config.renderThroughBlocks = !Config.renderThroughBlocks;
				client.player.sendMessage(Text.of("[" + MOD_NAME + "] Render Through Blocks: " + Config.renderThroughBlocks), false);
			}
			while (keySetPositionToPlayer.wasPressed()) {
				//get client position
				activeReferenceImage.positionX = (float) client.player.getX();
				activeReferenceImage.positionY = (float) client.player.getY();
				activeReferenceImage.positionZ = (float) client.player.getZ();				
				client.player.sendMessage(Text.of("[" + MOD_NAME + "] Changed position to current player position: " + Config.positionX + ", " + Config.positionY + ", " + Config.positionZ), false);
			}			
			while (keyCycleNextImage.wasPressed()) {
				int index = referenceImages.indexOf(activeReferenceImage);
				index++;
				if (index >= referenceImages.size()) {
					index = 0;
				}
				activeReferenceImage = referenceImages.get(index);			
				thumbnailDisplayTimer = 20f;	
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
				LOGGER.info("timer: " + thumbnailDisplayTimer);

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
		NORTH, EAST, SOUTH, WEST
	}

	private Direction getDirection(float yaw) {		
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
}