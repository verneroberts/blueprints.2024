package vernando.imageref;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.systems.RenderSystem;

public class ImageRef implements ModInitializer {
	private static final String MOD_ID = "image-ref";
	private static final String MOD_NAME = "Vernando's Image Ref";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);	
	
	private static boolean visible = false;

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

			boolean isHoldingPainting = client.player.getMainHandStack().getName().getString().equals("Painting");

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
				Config.positionY -= 0.1 + multiplier;
			}
			while (keyNudgeUp.wasPressed()) {
				Config.positionY += 0.1 + multiplier;
			}
			while (keyNudgeLeft.wasPressed()) {
				if (direction == Direction.NORTH) {
					Config.positionX -= 0.1 + multiplier;
				} else if (direction == Direction.EAST) {
					Config.positionZ -= 0.1 + multiplier;
				} else if (direction == Direction.SOUTH) {
					Config.positionX += 0.1 + multiplier;
				} else if (direction == Direction.WEST) {
					Config.positionZ += 0.1 + multiplier;
				}		
			}
			while (keyNudgeRight.wasPressed()) {
				if (direction == Direction.NORTH) {
					Config.positionX += 0.1 + multiplier;
				} else if (direction == Direction.EAST) {
					Config.positionZ += 0.1 + multiplier;
				} else if (direction == Direction.SOUTH) {
					Config.positionX -= 0.1 + multiplier;
				} else if (direction == Direction.WEST) {
					Config.positionZ -= 0.1 + multiplier;
				}
			}
			while (keyScaleXUp.wasPressed()) {
				Config.scaleX -= 0.1 + multiplier;				
			}
			while (keyScaleXDown.wasPressed()) {
				Config.scaleX += 0.1 + multiplier;
			}
			while (keyScaleYUp.wasPressed()) {
				Config.scaleY -= 0.1 + multiplier;
			}
			while (keyScaleYDown.wasPressed()) {
				Config.scaleY += 0.1 + multiplier;
			}

			while (keyRenderThroughBlocks.wasPressed()) {
				Config.renderThroughBlocks = !Config.renderThroughBlocks;
				client.player.sendMessage(Text.of("[" + MOD_NAME + "] Render Through Blocks: " + Config.renderThroughBlocks), false);
			}
			while (keySetPositionToPlayer.wasPressed()) {
				//get client position
				Config.positionX = (float) client.player.getX();
				Config.positionY = (float) client.player.getY();
				Config.positionZ = (float) client.player.getZ();				
				client.player.sendMessage(Text.of("[" + MOD_NAME + "] Changed position to current player position: " + Config.positionX + ", " + Config.positionY + ", " + Config.positionZ), false);
			}			
		});

		WorldRenderEvents.END.register(context -> {
			Boolean renderThroughBlocks = Config.renderThroughBlocks;
			float scaleX = Config.scaleX;
			float scaleY = Config.scaleY;
			float x = Config.positionX;
			float y = Config.positionY;
			float z = Config.positionZ;
			float rotationX = Config.rotationX;
			float rotationY = Config.rotationY;
			float rotationZ = Config.rotationZ;
			float alpha = Config.alpha;
			String assetPath = Config.imagePath;

			if (visible) {
				drawImage(context, renderThroughBlocks, scaleX, scaleY, x, y, z, rotationX, rotationY, rotationZ, alpha, assetPath);
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

	private void drawImage(WorldRenderContext context, Boolean renderThroughBlocks, float scaleX, float scaleY, float x,
			float y, float z, float rotationX, float rotationY, float rotationZ, float alpha, String assetPath) {

		// ensure [a-z0-9/._-] character in path of location
		if (!assetPath.matches("^[a-z0-9/._-]+$")) {
			LOGGER.error("Invalid asset path: " + assetPath);
			return;
		}

		Camera camera = context.camera();
		Vec3d targetPosition = new Vec3d(x, y, z);
		Vec3d transformedPosition = targetPosition.subtract(camera.getPos());

		MatrixStack matrixStack = new MatrixStack();
		matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
		matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);

		matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationX));
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationY));
		matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationZ));

		Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		if (alpha < 1f) {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
		// add vertices in a rectangle from -scale to +scale
		buffer.vertex(positionMatrix, -scaleX, scaleY, 0).color(1f, 1f, 1f, alpha).texture(0f, 0f).next();
		buffer.vertex(positionMatrix, -scaleX, -scaleY, 0).color(1f, 1f, 1f, alpha).texture(0f, 1f).next();
		buffer.vertex(positionMatrix, scaleX, -scaleY, 0).color(1f, 1f, 1f, alpha).texture(1f, 1f).next();
		buffer.vertex(positionMatrix, scaleX, scaleY, 0).color(1f, 1f, 1f, alpha).texture(1f, 0f).next();

		RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
		RenderSystem.setShaderTexture(0, new Identifier(MOD_ID, assetPath));
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

		if (renderThroughBlocks) {
			RenderSystem.disableCull();
			RenderSystem.depthFunc(GL11.GL_ALWAYS);
		}

		tessellator.draw();

		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.enableCull();
		matrixStack.pop();
	}
}