package vernando.blueprints;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
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

	public void render(WorldRenderContext context, Boolean renderThroughBlocks, Boolean renderBothSides) {
		if (texture == null || !visibility) {
			return;
		}
		
		Camera camera = context.camera();
		Vec3d targetPosition = new Vec3d(positionX, positionY, positionZ);
		Vec3d transformedPosition = targetPosition.subtract(camera.getPos());

		// Create a new matrix stack and properly isolate transformations
		MatrixStack matrixStack = new MatrixStack();
		matrixStack.push(); // Push a new matrix to isolate transformations
		
		matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
		matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);

		matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationX));
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationY));
		matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationZ));

		Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
		
		// Store current render state to restore it later - be more comprehensive
		boolean wasBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
		boolean wasDepthTestEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
		boolean wasCullFaceEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
		int[] currentBlendSrc = new int[1];
		int[] currentBlendDst = new int[1];
		GL11.glGetIntegerv(GL11.GL_BLEND_SRC, currentBlendSrc);
		GL11.glGetIntegerv(GL11.GL_BLEND_DST, currentBlendDst);
		
		// Store current depth function to restore it
		int[] currentDepthFunc = new int[1];
		GL11.glGetIntegerv(GL11.GL_DEPTH_FUNC, currentDepthFunc);
		
		try {
			// Don't set global shader color alpha - keep it at 1.0 to not affect other rendering
			// The alpha will be applied per-vertex instead
			
			// Enable blending for transparency
			if (!wasBlendEnabled) {
				GL11.glEnable(GL11.GL_BLEND);
			}
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			// Handle depth testing
			if (renderThroughBlocks) {
				if (wasDepthTestEnabled) {
					GL11.glDisable(GL11.GL_DEPTH_TEST);
				}
			} else {
				if (!wasDepthTestEnabled) {
					GL11.glEnable(GL11.GL_DEPTH_TEST);
				}
			}
			
			// Handle face culling
			if (renderBothSides) {
				if (wasCullFaceEnabled) {
					GL11.glDisable(GL11.GL_CULL_FACE);
				}
			} else {
				if (!wasCullFaceEnabled) {
					GL11.glEnable(GL11.GL_CULL_FACE);
				}
			}

			// Use a render layer that only requires position, texture, and color
			RenderLayer renderLayer = RenderLayer.getGuiTextured(textureId);
			var bufferBuilder = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
			var vertexConsumer = bufferBuilder.getBuffer(renderLayer);
			
			// Add vertices for a quad (rectangle) with alpha applied per-vertex to avoid affecting other blueprints
			vertexConsumer.vertex(positionMatrix, -scaleX, -scaleY / aspectRatio, 0).color(1f, 1f, 1f, alpha).texture(0f, 1f);
			vertexConsumer.vertex(positionMatrix, scaleX, -scaleY / aspectRatio, 0).color(1f, 1f, 1f, alpha).texture(1f, 1f);
			vertexConsumer.vertex(positionMatrix, scaleX, scaleY / aspectRatio, 0).color(1f, 1f, 1f, alpha).texture(1f, 0f);
			vertexConsumer.vertex(positionMatrix, -scaleX, scaleY / aspectRatio, 0).color(1f, 1f, 1f, alpha).texture(0f, 0f);

			// Ensure this blueprint's vertices are drawn before the next blueprint
			bufferBuilder.drawCurrentLayer();
			
		} finally {
			// Always restore matrix stack state
			matrixStack.pop();
			
			// Properly restore all render state to exactly what it was before
			if (wasCullFaceEnabled && !GL11.glIsEnabled(GL11.GL_CULL_FACE)) {
				GL11.glEnable(GL11.GL_CULL_FACE);
			} else if (!wasCullFaceEnabled && GL11.glIsEnabled(GL11.GL_CULL_FACE)) {
				GL11.glDisable(GL11.GL_CULL_FACE);
			}
			
			if (wasDepthTestEnabled && !GL11.glIsEnabled(GL11.GL_DEPTH_TEST)) {
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			} else if (!wasDepthTestEnabled && GL11.glIsEnabled(GL11.GL_DEPTH_TEST)) {
				GL11.glDisable(GL11.GL_DEPTH_TEST);
			}
			
			// Restore depth function
			GL11.glDepthFunc(currentDepthFunc[0]);
			
			if (wasBlendEnabled && !GL11.glIsEnabled(GL11.GL_BLEND)) {
				GL11.glEnable(GL11.GL_BLEND);
			} else if (!wasBlendEnabled && GL11.glIsEnabled(GL11.GL_BLEND)) {
				GL11.glDisable(GL11.GL_BLEND);
			}
			
			// Restore original blend function
			GL11.glBlendFunc(currentBlendSrc[0], currentBlendDst[0]);
		}
	}

	public void renderThumbnail(DrawContext drawContext, int x, int y, int width, int height, boolean includeFrame) {
		if (texture == null) {
			Main.LOGGER.error('"' + texturePath + '"' + " is not a valid image file.");
			return;
		}
		if (includeFrame) {
			drawContext.drawTexture(RenderLayer::getGuiTexturedOverlay, Identifier.of(Main.MOD_ID, "item_frame.png"), x, y, 0, 0, width, height, width, height);
		}

		x += ((2/14f) * width) + 1;
		y += ((2/12f) * height) + 1;

		width *= (10/14f);
		height *= (8/12f);
		
		drawContext.drawTexture(RenderLayer::getGuiTexturedOverlay, textureId, x, y, 0, 0, width, height, width, height);
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
		scaleY = scaleX / aspectRatio;
    }

	public void setVisible(boolean b) {
		this.visibility = b;
	}

	public String getFilename() {
		return texturePath;
	}

    public Float getPosX() {
        return positionX;
    }

	public Float getPosY() {
		return positionY;
	}

	public Float getPosZ() {
		return positionZ;
	}

	public Float getRotationX() {
		return rotationX;
	}

	public Float getRotationY() {
		return rotationY;
	}

	public Float getRotationZ() {
		return rotationZ;
	}

	public Float getScaleX() {
		return scaleX;
	}

	public Float getScaleY() {
		return scaleY;
	}

	public Float getAlpha() {
		return alpha;
	}

	public String getVisibility() {
		return Boolean.toString(visibility);
	}

	public void setVisibility(boolean b) {
		this.visibility = b;
	}

	public void setPosX(float f) {
		positionX = f;
	}

	public void setPosY(float f) {
		positionY = f;
	}

	public void setPosZ(float f) {
		positionZ = f;
	}

	public void setRotationX(float f) {
		rotationX = f;
	}

	public void setRotationY(float f) {
		rotationY = f;
	}

	public void setRotationZ(float f) {
		rotationZ = f;
	}

	public void setScaleX(float f) {
		scaleX = f;
	}

	public void setScaleY(float f) {
		scaleY = f;
	}

	public void setAlpha(float f) {
		alpha = f;
	}

	public void setVisibility(String string) {
		visibility = Boolean.parseBoolean(string);
	}	
}