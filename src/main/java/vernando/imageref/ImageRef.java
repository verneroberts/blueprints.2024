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

	private static KeyBinding keyBindingKey4;
	private static KeyBinding keyBindingKey6;
	private static KeyBinding keyBindingKey8;
	private static KeyBinding keyBindingKey2;
	private static KeyBinding keyBindingKey7;
	private static KeyBinding keyBindingKey9;
	private static KeyBinding keyBindingKey1;
	private static KeyBinding keyBindingKey3;
	private static KeyBinding keyBindingKey5;
	private static KeyBinding keyBindingKey0;
	private static KeyBinding keyBindingKeyDot;

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");

		Config.init(MOD_ID, Config.class);

		keyBindingKey2 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.image-ref.key2", GLFW.GLFW_KEY_KP_2, "key.categories.misc"));
		keyBindingKey4 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.image-ref.key4", GLFW.GLFW_KEY_KP_4, "key.categories.misc"));
		keyBindingKey6 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.image-ref.key6", GLFW.GLFW_KEY_KP_6, "key.categories.misc"));
		keyBindingKey8 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.image-ref.key8", GLFW.GLFW_KEY_KP_8, "key.categories.misc"));
		keyBindingKey7 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.image-ref.key7", GLFW.GLFW_KEY_KP_7, "key.categories.misc"));
		keyBindingKey9 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.image-ref.key9", GLFW.GLFW_KEY_KP_9, "key.categories.misc"));
		keyBindingKey1 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.image-ref.key1", GLFW.GLFW_KEY_KP_1, "key.categories.misc"));
		keyBindingKey3 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.image-ref.key3", GLFW.GLFW_KEY_KP_3, "key.categories.misc"));
		keyBindingKey5 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.image-ref.key5", GLFW.GLFW_KEY_KP_5, "key.categories.misc"));
		keyBindingKey0 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.image-ref.key0", GLFW.GLFW_KEY_KP_0, "key.categories.misc"));
		keyBindingKeyDot = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.image-ref.keydot", GLFW.GLFW_KEY_KP_DECIMAL, "key.categories.misc"));

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
					// client.player.sendMessage(Text.of("[" + MOD_NAME + "] Enabled"), false);
				}
			} else {
				if (visible == true) {
					visible = false;
					// client.player.sendMessage(Text.of("[" + MOD_NAME + "] Disabled"), false);
				}				
				return;
			}

			// work out the direction the player is facing and use that as the reference when tweaking the position
			String facing = getFacing(client.player.getYaw());
			
			String upDown = getUpDown(client.player.getPitch());
			client.player.sendMessage(Text.of("[" + MOD_NAME + "] Facing: " + facing + ", UpDown: " + upDown), false);
			client.player.sendMessage(Text.of("current yaw: " + client.player.getYaw() + ", pitch: " + client.player.getPitch()), false);

			float multiplier = keyBindingKey5.isPressed() ? 9.9f : 0f;
			while (keyBindingKey2.wasPressed()) {
				Config.positionY -= 0.1 + multiplier;
			}
			while (keyBindingKey8.wasPressed()) {
				Config.positionY += 0.1 + multiplier;
			}
			while (keyBindingKey4.wasPressed()) {
				Config.positionX -= 0.1 + multiplier;
			}
			while (keyBindingKey6.wasPressed()) {
				Config.positionX += 0.1 + multiplier;
			}
			while (keyBindingKey7.wasPressed()) {
				Config.scaleX -= 0.1 + multiplier;				
			}
			while (keyBindingKey9.wasPressed()) {
				Config.scaleX += 0.1 + multiplier;
			}
			while (keyBindingKey1.wasPressed()) {
				Config.scaleY -= 0.1 + multiplier;
			}
			while (keyBindingKey3.wasPressed()) {
				Config.scaleY += 0.1 + multiplier;
			}

			while (keyBindingKey0.wasPressed()) {
				Config.renderThroughBlocks = !Config.renderThroughBlocks;
				client.player.sendMessage(Text.of("[" + MOD_NAME + "] Render Through Blocks: " + Config.renderThroughBlocks), false);
			}
			while (keyBindingKeyDot.wasPressed()) {
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

	private String getFacing(float yaw) {
		String facing = "?";
		if (yaw > -45 && yaw < 45) {
			facing = "S";
		}
		if (yaw > 45 && yaw < 135) {
			facing = "W";
		}
		if (yaw > 135 || yaw < -135) {
			facing = "N";
		}
		if (yaw > -135 && yaw < -45) {
			facing = "E";
		}
		return facing;
	}

	private String getUpDown(float pitch) {
		String upDown = "N";
		if (pitch >= 45) {
			upDown = "U";
		} else if (pitch <= -45) {
			upDown = "D";
		}
		return upDown;
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
		buffer.vertex(positionMatrix, 0, scaleY, 0).color(1f, 1f, 1f, 1f).texture(0f, 0f).next();
		buffer.vertex(positionMatrix, 0, 0, 0).color(1f, 1f, 1f, 1f).texture(0f, 1f).next();
		buffer.vertex(positionMatrix, scaleX, 0, 0).color(1f, 1f, 1f, 1f).texture(1f, 1f).next();
		buffer.vertex(positionMatrix, scaleX, scaleY, 0).color(1f, 1f, 1f, 1f).texture(1f, 0f).next();

		// create texture by loading image from asset path, then register the texture

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