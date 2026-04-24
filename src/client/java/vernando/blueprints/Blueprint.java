package vernando.blueprints;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;

public class Blueprint {

	private DynamicTexture texture;
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
	private List<DynamicTexture> frameTextures;
	private List<Identifier> frameTextureIds;

	// World-space direct render resources (for depth-tested rendering)
	private ByteBufferBuilder worldAllocator;
	private MappableRingBuffer worldVertexBuffer;

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
		textureId = Identifier.fromNamespaceAndPath(Main.MOD_ID, id);
		texture = Util.RegisterTexture(texturePath, textureId);
		if (texture != null) {
			aspectRatio = (float) texture.getPixels().getWidth() / (float) texture.getPixels().getHeight();
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
		Minecraft client = Minecraft.getInstance();
		int[] index = { 0 };
		gifAnimation.frames.forEach(frame -> {
			Identifier frameId = Identifier.fromNamespaceAndPath(Main.MOD_ID, id + "_frame_" + index[0]);
			DynamicTexture frameTexture = new DynamicTexture(
					() -> texturePath + " frame " + index[0], frame.image);

			client.getTextureManager().register(frameId, frameTexture);
			frameTextures.add(frameTexture);
			frameTextureIds.add(frameId);
			index[0]++;
		});

		// Set up initial texture and aspect ratio
		if (!frameTextures.isEmpty()) {
			texture = frameTextures.get(0);
			textureId = frameTextureIds.get(0);
			aspectRatio = (float) texture.getPixels().getWidth() / (float) texture.getPixels().getHeight();
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
		Minecraft client = Minecraft.getInstance();
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


	/**
	 * Render this blueprint in world space with depth testing (render-visible mode).
	 * Uses a direct GPU render pass so that DynamicTransforms UBO is correctly bound.
	 */
	public void renderWorld(Camera camera, boolean renderThroughBlocks) {
		if (texture == null || !visibility || BlueprintPipelines.BLUEPRINT_WORLD == null || BlueprintPipelines.BLUEPRINT_ALL == null) {
			return;
		}

		updateAnimation();

		RenderPipeline pipeline = renderThroughBlocks ? BlueprintPipelines.BLUEPRINT_ALL : BlueprintPipelines.BLUEPRINT_WORLD;
		VertexFormat vertexFormat = pipeline.getVertexFormat();
		VertexFormat.Mode drawMode = pipeline.getVertexFormatMode();

		// Blueprint model matrix: camera-origin translation + local rotations.
		Vec3 cam = camera.position();
		PoseStack ms = new PoseStack();
		ms.translate((float)(positionX - cam.x), (float)(positionY - cam.y), (float)(positionZ - cam.z));
		ms.mulPose(Axis.XP.rotationDegrees(rotationX));
		ms.mulPose(Axis.YP.rotationDegrees(rotationY));
		ms.mulPose(Axis.ZP.rotationDegrees(rotationZ));
		Matrix4f modelMatrix = new Matrix4f(ms.last().pose());

		// Full model-view = current view matrix × blueprint model matrix.
		// Vertices submitted in local space; shader applies this full transform.
		Matrix4f modelViewMatrix = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(modelMatrix);

		// Build vertex buffer (local quad coords, not pre-transformed by model matrix).
		int vertexSize = vertexFormat.getVertexSize();
		if (worldAllocator == null) {
			worldAllocator = new ByteBufferBuilder(vertexSize * 4 + 64);
		}

		BufferBuilder buffer = new BufferBuilder(worldAllocator, drawMode, vertexFormat);
		buffer.addVertex(-scaleX, -scaleY / aspectRatio, 0).setUv(0f, 1f).setColor(1f, 1f, 1f, alpha);
		buffer.addVertex( scaleX, -scaleY / aspectRatio, 0).setUv(1f, 1f).setColor(1f, 1f, 1f, alpha);
		buffer.addVertex( scaleX,  scaleY / aspectRatio, 0).setUv(1f, 0f).setColor(1f, 1f, 1f, alpha);
		buffer.addVertex(-scaleX,  scaleY / aspectRatio, 0).setUv(0f, 0f).setColor(1f, 1f, 1f, alpha);

		MeshData builtBuffer = buffer.buildOrThrow();
		MeshData.DrawState drawParams = builtBuffer.drawState();

		// Upload vertex data to a ring buffer (3 buffers to avoid CPU/GPU sync stalls).
		int bufSize = drawParams.vertexCount() * vertexSize;
		if (worldVertexBuffer == null || worldVertexBuffer.size() < bufSize) {
			if (worldVertexBuffer != null) worldVertexBuffer.close();
			worldVertexBuffer = new MappableRingBuffer(
				() -> "blueprints_world_" + id,
				GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
				bufSize
			);
		}

		GpuBuffer gpuBuf = worldVertexBuffer.currentBuffer();
		try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder()
				.mapBuffer(gpuBuf, false, true)) {
			MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
		}

		// Sequential index buffer works for both QUADS and other draw modes.
		RenderSystem.AutoStorageIndexBuffer seqBuf = RenderSystem.getSequentialBuffer(drawMode);
		GpuBuffer indices = seqBuf.getBuffer(drawParams.indexCount());
		VertexFormat.IndexType indexType = seqBuf.type();

		// Write DynamicTransforms UBO (model-view matrix + identity texture transform).
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
			.writeTransform(modelViewMatrix, new Vector4f(1f, 1f, 1f, 1f), new Vector3f(), new Matrix4f());

		// Get GPU texture view and sampler from the registered texture.
		AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(textureId);

		// Execute render pass against the main framebuffer (world depth buffer is attached here).
		Minecraft client = Minecraft.getInstance();
		try (RenderPass rp = RenderSystem.getDevice().createCommandEncoder()
				.createRenderPass(
					() -> "blueprint_world_render",
					client.getMainRenderTarget().getColorTextureView(),
					OptionalInt.empty(),
					client.getMainRenderTarget().getDepthTextureView(),
					OptionalDouble.empty())) {
			rp.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(rp);
			rp.setUniform("DynamicTransforms", dynamicTransforms);
			rp.bindTexture("Sampler0", tex.getTextureView(), tex.getSampler());
			rp.setVertexBuffer(0, gpuBuf);
			rp.setIndexBuffer(indices, indexType);
			rp.drawIndexed(0, 0, drawParams.indexCount(), 1);
		}

		builtBuffer.close();
		worldVertexBuffer.rotate();
	}

	public void closeWorldResources() {
		if (worldVertexBuffer != null) {
			worldVertexBuffer.close();
			worldVertexBuffer = null;
		}
		if (worldAllocator != null) {
			worldAllocator.close();
			worldAllocator = null;
		}
	}

	public void renderThumbnail(GuiGraphicsExtractor drawContext, int x, int y, int width, int height, boolean includeFrame) {
		if (texture == null) {
			Main.LOGGER.error('"' + texturePath + '"' + " is not a valid image file.");
			return;
		}

		if (includeFrame) {
			drawContext.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(Main.MOD_ID, "item_frame.png"), x, y,
					0.0f, 0.0f, width, height, width, height);
		}

		x += ((2 / 14f) * width) + 1;
		y += ((2 / 12f) * height) + 1;

		width *= (10 / 14f);
		height *= (8 / 12f);

		drawContext.blit(RenderPipelines.GUI_TEXTURED, textureId, x, y, 0.0f, 0.0f, width, height, width,
				height);
	}

	public void NudgeRotation(net.minecraft.core.Direction.Axis axis, float amount, boolean multiply, boolean finetune) {
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

	public void NudgeScale(net.minecraft.core.Direction.Axis axis, float amount, boolean multiply, boolean finetune) {
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
		Vec3 cameraPos = camera.position();
		Vec3 blueprintPos = new Vec3(positionX, positionY, positionZ);
		return cameraPos.distanceTo(blueprintPos);
	}

	/**
	 * Get the texture object for this blueprint
	 * @return The NativeImageBackedTexture
	 */
	public DynamicTexture getTexture() {
		return texture;
	}

	/**
	 * Get the texture width
	 * @return The width in pixels
	 */
	public int getTextureWidth() {
		return texture != null ? texture.getPixels().getWidth() : 0;
	}

	/**
	 * Get the texture height
	 * @return The height in pixels
	 */
	public int getTextureHeight() {
		return texture != null ? texture.getPixels().getHeight() : 0;
	}
}