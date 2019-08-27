package me.picknchew.teams.utilities;

import me.picknchew.teams.Teams;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;

public final class FileUtils {

	private FileUtils() {
	}

	public static File createFile(String name) {
		File file = new File("plugins" + File.separator + "Teams", name);

		if (!file.exists() || file.isDirectory()) {
			file.getParentFile().mkdirs();

			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return file;
	}

	public static File exportFromJar(String name) {
		File file = new File("plugins" + File.separator + "Teams", name);

		if (!file.exists() || file.isDirectory()) {
			file.getParentFile().mkdirs();

			Teams.getInstance().saveResource(name, false);
		}

		return file;
	}

	public static String serializeLocation(Location location) {
		return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
	}

	public static Location deserializeLocation(String serialized) {
		String[] split = serialized.split(",");

		World world = Bukkit.getWorld(split[0]);
		double x = Double.parseDouble(split[1]);
		double y = Double.parseDouble(split[2]);
		double z = Double.parseDouble(split[3]);
		float yaw = Float.parseFloat(split[4]);
		float pitch = Float.parseFloat(split[5]);

		return new Location(world, x, y, z, yaw, pitch);
	}
}
