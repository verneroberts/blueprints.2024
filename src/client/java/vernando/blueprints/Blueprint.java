package vernando.blueprints;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.PrimitiveTopology;
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

public class Blueprint {

	private static final AtomicInteger NEXT_ID = new AtomicInteger();
	private static final Gson GSON = new Gson();

	/**
	 * The persisted state of a blueprint. Field names double as the JSON keys
	 * in the per-image .json config file, so renaming a field is a format change.
	 */
	private static class Config {
		float scaleX = 1.0f;
		float scaleY = 1.0f;
		float positionX;
		float positionY;
		float positionZ;
		float rotationX;
		float rotationY;
		float rotationZ;
		float alpha = 1.0f;
		boolean visibility = true;
		boolean flipHorizontal;
		boolean flipVertical;
		int gridMode; // 0 = off, 1 = dark lines, 2 = light lines
		float gridSpacing = 1.0f; // in blocks
	}

	private DynamicTexture texture;
	public Identifier textureId;

	private final String texturePath;
	private final String id;
	private final String configFile;

	private Config config = new Config();
	private float aspectRatio;
	public int order;

	// GIF animation support
	private Util.GifAnimation gifAnimation;
	private boolean isAnimated;
	private int currentFrame;
	private long lastFrameTime;
	private final List<DynamicTexture> frameTextures;
	private final List<Identifier> frameTextureIds;

	// World-space direct render resources (for depth-tested rendering)
	private ByteBufferBuilder worldAllocator;
	private MappableRingBuffer worldVertexBuffer;
	private ByteBufferBuilder gridAllocator;
	private MappableRingBuffer gridVertexBuffer;

	// Cap grid lines per axis so a huge image with a tiny spacing can't build an enormous mesh
	private static final int MAX_GRID_LINES_PER_AXIS = 257;

	public Blueprint(String filename) {
		texturePath = filename;
		configFile = texturePath + ".json";
		id = "blueprint_" + NEXT_ID.incrementAndGet();
		loadConfig();

		// Initialize animation data
		frameTextures = new ArrayList<>();
		frameTextureIds = new ArrayList<>();
		currentFrame = 0;
		lastFrameTime = System.currentTimeMillis();

		// Load texture(s) - handle both static images and GIFs
		if (Util.isGifFile(texturePath)) {
			loadGifTextures();
		} else {
			loadStaticTexture();
		}

		saveConfig();
	}

	private void loadStaticTexture() {
		textureId = Identifier.fromNamespaceAndPath(Main.MOD_ID, id);
		texture = Util.registerTexture(texturePath, textureId);
		if (texture != null) {
			aspectRatio = (float) texture.getPixels().getWidth() / (float) texture.getPixels().getHeight();
		}
		isAnimated = false;
	}

