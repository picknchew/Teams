package me.picknchew.teams.players;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.picknchew.teams.Team;
import me.picknchew.teams.Teams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TeamPlayer {
	private volatile int teamId;

	private volatile String name;
	private final UUID uuid;

	private final AtomicInteger maxMembers, maxHomes;
	private final AtomicBoolean teamChat = new AtomicBoolean(false);

	public TeamPlayer(String name, UUID uuid) {
		this(name, uuid, 0, 0);
	}

	public TeamPlayer(String name, UUID uuid, int maxMembers, int maxHomes) {
		this(name, uuid, maxMembers, maxHomes, 0);
	}

	public TeamPlayer(String name, UUID uuid, int maxMembers, int maxHomes, int teamId) {
		this.name = name;
		this.uuid = uuid;
		this.maxMembers = new AtomicInteger(maxMembers);
		this.maxHomes = new AtomicInteger(maxHomes);
		this.teamId = teamId;
	}

	public boolean isOnline() {
		return Bukkit.getPlayer(uuid) != null;
	}

	public Team getTeam() {
		return Teams.getTeamHandler().getTeamById(teamId);
	}

	public void setTeam(Team team) {
		teamId = team.getId();
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public boolean toggleTeamChat() {
		boolean value = !teamChat.get();
		teamChat.set(value);
		return value;
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}

	public boolean inTeamChat() {
		return teamChat.get();
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMaxMembers() {
		return maxMembers.get();
	}

	public int getMaxHomes() {
		return maxHomes.get();
	}

	public void setMaxMembers(int value) {
		maxMembers.set(value);
	}

	public void setMaxHomes(int value) {
		maxHomes.set(value);
	}

	public void resetTeamData() {
		teamId = 0;

		toggleTeamChat();
	}

	public static class TeamPlayerAdapter extends TypeAdapter<TeamPlayer> {

		@Override
		public void write(JsonWriter out, TeamPlayer value) throws IOException {
			out.beginObject();

			out.name("team").value(value.teamId);
			out.name("name").value(value.name);
			out.name("uuid").value(value.uuid.toString());
			out.name("max_members").value(value.maxMembers);
			out.name("max_homes").value(value.maxHomes);

			out.endObject();

			out.close();
		}

		@Override
		public TeamPlayer read(JsonReader in) throws IOException {
			in.beginObject();

			in.nextName();
			int teamId = in.nextInt();
			in.nextName();
			String name = in.nextString();
			in.nextName();
			UUID uuid = UUID.fromString(in.nextString());
			in.nextName();
			int maxMembers = in.nextInt();
			in.nextName();
			int maxHomes = in.nextInt();

			in.endObject();

			return new TeamPlayer(name, uuid, maxMembers, maxHomes, teamId);
		}
	}
}
