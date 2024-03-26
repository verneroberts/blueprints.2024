package vernando.imageref;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;

import java.io.File;
import java.io.FileInputStream;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.FileReader;

public class Blueprint {	

	private NativeImageBackedTexture texture;
	private Identifier textureId;

	private String texturePath;
	private String id;

	public float scaleX;
	public float scaleY;
	public float positionX;
	public float positionY;
	public float positionZ;
	public float rotationX;
	public float rotationY;
	public float rotationZ;
	public float alpha;
	private String configFile;

	public Blueprint(String filename) {
		texturePath = filename;
		configFile = texturePath + ".json";
		id = Long.toString(System.currentTimeMillis());
		LoadConfig();
		registerTexture();
	}

	private void LoadConfig() {
		MinecraftClient client = MinecraftClient.getInstance();
		// start with default values:
		scaleX = 1.0f;
		scaleY = 1.0f;
		positionX = (float)client.player.getX();
		positionY = (float)client.player.getY();
		positionZ = (float)client.player.getZ();
		rotationX = 0.0f;
		rotationY = 0.0f;
		rotationZ = 0.0f;
		alpha = 1.0f;

		try {
			File file = new File(configFile);
			if (file.exists()) {
				Main.LOGGER.info("Loading config: " + configFile);
				// use gson to load configFile
				Gson gson = new Gson();
				JsonReader reader = new JsonReader(new FileReader(configFile));
				JsonObject obj = gson.fromJson(reader, JsonObject.class);
				if (obj != null && obj.has("scaleX")) scaleX = obj.get("scaleX").getAsFloat();
				if (obj != null && obj.has("scaleY")) scaleY = obj.get("scaleY").getAsFloat();
				if (obj != null && obj.has("positionX")) positionX = obj.get("positionX").getAsFloat();
				if (obj != null && obj.has("positionY")) positionY = obj.get("positionY").getAsFloat();
				if (obj != null && obj.has("positionZ")) positionZ = obj.get("positionZ").getAsFloat();
				if (obj != null && obj.has("rotationX")) rotationX = obj.get("rotationX").getAsFloat();
				if (obj != null && obj.has("rotationY")) rotationY = obj.get("rotationY").getAsFloat();
				if (obj != null && obj.has("rotationZ")) rotationZ = obj.get("rotationZ").getAsFloat();
				if (obj != null && obj.has("alpha")) alpha = obj.get("alpha").getAsFloat();
			} else {
				Main.LOGGER.info("Config file not found: " + configFile);
			}
		} catch (Exception e) {
			Main.LOGGER.error("Failed to load config: " + configFile);
			Main.LOGGER.error(e.getMessage());
		}
		SaveConfig();
	}

	private void SaveConfig() {
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
			String json = obj.toString();
			java.nio.file.Files.write(java.nio.file.Paths.get(configFile), json.getBytes());			
		} catch (Exception e) {
			Main.LOGGER.error("Failed to save config: " + configFile);
			Main.LOGGER.error(e.getMessage());
			return;
		}
	}	

	private void registerTexture() {
		MinecraftClient client = MinecraftClient.getInstance();
		try {
			Main.LOGGER.info("Loading image: " + texturePath + " as " + id);
			NativeImage image = NativeImage.read(new FileInputStream(new File(texturePath)));
			texture = new NativeImageBackedTexture(image);		
			textureId = new Identifier(Main.MOD_ID, id);
			Main.LOGGER.info("Registering texture: " + textureId);			
			client.getTextureManager().registerTexture(textureId, texture);		
			
		} catch (Exception e) {
			Main.LOGGER.error("Failed to load image: " + texturePath);
			Main.LOGGER.error(e.getMessage());
			return;
		}
	}

	public void render(WorldRenderContext context, Boolean renderThroughBlocks) {		
		if (texture == null) {
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
		RenderSystem.setShaderTexture(0, textureId);
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

		if (renderThroughBlocks) {
			RenderSystem.disableCull();
			RenderSystem.depthFunc(GL11.GL_ALWAYS);
		}

		tessellator.draw();

		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.enableCull();
		matrixStack.pop();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1);
	}

    public void renderThumbnail(DrawContext drawContext) {
		if (texture == null) {
			return;
		}

		// Get the transformation matrix from the matrix stack, alongside the tessellator instance and a new buffer builder.
		Matrix4f transformationMatrix = drawContext.getMatrices().peek().getPositionMatrix();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
		buffer.vertex(transformationMatrix, 20, 20, 0).color(1f, 1f, 1f, 1f).texture(0f, 0f).next();
		buffer.vertex(transformationMatrix, 20, 60, 0).color(1f, 1f, 1f, 1f).texture(0f, 1f).next();
		buffer.vertex(transformationMatrix, 60, 60, 0).color(1f, 1f, 1f, 1f).texture(1f, 1f).next();
		buffer.vertex(transformationMatrix, 60, 20, 0).color(1f, 1f, 1f, 1f).texture(1f, 0f).next();

		RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
		RenderSystem.setShaderTexture(0, textureId);
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

		tessellator.draw();		
	}

	public void NudgeRotation(Axis axis, Boolean multiply) {
		switch (axis) {
			case X:
				rotationX += 1 + (multiply ? 9 : 0);
				break;
			case Y:
				rotationY += 1 + (multiply ? 9 : 0);
				break;
			case Z:
				rotationZ += 1 + (multiply ? 9 : 0);
				break;
		}
		SaveConfig();
	}

	public void NudgePosition(vernando.imageref.Util.Direction direction, Boolean multiply) {
		switch (direction) {
			case UP:
				positionY += 0.1 + (multiply ? 0.9 : 0);
				break;
			case DOWN:
				positionY -= 0.1 + (multiply ? 0.9 : 0);
				break;
			case EAST:
				positionX += 0.1 + (multiply ? 0.9 : 0);
				break;
			case WEST:
				positionX -= 0.1 + (multiply ? 0.9 : 0);
				break;
			case NORTH:
				positionZ -= 0.1 + (multiply ? 0.9 : 0);
				break;
			case SOUTH:
				positionZ += 0.1 + (multiply ? 0.9 : 0);
				break;
		}
		SaveConfig();
	}

	public void ToggleAlpha() {
		alpha = alpha == 1.0f ? 0.5f : 1.0f;
		SaveConfig();
	}

    public void SetPosition(float x, float y, float z) {
        positionX = x;
		positionY = y;
		positionZ = z;
		SaveConfig();
    }

    public void NudgeScale(Axis axis, float amount, Boolean multiply) {
		if (multiply) {
			amount *= 10;
		}		
		switch (axis) {
			case X:
				scaleX += amount;
				break;
			case Y:
				scaleY += amount;
				break;	
		}
    }

    public String getName() {
        return texturePath.split("/")[texturePath.split("/").length-1];
    }
}