	private void loadGifTextures() {
		gifAnimation = Util.loadGif(texturePath);
		isAnimated = gifAnimation.isAnimated;

		if (gifAnimation.frames.isEmpty()) {
			Main.LOGGER.error("No frames loaded from GIF: {}", texturePath);
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

		Main.LOGGER.info("Loaded animated GIF with {} frames: {}", gifAnimation.frames.size(), texturePath);
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

	private void loadConfig() {
		config = new Config();
		order = -1;

		// New blueprints start at the player's feet rather than the world origin.
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			config.positionX = (float) player.getX();
			config.positionY = (float) player.getY();
			config.positionZ = (float) player.getZ();
		}

		File file = new File(configFile);
		if (!file.exists()) {
			Main.LOGGER.info("Config file not found: {}", configFile);
			return;
		}

		Main.LOGGER.info("Loading config: {}", configFile);
		try (FileReader reader = new FileReader(file)) {
			Config loaded = GSON.fromJson(reader, Config.class);
			if (loaded != null) {
				config = loaded;
			}
		} catch (Exception e) {
			Main.LOGGER.error("Failed to load config: {}", configFile, e);
		}
	}

	public void saveConfig() {
		Main.LOGGER.info("Saving config: {}", configFile);
		try (FileWriter writer = new FileWriter(configFile)) {
			GSON.toJson(config, writer);
		} catch (Exception e) {
			Main.LOGGER.error("Failed to save config: {}", configFile, e);
		}
	}


	/**
	 * Render this blueprint in world space with depth testing (render-visible mode).
	 * Uses a direct GPU render pass so that DynamicTransforms UBO is correctly bound.
	 */
	public void renderWorld(Camera camera, boolean renderThroughBlocks) {
		if (texture == null || !config.visibility || BlueprintPipelines.BLUEPRINT_WORLD == null || BlueprintPipelines.BLUEPRINT_ALL == null) {
			return;
		}

		updateAnimation();

		RenderPipeline pipeline = renderThroughBlocks ? BlueprintPipelines.BLUEPRINT_ALL : BlueprintPipelines.BLUEPRINT_WORLD;
		VertexFormat vertexFormat = pipeline.getVertexFormatBinding(0);
		PrimitiveTopology drawMode = pipeline.getPrimitiveTopology();

		// Blueprint model matrix: camera-origin translation + local rotations.
		Vec3 cam = camera.position();
		PoseStack ms = new PoseStack();
		ms.translate((float)(config.positionX - cam.x), (float)(config.positionY - cam.y), (float)(config.positionZ - cam.z));
		ms.mulPose(Axis.XP.rotationDegrees(config.rotationX));
		ms.mulPose(Axis.YP.rotationDegrees(config.rotationY));
		ms.mulPose(Axis.ZP.rotationDegrees(config.rotationZ));
		Matrix4f modelMatrix = new Matrix4f(ms.last().pose());

		// Full model-view = current view matrix × blueprint model matrix.
		// Vertices submitted in local space; shader applies this full transform.
		Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrixCopy().mul(modelMatrix);

		// Build vertex buffer (local quad coords, not pre-transformed by model matrix).
		int vertexSize = vertexFormat.getVertexSize();
		if (worldAllocator == null) {
			worldAllocator = new ByteBufferBuilder(vertexSize * 4 + 64);
		}

		float scaleX = config.scaleX;
		float scaleY = config.scaleY;
		float alpha = config.alpha;
		float halfHeight = scaleY / aspectRatio;
		float uLeft = config.flipHorizontal ? 1f : 0f;
		float uRight = config.flipHorizontal ? 0f : 1f;
		float vBottom = config.flipVertical ? 0f : 1f;
		float vTop = config.flipVertical ? 1f : 0f;

		BufferBuilder buffer = new BufferBuilder(worldAllocator, drawMode, vertexFormat);
		buffer.addVertex(-scaleX, -halfHeight, 0).setUv(uLeft, vBottom).setColor(1f, 1f, 1f, alpha);
		buffer.addVertex( scaleX, -halfHeight, 0).setUv(uRight, vBottom).setColor(1f, 1f, 1f, alpha);
		buffer.addVertex( scaleX,  halfHeight, 0).setUv(uRight, vTop).setColor(1f, 1f, 1f, alpha);
		buffer.addVertex(-scaleX,  halfHeight, 0).setUv(uLeft, vTop).setColor(1f, 1f, 1f, alpha);

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
		try (GpuBufferSlice.MappedView mappedView = gpuBuf.map(false, true)) {
			MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
		}

		// Sequential index buffer works for both QUADS and other draw modes.
		RenderSystem.AutoStorageIndexBuffer seqBuf = RenderSystem.getSequentialBuffer(drawMode);
		GpuBuffer indices = seqBuf.getBuffer(drawParams.indexCount());
		IndexType indexType = seqBuf.type();

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
					client.gameRenderer.mainRenderTarget().getColorTextureView(),
					Optional.empty(),
					client.gameRenderer.mainRenderTarget().getDepthTextureView(),
					OptionalDouble.empty())) {
			rp.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(rp);
			rp.setUniform("DynamicTransforms", dynamicTransforms);
			rp.bindTexture("Sampler0", tex.getTextureView(), tex.getSampler());
			rp.setVertexBuffer(0, gpuBuf.slice());
			rp.setIndexBuffer(indices, indexType);
			rp.drawIndexed(drawParams.indexCount(), 1, 0, 0, 0);
		}

		builtBuffer.close();
		worldVertexBuffer.rotate();

