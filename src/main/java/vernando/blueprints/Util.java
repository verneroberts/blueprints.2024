package vernando.blueprints;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;

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

}