package me.picknchew.teams;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.picknchew.teams.messages.Message;
import me.picknchew.teams.players.TeamPlayer;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TeamHandler {
	private static final Gson gson = new GsonBuilder().registerTypeAdapter(Team.class, new Team.TeamAdapter())
			.create();

	private Map<Integer, Team> teams = new HashMap<>();
	private Map<String, Team> stringIndexedTeams = new HashMap<>();

	private int currentId = 1;

	public TeamHandler() {
		File teamsDirectory = new File("plugins" + File.separator + "Teams" + File.separator + "teams");

		teamsDirectory.mkdir();

		for (File file : teamsDirectory.listFiles()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				Team team = gson.fromJson(reader, Team.class);

				teams.put(team.getId(), team);
				stringIndexedTeams.put(team.getName().toLowerCase(), team);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void createNewTeam(String name, TeamPlayer owner) {
		int id = getNextId();
		Team team = new Team(id, name, owner);

		owner.setTeam(team);

		teams.put(id, team);
		stringIndexedTeams.put(name.toLowerCase(), team);

		Teams.getTeamPlayers().saveToDisk(owner);
		saveToDisk(team);
	}

	public Team getTeamById(int id) {
		return teams.get(id);
	}

	public Team getTeamByName(String name) {
		return stringIndexedTeams.get(name.toLowerCase());
	}

	public void disband(Team team) {
		for (TeamPlayer player : team.getMembers()) {
			player.resetTeamData();
			Teams.getTeamPlayers().saveToDisk(player);

			if (!player.isOnline()) {
				Teams.getTeamPlayers().removePlayer(player);
				return;
			}

			if (player != team.getOwner()) {
				Message.TEAM_DISBANDED.send(player.getPlayer());
			}
		}

		teams.remove(team.getId());
		stringIndexedTeams.remove(team.getName());

		new File("plugins" + File.separator + "Teams" + File.separator + "teams",
				team.getName().toLowerCase() + ".json").delete();
	}

	private int getNextId() {
		while (teams.containsKey(currentId)) {
			currentId += 1;
		}

		return currentId;
	}

	public void saveToDisk(Team team) {
		Bukkit.getScheduler().runTaskAsynchronously(Teams.getInstance(), () -> {
			File file = new File("plugins" + File.separator + "Teams" + File.separator + "teams",
					team.getName().toLowerCase() + ".json");

			try (Writer writer = new FileWriter(file)) {
				gson.toJson(team, writer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
