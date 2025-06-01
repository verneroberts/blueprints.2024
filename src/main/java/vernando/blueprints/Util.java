package vernando.blueprints;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
	public static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

	public static String GetPerWorldDimensionConfigPath() {
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

	public static String GetConfigPath() {
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
		while (yaw < -180) {
			yaw += 360;
		}
		while (yaw > 180) {
			yaw -= 360;
		}
		return yaw;
	}

	public static Direction PlayerFacingDirection(Boolean usePitch) {
		MinecraftClient client = MinecraftClient.getInstance();

		if (usePitch) {
			float pitch = client.getCameraEntity().getPitch();
			if (pitch >= 45) {
				return Direction.UP;
			}
			if (pitch <= -45) {
				return Direction.DOWN;
			}
		}

		float yaw = client.getCameraEntity().getYaw();
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

	public static String getWorldOrServerName() {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.isInSingleplayer()) {
				if (client.getServer() == null) {
					return "singleplayer";
				}
				if (client.getServer().getSaveProperties() == null) {
					return "singleplayer";
				}
				return client.getServer().getSaveProperties().getLevelName();
			} else {
				return client.getCurrentServerEntry().address;
			}
		} catch (Exception e) {
			return "default";
		}
	}

	public static String getDimensionName() {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world == null) {
				return "null";
			}
			if (client.world.getDimensionEntry() == null) {
				return "null";
			}
			return client.world.getDimensionEntry().toString().toString().split(":")[2].split("]")[0];
		} catch (Exception e) {
			return "default";
		}
	}


	private static NativeImage LoadAsPng(String texturePath) {
		try {
			String format = javax.imageio.ImageIO.getReaderFormatNames()[0];
			if (!format.equals("png")) {
				Main.LOGGER.info("Converting image to png: " + texturePath);

				// write to memory and reload as png
				java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
				javax.imageio.ImageIO.write(javax.imageio.ImageIO.read(new File(texturePath)), "png", baos);
				return NativeImage.read(new java.io.ByteArrayInputStream(baos.toByteArray()));
			} else {
				return NativeImage.read(new FileInputStream(texturePath));
			}
		} catch (Exception e) {
			Main.LOGGER.error("Failed to load image: " + texturePath);
			Main.LOGGER.error(e.getMessage());
			return null;
		}
	}

	public static NativeImageBackedTexture RegisterTexture(String texturePath, Identifier textureId) {
		MinecraftClient client = MinecraftClient.getInstance();
		try {
			Main.LOGGER.info("Loading image: " + texturePath + " as " + textureId.toString());

			// create an image input stream and convert to png if needed
			ImageInputStream iis = javax.imageio.ImageIO.createImageInputStream(new File(texturePath));
			if (iis == null) {
				Main.LOGGER.error("Failed to load image: " + texturePath);
				return null;
			}

			NativeImage image = LoadAsPng(texturePath);			
			if (image == null) {
				Main.LOGGER.error("Failed to load image: " + texturePath);
				return null;
			}			
			NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> texturePath, image);
			Main.LOGGER.info("Registering texture: " + textureId);
			client.getTextureManager().registerTexture(textureId, texture);
			return texture;
		} catch (Exception e) {
			Main.LOGGER.error("Failed to load image: " + texturePath);
			Main.LOGGER.error(e.getMessage());
			return null;
		}
	}

    public static void OpenFolder(String folder) {
		// create process to launch explorer.exe with this path
		MinecraftClient client = MinecraftClient.getInstance();
		String basePath = client.runDirectory.getAbsolutePath();		
		Path path = Path.of(basePath, folder);
		String folderPath = path.toString();
		Main.LOGGER.info("opening folder: " + folderPath);
		String os = System.getProperty("os.name").toLowerCase();
		try {								
			
			if (os.contains("win")) {
				ProcessBuilder pb = new ProcessBuilder("explorer.exe", folderPath);
				pb.start();
			} else if (os.contains("nix") || os.contains("nux")) {
				ProcessBuilder pb = new ProcessBuilder("xdg-open", folderPath);
				pb.start();
			} else if (os.contains("mac")) {
				ProcessBuilder pb = new ProcessBuilder("open", folderPath);
				pb.start();
			} else {
				Main.LOGGER.error("Unknown OS: " + os);
			}

		} catch (Exception e) {
			Main.LOGGER.error("Failed to open folder: " + folderPath + " on " + os);
			Main.LOGGER.error(e.getMessage());
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

	public static boolean IsGifFile(String texturePath) {
		return texturePath.toLowerCase().endsWith(".gif");
	}

	public static GifAnimation LoadGif(String texturePath) {
		GifAnimation animation = new GifAnimation();
		
		try {
			File file = new File(texturePath);
			ImageInputStream iis = ImageIO.createImageInputStream(file);
			ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
			reader.setInput(iis, false);
			
			int numFrames = reader.getNumImages(true);
			animation.isAnimated = numFrames > 1;
			
			Main.LOGGER.info("Loading GIF with " + numFrames + " frames: " + texturePath);
			
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
						
						// Try to get background color index
						String bgColorIndex = screenDescriptor.getAttribute("backgroundColorIndex");
						if (bgColorIndex != null && !bgColorIndex.isEmpty()) {
							// Note: In a full implementation, you'd use this index with the global color table
							// For now, we'll keep using transparent background
						}
						
						Main.LOGGER.debug("GIF logical screen size: " + canvasWidth + "x" + canvasHeight);
					} else {
						// Fallback to first frame size
						BufferedImage firstFrame = reader.read(0);
						canvasWidth = firstFrame.getWidth();
						canvasHeight = firstFrame.getHeight();
						Main.LOGGER.warn("Could not read GIF logical screen size, using first frame: " + canvasWidth + "x" + canvasHeight);
					}
				} else {
					// Fallback to first frame size
					BufferedImage firstFrame = reader.read(0);
					canvasWidth = firstFrame.getWidth();
					canvasHeight = firstFrame.getHeight();
					Main.LOGGER.warn("No stream metadata available, using first frame size: " + canvasWidth + "x" + canvasHeight);
				}
			} catch (Exception e) {
				// Fallback to first frame size
				BufferedImage firstFrame = reader.read(0);
				canvasWidth = firstFrame.getWidth();
				canvasHeight = firstFrame.getHeight();
				Main.LOGGER.warn("Failed to read GIF logical screen size, using first frame: " + canvasWidth + "x" + canvasHeight + " - " + e.getMessage());
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
							Main.LOGGER.debug("Frame " + (i-1) + ": restoreToPrevious disposal not fully implemented, treating as none");
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
					Main.LOGGER.warn("Failed to read frame metadata for frame " + i + ", using defaults: " + e.getMessage());
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
				
				Main.LOGGER.debug("Frame " + i + ": disposal=" + disposalMethod + ", delay=" + delayMs + "ms, pos=(" + frameX + "," + frameY + "), size=(" + frameImage.getWidth() + "x" + frameImage.getHeight() + "), canvas=(" + canvasWidth + "x" + canvasHeight + ")");
			}
			
			canvasGraphics.dispose();
			reader.dispose();
			iis.close();
			
		} catch (Exception e) {
			Main.LOGGER.error("Failed to load GIF: " + texturePath);
			Main.LOGGER.error(e.getMessage());
			e.printStackTrace();
			
			// Fallback: try to load as static image
			try {
				NativeImage staticImage = LoadAsPng(texturePath);
				if (staticImage != null) {
					animation.frames.add(new GifFrame(staticImage, 1000));
				}
			} catch (Exception fallbackEx) {
				Main.LOGGER.error("Fallback loading also failed: " + fallbackEx.getMessage());
			}
		}
		
		return animation;
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
				nativeImage.setColor(x, y, abgr);
			}
		}
		
		return nativeImage;
	}
}