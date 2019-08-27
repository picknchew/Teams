package me.picknchew.teams.players;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.picknchew.teams.Teams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeamPlayers {
	private static final Gson gson = new GsonBuilder().registerTypeAdapter(TeamPlayer.class, new TeamPlayer.TeamPlayerAdapter())
			.create();
	private Map<UUID, TeamPlayer> players = new ConcurrentHashMap<>();

	public TeamPlayers() {
		File playersDirectory = new File("plugins" + File.separator + "Teams" + File.separator + "players");

		playersDirectory.mkdirs();

		for (File file : playersDirectory.listFiles()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				TeamPlayer teamPlayer = gson.fromJson(reader, TeamPlayer.class);

				players.put(teamPlayer.getUniqueId(), teamPlayer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public TeamPlayer getPlayer(Player player) {
		return getPlayer(player.getUniqueId());
	}

	public TeamPlayer getPlayer(UUID uuid) {
		return players.get(uuid);
	}

	public TeamPlayer getPlayer(String name) {
		for (TeamPlayer player : players.values()) {
			if (player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}

		return null;
	}

	public TeamPlayer createPlayer(Player player, int maxHomes, int maxMembers) {
		TeamPlayer teamPlayer = new TeamPlayer(player.getName(), player.getUniqueId(), maxMembers, maxHomes);

		players.put(player.getUniqueId(), teamPlayer);

		return teamPlayer;
	}

	public boolean removePlayer(TeamPlayer teamPlayer) {
		if (teamPlayer.getTeam() == null) {
			players.remove(teamPlayer.getUniqueId());

			return true;
		}

		return false;
	}

	public void saveToDisk(TeamPlayer player) {
		Bukkit.getScheduler().runTaskAsynchronously(Teams.getInstance(), () -> {
			File file = new File("plugins" + File.separator + "Teams" + File.separator + "players",
					player.getUniqueId().toString() + ".json");

			if (player.getTeam() != null) {
				try (Writer writer = new FileWriter(file)) {
					gson.toJson(player, writer);
				} catch (IOException e) {
					e.printStackTrace();
				}

				return;
			}

			file.delete();
		});
	}
}
