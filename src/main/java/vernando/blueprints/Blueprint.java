package vernando.blueprints;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.pipeline.RenderPipeline;

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

	// GIF animation support
	private Util.GifAnimation gifAnimation;
	private boolean isAnimated;
	private int currentFrame;
	private long lastFrameTime;
	private List<NativeImageBackedTexture> frameTextures;
	private List<Identifier> frameTextureIds;

	public Blueprint(String filename) {
		texturePath = filename;
		configFile = texturePath + ".json";
		id = Long.toString(System.currentTimeMillis());
		LoadConfig();

		// Initialize animation data
		frameTextures = new ArrayList<>();
		frameTextureIds = new ArrayList<>();
		currentFrame = 0;
		lastFrameTime = System.currentTimeMillis();

		// Load texture(s) - handle both static images and GIFs
		if (Util.IsGifFile(texturePath)) {
			loadGifTextures();
		} else {
			loadStaticTexture();
		}

		SaveConfig();
	}

	private void loadStaticTexture() {
		textureId = Identifier.of(Main.MOD_ID, id);
		texture = Util.RegisterTexture(texturePath, textureId);
		if (texture != null) {
			aspectRatio = (float) texture.getImage().getWidth() / (float) texture.getImage().getHeight();
		}
		isAnimated = false;
	}

	private void loadGifTextures() {
		gifAnimation = Util.LoadGif(texturePath);
		isAnimated = gifAnimation.isAnimated;

		if (gifAnimation.frames.isEmpty()) {
			Main.LOGGER.error("No frames loaded from GIF: " + texturePath);
			return;
		}

		// Register textures for each frame
		MinecraftClient client = MinecraftClient.getInstance();
		int[] index = { 0 };
		gifAnimation.frames.forEach(frame -> {
			Identifier frameId = Identifier.of(Main.MOD_ID, id + "_frame_" + index[0]);
			NativeImageBackedTexture frameTexture = new NativeImageBackedTexture(
					() -> texturePath + " frame " + index[0], frame.image);

			client.getTextureManager().registerTexture(frameId, frameTexture);
			frameTextures.add(frameTexture);
			frameTextureIds.add(frameId);
			index[0]++;
		});

		// Set up initial texture and aspect ratio
		if (!frameTextures.isEmpty()) {
			texture = frameTextures.get(0);
			textureId = frameTextureIds.get(0);
			aspectRatio = (float) texture.getImage().getWidth() / (float) texture.getImage().getHeight();
		}

		Main.LOGGER.info("Loaded animated GIF with " + gifAnimation.frames.size() + " frames: " + texturePath);
	}

	private void updateAnimation() {
		if (!isAnimated || gifAnimation == null || gifAnimation.frames.isEmpty()) {
			return;
		}

		long currentTime = System.currentTimeMillis();
		Util.GifFrame currentGifFrame = gifAnimation.frames.get(currentFrame);

		if (currentTime - lastFrameTime >= currentGifFrame.delayMs) {
			currentFrame = (currentFrame + 1) % gifAnimation.frames.size();
			lastFrameTime = currentTime;

			// Update current texture to the new frame
			if (currentFrame < frameTextures.size() && currentFrame < frameTextureIds.size()) {
				texture = frameTextures.get(currentFrame);
				textureId = frameTextureIds.get(currentFrame);
			}
		}
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

	public void render(MatrixStack matrices, Camera camera, Boolean renderThroughBlocks, Boolean renderBothSides) {
		if (texture == null || !visibility) {
			return;
		}

		// Create RenderLayer: GUI_TEXTURED (no depth) for render-all mode,
		// registered BLUEPRINT_WORLD pipeline (depth-enabled) for render-visible mode.
		RenderPipeline pipeline = (renderThroughBlocks || BlueprintPipelines.BLUEPRINT_WORLD == null)
			? RenderPipelines.GUI_TEXTURED
			: BlueprintPipelines.BLUEPRINT_WORLD;
		RenderSetup setup = RenderSetup.builder(pipeline)
			.texture("Sampler0", textureId)
			.build();
		RenderLayer renderLayer = RenderLayer.of("blueprint_texture", setup);

		// Update animation frame if this is an animated GIF
		updateAnimation();
		Vec3d targetPosition = new Vec3d(positionX, positionY, positionZ);
		Vec3d transformedPosition = targetPosition.subtract(camera.getCameraPos());

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
			// Enable blending for transparency
			if (!wasBlendEnabled) {
				GL11.glEnable(GL11.GL_BLEND);
			}
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

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

			var bufferBuilder = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
			var vertexConsumer = bufferBuilder.getBuffer(renderLayer);

			// Get lightmap coordinates (usually full brightness for UI elements)
			int lightmapUV = 0xF000F0; // Full brightness lightmap
			int overlayUV = 0; // No overlay (UV1)

			// Normal vector pointing towards camera (since we're facing the camera)
			float normalX = 0.0f;
			float normalY = 0.0f;
			float normalZ = 1.0f;

			vertexConsumer.vertex(positionMatrix, -scaleX, -scaleY / aspectRatio, 0).color(1f, 1f, 1f, alpha)
					.texture(0f, 1f).overlay(overlayUV).light(lightmapUV).normal(normalX, normalY, normalZ);
			vertexConsumer.vertex(positionMatrix, scaleX, -scaleY / aspectRatio, 0).color(1f, 1f, 1f, alpha)
					.texture(1f, 1f).overlay(overlayUV).light(lightmapUV).normal(normalX, normalY, normalZ);
			vertexConsumer.vertex(positionMatrix, scaleX, scaleY / aspectRatio, 0).color(1f, 1f, 1f, alpha)
					.texture(1f, 0f).overlay(overlayUV).light(lightmapUV).normal(normalX, normalY, normalZ);
			vertexConsumer.vertex(positionMatrix, -scaleX, scaleY / aspectRatio, 0).color(1f, 1f, 1f, alpha)
					.texture(0f, 0f).overlay(overlayUV).light(lightmapUV).normal(normalX, normalY, normalZ);

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
			drawContext.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of(Main.MOD_ID, "item_frame.png"), x, y,
					0.0f, 0.0f, width, height, width, height);
		}

		x += ((2 / 14f) * width) + 1;
		y += ((2 / 12f) * height) + 1;

		width *= (10 / 14f);
		height *= (8 / 12f);

		drawContext.drawTexture(RenderPipelines.GUI_TEXTURED, textureId, x, y, 0.0f, 0.0f, width, height, width,
				height);
	}

	public void NudgeRotation(Axis axis, float amount, boolean multiply, boolean finetune) {
		TransformUtils.Rotation current = new TransformUtils.Rotation(rotationX, rotationY, rotationZ);
		TransformUtils.Rotation newRotation = TransformUtils.nudgeRotation3D(current, axis, amount, multiply, finetune);

		rotationX = newRotation.x;
		rotationY = newRotation.y;
		rotationZ = newRotation.z;
	}

	public void NudgePosition(vernando.blueprints.Util.Direction direction, float amount, boolean multiply,
			boolean finetune) {
		TransformUtils.Position current = new TransformUtils.Position(positionX, positionY, positionZ);
		TransformUtils.Position newPosition = TransformUtils.nudgePosition3D(current, direction, amount, multiply, finetune);

		positionX = newPosition.x;
		positionY = newPosition.y;
		positionZ = newPosition.z;
	}

	public void NudgeAlpha(float amount, boolean multiply, boolean finetune) {
		alpha = TransformUtils.nudgeAlpha(alpha, amount, multiply, finetune);
	}

	public void SetPosition(float x, float y, float z) {
		positionX = x;
		positionY = y;
		positionZ = z;
	}

	public void NudgeScale(Axis axis, float amount, boolean multiply, boolean finetune) {
		TransformUtils.Scale current = new TransformUtils.Scale(scaleX, scaleY);
		TransformUtils.Scale newScale = TransformUtils.nudgeScale2D(current, axis, amount, multiply, finetune);

		scaleX = newScale.x;
		scaleY = newScale.y;
	}

	public String getName() {
		return MathUtils.getFilenameFromPath(texturePath);
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

	/**
	 * Calculate the distance from the camera to this blueprint
	 * @param camera The camera to calculate distance from
	 * @return The distance in blocks
	 */
	public double getDistanceFromCamera(Camera camera) {
		Vec3d cameraPos = camera.getCameraPos();
		Vec3d blueprintPos = new Vec3d(positionX, positionY, positionZ);
		return cameraPos.distanceTo(blueprintPos);
	}

	/**
	 * Get the texture object for this blueprint
	 * @return The NativeImageBackedTexture
	 */
	public NativeImageBackedTexture getTexture() {
		return texture;
	}

	/**
	 * Get the texture width
	 * @return The width in pixels
	 */
	public int getTextureWidth() {
		return texture != null ? texture.getImage().getWidth() : 0;
	}

	/**
	 * Get the texture height
	 * @return The height in pixels
	 */
	public int getTextureHeight() {
		return texture != null ? texture.getImage().getHeight() : 0;
	}
}