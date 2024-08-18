package vernando.blueprints;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.Logger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;

import java.io.File;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.FileReader;

public class Blueprint {

	private NativeImageBackedTexture texture;
	public Identifier textureId;

	private String texturePath;
	private String id;

	private float scaleX;
	private float scaleY;
	private float positionX;
	private float positionY;
	private float positionZ;
	private float rotationX;
	private float rotationY;
	private float rotationZ;
	private float alpha;
	private float aspectRatio;
	public int order;
	private String configFile;
	private boolean visibility;

	public Blueprint(String filename) {
		texturePath = filename;
		configFile = texturePath + ".json";
		id = Long.toString(System.currentTimeMillis());
		LoadConfig();
		textureId = Identifier.of(Main.MOD_ID, id);
		texture = Util.RegisterTexture(texturePath, textureId);
		aspectRatio = (float) texture.getImage().getWidth() / (float) texture.getImage().getHeight();
		SaveConfig();
	}

	private void LoadConfig() {
		MinecraftClient client = MinecraftClient.getInstance();
		// start with default values:
		scaleX = 1.0f;
		scaleY = 1.0f;
		positionX = (float) client.player.getX();
		positionY = (float) client.player.getY();
		positionZ = (float) client.player.getZ();
		rotationX = 0.0f;
		rotationY = 0.0f;
		rotationZ = 0.0f;
		order = -1;
		alpha = 1.0f;
		visibility = true;

		try {
			File file = new File(configFile);
			if (file.exists()) {
				Main.LOGGER.info("Loading config: " + configFile);
				// use gson to load configFile
				Gson gson = new Gson();
				JsonReader reader = new JsonReader(new FileReader(configFile));
				JsonObject obj = gson.fromJson(reader, JsonObject.class);
				if (obj != null && obj.has("scaleX"))
					scaleX = obj.get("scaleX").getAsFloat();
				if (obj != null && obj.has("scaleY"))
					scaleY = obj.get("scaleY").getAsFloat();
				if (obj != null && obj.has("positionX"))
					positionX = obj.get("positionX").getAsFloat();
				if (obj != null && obj.has("positionY"))
					positionY = obj.get("positionY").getAsFloat();
				if (obj != null && obj.has("positionZ"))
					positionZ = obj.get("positionZ").getAsFloat();
				if (obj != null && obj.has("rotationX"))
					rotationX = obj.get("rotationX").getAsFloat();
				if (obj != null && obj.has("rotationY"))
					rotationY = obj.get("rotationY").getAsFloat();
				if (obj != null && obj.has("rotationZ"))
					rotationZ = obj.get("rotationZ").getAsFloat();
				if (obj != null && obj.has("alpha"))
					alpha = obj.get("alpha").getAsFloat();
				if (obj != null && obj.has("visibility"))
					visibility = obj.get("visibility").getAsBoolean();
			} else {
				Main.LOGGER.info("Config file not found: " + configFile);
			}
		} catch (Exception e) {
			Main.LOGGER.error("Failed to load config: " + configFile);
			Main.LOGGER.error(e.getMessage());
		}
	}

	public void SaveConfig() {
		try {
			Main.LOGGER.info("Saving config: " + configFile);
			JsonObject obj = new JsonObject();
			obj.addProperty("scaleX", scaleX);
			obj.addProperty("scaleY", scaleY);
			obj.addProperty("positionX", positionX);
			obj.addProperty("positionY", positionY);
			obj.addProperty("positionZ", positionZ);
			obj.addProperty("rotationX", rotationX);
			obj.addProperty("rotationY", rotationY);
			obj.addProperty("rotationZ", rotationZ);
			obj.addProperty("alpha", alpha);
			obj.addProperty("visibility", visibility);
			String json = obj.toString();
			java.nio.file.Files.write(java.nio.file.Paths.get(configFile), json.getBytes());
		} catch (Exception e) {
			Main.LOGGER.error("Failed to save config: " + configFile);
			Main.LOGGER.error(e.getMessage());
			return;
		}
	}

