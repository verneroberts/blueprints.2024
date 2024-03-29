package vernando.blueprints;

import net.minecraft.client.MinecraftClient;
import java.io.File;
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
			if (client.world.getDimensionKey() == null) {
				return "null";
			}
			return client.world.getDimensionKey().getValue().toString().split(":")[1];
		} catch (Exception e) {
			return "default";
		}
	}

}