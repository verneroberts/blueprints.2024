package vernando.blueprints;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public class Util {

	public static String getPerWorldDimensionConfigPath() {
		String worldString = getWorldOrServerName();
		String dimension = getDimensionName();
		worldString = worldString.replace(":", "_").trim();
		String path = "config/" + Main.MOD_ID + "/" + worldString + "/" + dimension;
		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return path;
	}

	public static String getConfigPath() {
		String path = "config/" + Main.MOD_ID;
		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return path;
	}

	private static float fixYaw(float yaw) {
		// no idea why, but yaw seems to just grow when the player spins in the same
		// direction
		return MathUtils.normalizeYaw(yaw);
	}

	public static Direction playerFacingDirection(boolean usePitch) {
		Minecraft client = Minecraft.getInstance();

		if (usePitch) {
			float pitch = client.getCameraEntity().getXRot();
			if (pitch >= 45) {
				return Direction.UP;
			}
			if (pitch <= -45) {
				return Direction.DOWN;
			}
		}

		float yaw = client.getCameraEntity().getYRot();
		yaw = fixYaw(yaw);
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

	enum Direction {
		NORTH, EAST, SOUTH, WEST, UP, DOWN
	}

	public static boolean isShiftDown() {
		Window window = Minecraft.getInstance().getWindow();
		return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
				|| InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
	}

	public static boolean isCtrlDown() {
		Window window = Minecraft.getInstance().getWindow();
		return InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL)
				|| InputConstants.isKeyDown(window, InputConstants.KEY_RCONTROL);
	}

	public static String getWorldOrServerName() {
		try {
			Minecraft client = Minecraft.getInstance();
			if (client.isLocalServer()) {
				if (client.getSingleplayerServer() == null) {
					return "singleplayer";
				}
				if (client.getSingleplayerServer().getWorldData() == null) {
					return "singleplayer";
				}
				return client.getSingleplayerServer().getWorldData().getLevelName();
			} else {
				return client.getCurrentServer().ip;
			}
		} catch (Exception e) {
			return "default";
		}
	}

	public static String getDimensionName() {
		try {
			Minecraft client = Minecraft.getInstance();
			if (client.level == null) {
				return "null";
			}
			return client.level.dimension().identifier().getPath();
		} catch (Exception e) {
			return "default";
		}
	}


	private static NativeImage loadAsPng(String texturePath) {
		try {
			// NativeImage can only decode PNG; convert anything else in memory first.
			if (!texturePath.toLowerCase().endsWith(".png")) {
				Main.LOGGER.info("Converting image to png: {}", texturePath);
				BufferedImage image = ImageIO.read(new File(texturePath));
				if (image == null) {
					Main.LOGGER.error("No installed image reader can decode: {}", texturePath);
					return null;
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image, "png", baos);
				return NativeImage.read(new ByteArrayInputStream(baos.toByteArray()));
			}
			try (FileInputStream in = new FileInputStream(texturePath)) {
				return NativeImage.read(in);
			}
		} catch (Exception e) {
			Main.LOGGER.error("Failed to load image: {}", texturePath, e);
			return null;
		}
	}

	public static DynamicTexture registerTexture(String texturePath, Identifier textureId) {
		try {
			Main.LOGGER.info("Loading image: {} as {}", texturePath, textureId);

			NativeImage image = loadAsPng(texturePath);
			if (image == null) {
				Main.LOGGER.error("Failed to load image: {}", texturePath);
				return null;
			}
			DynamicTexture texture = new DynamicTexture(() -> texturePath, image);
			Minecraft.getInstance().getTextureManager().register(textureId, texture);
			return texture;
		} catch (Exception e) {
			Main.LOGGER.error("Failed to load image: {}", texturePath, e);
			return null;
		}
	}

	private static Identifier whiteTextureId;

	/**
	 * A 1x1 white texture, registered on first use. Lets untextured geometry (grid
	 * lines) run through the same textured pipeline using plain vertex colors.
	 */
	public static Identifier getWhiteTextureId() {
		if (whiteTextureId == null) {
			Identifier id = Identifier.fromNamespaceAndPath(Main.MOD_ID, "white_pixel");
			NativeImage image = new NativeImage(1, 1, false);
			image.setPixelABGR(0, 0, 0xFFFFFFFF);
			DynamicTexture texture = new DynamicTexture(() -> "blueprints white pixel", image);
			Minecraft.getInstance().getTextureManager().register(id, texture);
			whiteTextureId = id;
		}
		return whiteTextureId;
	}

	/**
	 * Copy dropped image files into the current world/dimension blueprint folder.
	 * Existing files are never overwritten; a numeric suffix is added instead.
	 * @return the number of files imported
	 */
	public static int importImageFiles(List<Path> paths) {
		String targetDir = getPerWorldDimensionConfigPath();
		int imported = 0;
		for (Path source : paths) {
			try {
				File sourceFile = source.toFile();
				if (!sourceFile.isFile() || !BlueprintsManager.isImageFile(sourceFile.getName())) {
					Main.LOGGER.warn("Skipping non-image file: {}", source);
					continue;
				}
				String name = sourceFile.getName();
				String base = name;
				String extension = "";
				int dot = name.lastIndexOf('.');
				if (dot > 0) {
					base = name.substring(0, dot);
					extension = name.substring(dot);
				}
				File target = new File(targetDir, name);
				int suffix = 1;
				while (target.exists()) {
					target = new File(targetDir, base + "_" + suffix + extension);
					suffix++;
				}
				java.nio.file.Files.copy(source, target.toPath());
				Main.LOGGER.info("Imported image: {} -> {}", source, target.getPath());
				imported++;
			} catch (Exception e) {
				Main.LOGGER.error("Failed to import image: {}", source, e);
			}
		}
		return imported;
	}

    public static void openFolder(String folder) {
		Minecraft client = Minecraft.getInstance();
		String basePath = client.gameDirectory.getAbsolutePath();
		Path path = Path.of(basePath, folder);
		String folderPath = path.toString();
		Main.LOGGER.info("opening folder: {}", folderPath);
		String os = System.getProperty("os.name").toLowerCase();
		try {

			if (os.contains("win")) {
				new ProcessBuilder("explorer.exe", folderPath).start();
			} else if (os.contains("nix") || os.contains("nux")) {
				new ProcessBuilder("xdg-open", folderPath).start();
			} else if (os.contains("mac")) {
				new ProcessBuilder("open", folderPath).start();
			} else {
				Main.LOGGER.error("Unknown OS: {}", os);
			}

		} catch (Exception e) {
			Main.LOGGER.error("Failed to open folder: {} on {}", folderPath, os, e);
		}
    }

	// GIF animation support
	public static class GifFrame {
		public NativeImage image;
		public int delayMs;

		public GifFrame(NativeImage image, int delayMs) {
			this.image = image;
			this.delayMs = delayMs;
		}
	}

	public static class GifAnimation {
		public List<GifFrame> frames;
		public boolean isAnimated;

		public GifAnimation() {
			this.frames = new ArrayList<>();
			this.isAnimated = false;
		}
	}

	public static boolean isGifFile(String texturePath) {
		return texturePath.toLowerCase().endsWith(".gif");
	}

	public static GifAnimation loadGif(String texturePath) {
		GifAnimation animation = new GifAnimation();
		File file = new File(texturePath);

		try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
			ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
			try {
				reader.setInput(iis, false);
				readGifFrames(reader, animation, texturePath);
			} finally {
				reader.dispose();
			}
		} catch (Exception e) {
			Main.LOGGER.error("Failed to load GIF: {}", texturePath, e);

			// Fallback: try to load as static image
			NativeImage staticImage = loadAsPng(texturePath);
			if (staticImage != null) {
				animation.frames.add(new GifFrame(staticImage, 1000));
			}
		}

		return animation;
	}

	private static void readGifFrames(ImageReader reader, GifAnimation animation, String texturePath) throws Exception {
		int numFrames = reader.getNumImages(true);
		animation.isAnimated = numFrames > 1;

		Main.LOGGER.info("Loading GIF with {} frames: {}", numFrames, texturePath);

		// Get the logical screen size and background color from GIF stream metadata
		int canvasWidth, canvasHeight;
		int backgroundColor = 0x00000000; // Default to transparent

		try {
			IIOMetadata streamMetadata = reader.getStreamMetadata();
			if (streamMetadata != null) {
				String metaFormatName = streamMetadata.getNativeMetadataFormatName();
				IIOMetadataNode root = (IIOMetadataNode) streamMetadata.getAsTree(metaFormatName);

				// Get logical screen descriptor
				IIOMetadataNode screenDescriptor = getNode(root, "LogicalScreenDescriptor");
				if (screenDescriptor != null) {
					String widthAttr = screenDescriptor.getAttribute("logicalScreenWidth");
					String heightAttr = screenDescriptor.getAttribute("logicalScreenHeight");
					canvasWidth = Integer.parseInt(widthAttr);
					canvasHeight = Integer.parseInt(heightAttr);

					Main.LOGGER.debug("GIF logical screen size: {}x{}", canvasWidth, canvasHeight);
				} else {
					// Fallback to first frame size
					BufferedImage firstFrame = reader.read(0);
					canvasWidth = firstFrame.getWidth();
					canvasHeight = firstFrame.getHeight();
					Main.LOGGER.warn("Could not read GIF logical screen size, using first frame: {}x{}", canvasWidth, canvasHeight);
				}
			} else {
				// Fallback to first frame size
				BufferedImage firstFrame = reader.read(0);
				canvasWidth = firstFrame.getWidth();
				canvasHeight = firstFrame.getHeight();
				Main.LOGGER.warn("No stream metadata available, using first frame size: {}x{}", canvasWidth, canvasHeight);
			}
		} catch (Exception e) {
			// Fallback to first frame size
			BufferedImage firstFrame = reader.read(0);
			canvasWidth = firstFrame.getWidth();
			canvasHeight = firstFrame.getHeight();
			Main.LOGGER.warn("Failed to read GIF logical screen size, using first frame: {}x{}", canvasWidth, canvasHeight, e);
		}

		// Create a canvas to composite frames onto
		BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D canvasGraphics = canvas.createGraphics();

		// Initialize canvas with background color (usually transparent for web GIFs)
		canvasGraphics.setComposite(AlphaComposite.Src);
		canvasGraphics.setColor(new java.awt.Color(backgroundColor, true));
		canvasGraphics.fillRect(0, 0, canvasWidth, canvasHeight);

		// Track disposal info for the current frame (to be applied after it's shown)
		String currentDisposalMethod = "none";
		int currentFrameX = 0, currentFrameY = 0, currentFrameWidth = 0, currentFrameHeight = 0;

		for (int i = 0; i < numFrames; i++) {
			BufferedImage frameImage = reader.read(i);

			// Apply disposal method from the PREVIOUS frame (after it was shown)
			if (i > 0) {
				switch (currentDisposalMethod) {
					case "restoreToBackgroundColor":
					case "2":
						// Clear the area where the previous frame was drawn
						canvasGraphics.setComposite(AlphaComposite.Src);
						canvasGraphics.setColor(new java.awt.Color(backgroundColor, true));
						canvasGraphics.fillRect(currentFrameX, currentFrameY, currentFrameWidth, currentFrameHeight);
						break;
					case "restoreToPrevious":
					case "3":
						// This would require saving the canvas state before drawing the previous frame
						// For now, treat as "none" but log it
						Main.LOGGER.debug("Frame {}: restoreToPrevious disposal not fully implemented, treating as none", i - 1);
						break;
					case "none":
					case "doNotDispose":
					case "1":
					default:
						// Leave canvas as-is (most common case)
						break;
				}
			}

			// Get frame metadata for disposal method and positioning
			IIOMetadata metadata = reader.getImageMetadata(i);
			String disposalMethod = "none";
			int frameX = 0, frameY = 0;
			int delayMs = 100; // Default delay

			try {
				String metaFormatName = metadata.getNativeMetadataFormatName();
				IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

				// Get graphic control extension for disposal method and delay
				IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
				if (graphicsControlExtensionNode != null) {
					String delayTime = graphicsControlExtensionNode.getAttribute("delayTime");
					if (delayTime != null && !delayTime.isEmpty()) {
						delayMs = Integer.parseInt(delayTime) * 10; // Convert from 1/100s to ms
						if (delayMs < 20) delayMs = 100; // Minimum reasonable delay
					}

					String disposal = graphicsControlExtensionNode.getAttribute("disposalMethod");
					if (disposal != null && !disposal.isEmpty()) {
						disposalMethod = disposal;
					}
				}

				// Get image descriptor for positioning
				IIOMetadataNode imageDescriptorNode = getNode(root, "ImageDescriptor");
				if (imageDescriptorNode != null) {
					String leftPos = imageDescriptorNode.getAttribute("imageLeft");
					String topPos = imageDescriptorNode.getAttribute("imageTop");
					if (leftPos != null && !leftPos.isEmpty()) {
						frameX = Integer.parseInt(leftPos);
					}
					if (topPos != null && !topPos.isEmpty()) {
						frameY = Integer.parseInt(topPos);
					}
				}
			} catch (Exception e) {
				Main.LOGGER.warn("Failed to read frame metadata for frame {}, using defaults", i, e);
			}

			// Draw the current frame onto the canvas at the specified position
			canvasGraphics.setComposite(AlphaComposite.SrcOver);
			canvasGraphics.drawImage(frameImage, frameX, frameY, null);

			// Create a copy of the current canvas state for this frame
			BufferedImage compositeFrame = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D frameGraphics = compositeFrame.createGraphics();
			frameGraphics.setComposite(AlphaComposite.Src);
			frameGraphics.drawImage(canvas, 0, 0, null);
			frameGraphics.dispose();

			// Convert the composite frame to NativeImage
			NativeImage nativeImage = convertBufferedImageToNativeImage(compositeFrame);
			animation.frames.add(new GifFrame(nativeImage, delayMs));

			// Store current frame info for disposal AFTER this frame is shown
			currentDisposalMethod = disposalMethod;
			currentFrameX = frameX;
			currentFrameY = frameY;
			currentFrameWidth = frameImage.getWidth();
			currentFrameHeight = frameImage.getHeight();
		}

		canvasGraphics.dispose();
	}

	private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
		int nNodes = rootNode.getLength();
		for (int i = 0; i < nNodes; i++) {
			if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
				return ((IIOMetadataNode) rootNode.item(i));
			}
		}
		IIOMetadataNode node = new IIOMetadataNode(nodeName);
		rootNode.appendChild(node);
		return node;
	}

	private static NativeImage convertBufferedImageToNativeImage(BufferedImage bufferedImage) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();

		NativeImage nativeImage = new NativeImage(width, height, false);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgb = bufferedImage.getRGB(x, y);
				// Convert ARGB to ABGR format that NativeImage expects
				int a = (rgb >> 24) & 0xFF;
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = rgb & 0xFF;
				int abgr = (a << 24) | (b << 16) | (g << 8) | r;
				nativeImage.setPixelABGR(x, y, abgr);
			}
		}

		return nativeImage;
	}
}