	public void render(WorldRenderContext context, Boolean renderThroughBlocks) {
		if (texture == null || !visibility) {
			return;
		}		

		Camera camera = context.camera();
		Vec3d targetPosition = new Vec3d(positionX, positionY, positionZ);
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

		if (alpha < 1f) {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}

		BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		// add vertices in a rectangle from -scale to +scale
		buffer.vertex(positionMatrix, -scaleX, scaleY / aspectRatio, 0).color(1f, 1f, 1f, alpha).texture(0f, 0f);
		buffer.vertex(positionMatrix, -scaleX, -scaleY / aspectRatio, 0).color(1f, 1f, 1f, alpha).texture(0f, 1f);
		buffer.vertex(positionMatrix, scaleX, -scaleY / aspectRatio, 0).color(1f, 1f, 1f, alpha).texture(1f, 1f);
		buffer.vertex(positionMatrix, scaleX, scaleY / aspectRatio, 0).color(1f, 1f, 1f, alpha).texture(1f, 0f);

		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.setShaderTexture(0, textureId);
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

		if (renderThroughBlocks) {			
			RenderSystem.disableCull();
			RenderSystem.depthFunc(GL11.GL_ALWAYS);
		}
		
		RenderSystem.enableDepthTest();
		BufferRenderer.drawWithGlobalProgram(buffer.end());

		RenderSystem.enableCull();
		matrixStack.pop();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1);
		
	}

	public void renderThumbnail(DrawContext drawContext, int x, int y, int width, int height, boolean includeFrame) {
		if (texture == null) {
			Main.LOGGER.error('"' + texturePath + '"' + " is not a valid image file.");
			return;
		}
		if (includeFrame) {
			drawContext.drawTexture(Identifier.of(Main.MOD_ID, "item_frame.png"), x, y, 0, 0, width, height, width, height);
		}

		x += ((2/14f) * width) + 1;
		y += ((2/12f) * height) + 1;

		width *= (10/14f);
		height *= (8/12f);
		
		drawContext.drawTexture(textureId, x, y, 0, 0, width, height, width, height);
	}

	public void NudgeRotation(Axis axis, float amount, Boolean multiply, Boolean finetune) {
		if (finetune && multiply) {
			amount *= 180;
		} else if (finetune) {
			amount /= 10;
		} else if (multiply) {
			amount *= 90;
		}
		switch (axis) {
			case X:
				rotationX += amount;
				if (rotationX > 360) {
					rotationX -= 360;
				}
				if (rotationX < 0) {
					rotationX += 360;
				}
				break;
			case Y:
				rotationY += amount;
				if (rotationY > 360) {
					rotationY -= 360;
				}
				if (rotationY < 0) {
					rotationY += 360;
				}
				break;
			case Z:
				rotationZ += amount;
				if (rotationZ > 360) {
					rotationZ -= 360;
				}
				if (rotationZ < 0) {
					rotationZ += 360;
				}
				break;
		}
	}

	public void NudgePosition(vernando.blueprints.Util.Direction direction, float amount, Boolean multiply,
			Boolean finetune) {
		if (finetune && multiply) {
			amount *= 100f;
		} else if (finetune) {
			amount /= 10f;
		} else if (multiply) {
			amount *= 10;
		}
		switch (direction) {
			case UP:
				positionY += amount;
				break;
			case DOWN:
				positionY -= amount;
				break;
			case EAST:
				positionX += amount;
				break;
			case WEST:
				positionX -= amount;
				break;
			case NORTH:
				positionZ -= amount;
				break;
			case SOUTH:
				positionZ += amount;
				break;
		}
	}

	public void NudgeAlpha(float amount, Boolean multiply, Boolean finetune) {
		if (finetune && multiply) {
			amount *= 100f;
		} else if (finetune) {
			amount /= 10f;
		} else if (multiply) {
			amount *= 10;
		}
		alpha += amount;
		if (alpha > 1) {
			alpha = 1;
		}
		if (alpha < 0) {
			alpha = 0;
		}
	}

	public void SetPosition(float x, float y, float z) {
		positionX = x;
		positionY = y;
		positionZ = z;
	}

	public void NudgeScale(Axis axis, float amount, Boolean multiply, Boolean finetune) {
		if (finetune && multiply) {
			amount *= 100f;
		} else if (finetune) {
			amount /= 10f;
		} else if (multiply) {
			amount *= 10;
		}
		switch (axis) {
			case X:
				scaleX += amount;
				break;
			case Y:
				scaleY += amount;
				break;
			case Z:
				// not implemented
				break;
		}
	}

	public String getName() {
		return texturePath.split("/")[texturePath.split("/").length - 1];
	}

	public void ToggleVisibility() {
		this.visibility = !this.visibility;
	}

	public boolean isVisible() {
		return this.visibility;
	}

	public void ResetRotation() {
		rotationX = 0.0f;
		rotationY = 0.0f;
		rotationZ = 0.0f;
	}

    public void ResetScale() {
        scaleX = 1.0f;
		scaleY = 1.0f;
    }

	public void setVisible(boolean b) {
		this.visibility = b;
	}
}