		if (config.gridMode > 0) {
			renderGrid(pipeline, vertexFormat, drawMode, dynamicTransforms, halfHeight);
		}
	}

	/**
	 * Render a block-spaced grid over the image in a second pass, using a 1x1 white
	 * texture so lines can be drawn with plain vertex colors through the same pipeline.
	 * Lines are anchored to the top-left corner of the image and spaced gridSpacing
	 * blocks apart in local space, so a calibrated image gets a true block grid.
	 */
	private void renderGrid(RenderPipeline pipeline, VertexFormat vertexFormat, PrimitiveTopology drawMode,
			GpuBufferSlice dynamicTransforms, float halfHeight) {
		float scaleX = config.scaleX;
		float lineAlpha = 0.6f * config.alpha;
		if (lineAlpha <= 0.01f) {
			return;
		}
		float shade = config.gridMode == 2 ? 1f : 0f;
		float spacing = Math.max(0.1f, config.gridSpacing);
		// half thickness; thin lines, but never wider than a quarter of a cell
		float t = Math.min(0.02f, spacing / 8f);

		int vertexSize = vertexFormat.getVertexSize();
		if (gridAllocator == null) {
			gridAllocator = new ByteBufferBuilder(vertexSize * 4 * 64);
		}

		BufferBuilder buffer = new BufferBuilder(gridAllocator, drawMode, vertexFormat);
		// vertical lines, anchored at the left edge, plus the right edge
		for (int i = 0; i < MAX_GRID_LINES_PER_AXIS; i++) {
			float x = -scaleX + i * spacing;
			if (x >= scaleX - t) {
				break;
			}
			addGridQuad(buffer, Math.max(x - t, -scaleX), -halfHeight, x + t, halfHeight, shade, lineAlpha);
		}
		addGridQuad(buffer, scaleX - 2 * t, -halfHeight, scaleX, halfHeight, shade, lineAlpha);
		// horizontal lines, anchored at the top edge, plus the bottom edge
		for (int i = 0; i < MAX_GRID_LINES_PER_AXIS; i++) {
			float y = halfHeight - i * spacing;
			if (y <= -halfHeight + t) {
				break;
			}
			addGridQuad(buffer, -scaleX, y - t, scaleX, Math.min(y + t, halfHeight), shade, lineAlpha);
		}
		addGridQuad(buffer, -scaleX, -halfHeight, scaleX, -halfHeight + 2 * t, shade, lineAlpha);

		MeshData mesh = buffer.buildOrThrow();
		MeshData.DrawState drawParams = mesh.drawState();

		int bufSize = drawParams.vertexCount() * vertexSize;
		if (gridVertexBuffer == null || gridVertexBuffer.size() < bufSize) {
			if (gridVertexBuffer != null) gridVertexBuffer.close();
			gridVertexBuffer = new MappableRingBuffer(
				() -> "blueprints_grid_" + id,
				GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
				bufSize
			);
		}

		GpuBuffer gpuBuf = gridVertexBuffer.currentBuffer();
		try (GpuBufferSlice.MappedView mappedView = gpuBuf.map(false, true)) {
			MemoryUtil.memCopy(mesh.vertexBuffer(), mappedView.data());
		}

		RenderSystem.AutoStorageIndexBuffer seqBuf = RenderSystem.getSequentialBuffer(drawMode);
		GpuBuffer indices = seqBuf.getBuffer(drawParams.indexCount());
		IndexType indexType = seqBuf.type();

		Minecraft client = Minecraft.getInstance();
		AbstractTexture tex = client.getTextureManager().getTexture(Util.getWhiteTextureId());
		try (RenderPass rp = RenderSystem.getDevice().createCommandEncoder()
				.createRenderPass(
					() -> "blueprint_grid_render",
					client.gameRenderer.mainRenderTarget().getColorTextureView(),
					Optional.empty(),
					client.gameRenderer.mainRenderTarget().getDepthTextureView(),
					OptionalDouble.empty())) {
			rp.setPipeline(pipeline);
			RenderSystem.bindDefaultUniforms(rp);
			rp.setUniform("DynamicTransforms", dynamicTransforms);
			rp.bindTexture("Sampler0", tex.getTextureView(), tex.getSampler());
			rp.setVertexBuffer(0, gpuBuf.slice());
			rp.setIndexBuffer(indices, indexType);
			rp.drawIndexed(drawParams.indexCount(), 1, 0, 0, 0);
		}

		mesh.close();
		gridVertexBuffer.rotate();
	}

	private void addGridQuad(BufferBuilder buffer, float x0, float y0, float x1, float y1, float shade, float a) {
		buffer.addVertex(x0, y0, 0).setUv(0f, 1f).setColor(shade, shade, shade, a);
		buffer.addVertex(x1, y0, 0).setUv(1f, 1f).setColor(shade, shade, shade, a);
		buffer.addVertex(x1, y1, 0).setUv(1f, 0f).setColor(shade, shade, shade, a);
		buffer.addVertex(x0, y1, 0).setUv(0f, 0f).setColor(shade, shade, shade, a);
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
		if (gridVertexBuffer != null) {
			gridVertexBuffer.close();
			gridVertexBuffer = null;
		}
		if (gridAllocator != null) {
			gridAllocator.close();
			gridAllocator = null;
		}
	}

	/**
	 * Release all GPU resources held by this blueprint: registered textures
	 * (including every GIF frame) and the world-space vertex buffers.
	 * Must be called before dropping a Blueprint, e.g. on filesystem rescan.
	 */
	public void close() {
		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		if (frameTextureIds.isEmpty()) {
			if (textureId != null) {
				textureManager.release(textureId);
			}
		} else {
			frameTextureIds.forEach(textureManager::release);
		}
		texture = null;
		textureId = null;
		frameTextures.clear();
		frameTextureIds.clear();
		closeWorldResources();
	}

	public void renderThumbnail(GuiGraphicsExtractor drawContext, int x, int y, int width, int height, boolean includeFrame) {
		if (texture == null) {
			Main.LOGGER.error("\"{}\" is not a valid image file.", texturePath);
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

	public void nudgeRotation(net.minecraft.core.Direction.Axis axis, float amount, boolean multiply, boolean finetune) {
		TransformUtils.Rotation current = new TransformUtils.Rotation(config.rotationX, config.rotationY, config.rotationZ);
		TransformUtils.Rotation newRotation = TransformUtils.nudgeRotation3D(current, axis, amount, multiply, finetune);

		config.rotationX = newRotation.x;
		config.rotationY = newRotation.y;
		config.rotationZ = newRotation.z;
	}

	public void nudgePosition(Util.Direction direction, float amount, boolean multiply, boolean finetune) {
		TransformUtils.Position current = new TransformUtils.Position(config.positionX, config.positionY, config.positionZ);
		TransformUtils.Position newPosition = TransformUtils.nudgePosition3D(current, direction, amount, multiply, finetune);

		config.positionX = newPosition.x;
		config.positionY = newPosition.y;
		config.positionZ = newPosition.z;
	}

	public void nudgeAlpha(float amount, boolean multiply, boolean finetune) {
		config.alpha = TransformUtils.nudgeAlpha(config.alpha, amount, multiply, finetune);
	}

	public void setPosition(float x, float y, float z) {
		config.positionX = x;
		config.positionY = y;
		config.positionZ = z;
	}

	public void nudgeScale(net.minecraft.core.Direction.Axis axis, float amount, boolean multiply, boolean finetune) {
		TransformUtils.Scale current = new TransformUtils.Scale(config.scaleX, config.scaleY);
		TransformUtils.Scale newScale = TransformUtils.nudgeScale2D(current, axis, amount, multiply, finetune);

		config.scaleX = newScale.x;
		config.scaleY = newScale.y;
	}

	public String getName() {
		return MathUtils.getFilenameFromPath(texturePath);
	}

	public boolean isVisible() {
		return config.visibility;
	}

	public void setVisible(boolean visible) {
		config.visibility = visible;
	}

	public void resetRotation() {
		config.rotationX = 0.0f;
		config.rotationY = 0.0f;
		config.rotationZ = 0.0f;
	}

	public void resetScale() {
		config.scaleX = 1.0f;
		config.scaleY = config.scaleX / aspectRatio;
	}

	public String getFilename() {
		return texturePath;
	}

	public float getPosX() {
		return config.positionX;
	}

	public float getPosY() {
		return config.positionY;
	}

	public float getPosZ() {
		return config.positionZ;
	}

	public float getRotationX() {
		return config.rotationX;
	}

	public float getRotationY() {
		return config.rotationY;
	}

	public float getRotationZ() {
		return config.rotationZ;
	}

	public float getScaleX() {
		return config.scaleX;
	}

	public float getScaleY() {
		return config.scaleY;
	}

	public float getAlpha() {
		return config.alpha;
	}

	public void setPosX(float f) {
		config.positionX = f;
	}

	public void setPosY(float f) {
		config.positionY = f;
	}

	public void setPosZ(float f) {
		config.positionZ = f;
	}

	public void setRotationX(float f) {
		config.rotationX = f;
	}

	public void setRotationY(float f) {
		config.rotationY = f;
	}

	public void setRotationZ(float f) {
		config.rotationZ = f;
	}

	public void setScaleX(float f) {
		config.scaleX = f;
	}

	public void setScaleY(float f) {
		config.scaleY = f;
	}

	public void setAlpha(float f) {
		config.alpha = f;
	}

	public boolean isFlippedHorizontally() {
		return config.flipHorizontal;
	}

	public void toggleFlipHorizontal() {
		config.flipHorizontal = !config.flipHorizontal;
	}

	public boolean isFlippedVertically() {
		return config.flipVertical;
	}

	public void toggleFlipVertical() {
		config.flipVertical = !config.flipVertical;
	}

	public int getGridMode() {
		return config.gridMode;
	}

	public void cycleGridMode() {
		config.gridMode = (config.gridMode + 1) % 3;
	}

	public float getGridSpacing() {
		return config.gridSpacing;
	}

	public void setGridSpacing(float f) {
		config.gridSpacing = Math.max(0.1f, f);
	}

	/**
	 * Calculate the distance from the camera to this blueprint
	 * @param camera The camera to calculate distance from
	 * @return The distance in blocks
	 */
	public double getDistanceFromCamera(Camera camera) {
		Vec3 cameraPos = camera.position();
		Vec3 blueprintPos = new Vec3(config.positionX, config.positionY, config.positionZ);
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
