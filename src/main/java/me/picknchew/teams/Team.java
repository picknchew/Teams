package me.picknchew.teams;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.picknchew.teams.players.TeamPlayer;
import me.picknchew.teams.utilities.FileUtils;
import org.bukkit.Location;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Team {
	private static final DecimalFormat TWO_DECIMAL_FORMAT = new DecimalFormat("#.##");

	private final int id;
	private final String name;

	private final TeamPlayer owner;
	private final Set<TeamPlayer> members;
	private final Set<TeamPlayer> invites = new HashSet<>();

	private final Map<String, Location> homes;
	private final AtomicInteger kills, deaths;

	public Team(int id, String name, TeamPlayer owner) {
		this(id, name, owner, new HashSet<>(), new HashMap<>(), 0, 0);
	}

	public Team(int id, String name, TeamPlayer owner, Set<TeamPlayer> members, Map<String, Location> homes, int kills, int deaths) {
		this.id = id;
		this.name = name;
		this.members = members;
		this.homes = homes;
		this.kills = new AtomicInteger(kills);
		this.deaths = new AtomicInteger(deaths);

		members.add(this.owner = owner);
	}

	public TeamPlayer getOwner() {
		return owner;
	}

	public void invitePlayer(TeamPlayer player) {
		invites.add(player);
	}

	public Set<TeamPlayer> getMembers() {
		return members;
	}

	public boolean addPlayer(TeamPlayer player) {
		if (invites.remove(player)) {
			members.add(player);

			return true;
		}

		return false;
	}

	public void incrementKills() {
		kills.incrementAndGet();
	}

	public void incrementDeaths() {
		deaths.incrementAndGet();
	}

	public int getKills() {
		return kills.get();
	}

	public int getDeaths() {
		return deaths.get();
	}

	public String getKillDeathRatio() {
		return TWO_DECIMAL_FORMAT.format((double) getKills() / (double) getDeaths());
	}

	public void removePlayer(TeamPlayer player) {
		members.remove(player);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Location getHome(String homeName) {
		if (homes.containsKey(homeName)) {
			return homes.get(homeName);
		}

		if (homes.containsKey("home")) {
			return homes.get("home");
		}

		return null;
	}

	public void setHome(String homeName, Location location) {
		homes.put(homeName.toLowerCase(), location);
	}

	public Map<String, Location> getHomes() {
		return homes;
	}

	public Set<TeamPlayer> getOnlineMembers() {
		return members.stream().filter(TeamPlayer::isOnline).collect(Collectors.toSet());
	}

	public static class TeamAdapter extends TypeAdapter<Team> {

		@Override
		public void write(JsonWriter out, Team value) throws IOException {
			out.beginObject();

			out.name("id").value(value.id);
			out.name("name").value(value.name);
			out.name("owner").value(value.owner.getUniqueId().toString());
			out.name("kills").value(value.kills.get());
			out.name("deaths").value(value.deaths.get());
			out.name("members");

			// members
			out.beginArray();

			for (TeamPlayer teamPlayer : value.members) {
				out.value(teamPlayer.getUniqueId().toString());
			}

			out.endArray();

			out.name("homes");
			// homes
			out.beginObject();

			for (Map.Entry<String, Location> home : value.homes.entrySet()) {
				out.name(home.getKey()).value(FileUtils.serializeLocation(home.getValue()));
			}

			out.endObject();

			out.endObject();

			out.close();
		}

		@Override
		public Team read(JsonReader in) throws IOException {
			in.beginObject();

			in.nextName();
			int id = in.nextInt();
			in.nextName();
			String name = in.nextString();
			in.nextName();
			TeamPlayer owner = Teams.getTeamPlayers().getPlayer(UUID.fromString(in.nextString()));
			in.nextName();
			int kills = in.nextInt();
			in.nextName();
			int deaths = in.nextInt();
			in.nextName();
			Set<TeamPlayer> members = new HashSet<>();
			Map<String, Location> homes = new HashMap<>();

			// members
			in.beginArray();

			while (in.hasNext()) {
				members.add(Teams.getTeamPlayers().getPlayer(UUID.fromString(in.nextString())));
			}

			in.endArray();

			in.nextName();

			// homes
			in.beginObject();

			while (in.hasNext()) {
				homes.put(in.nextName(), FileUtils.deserializeLocation(in.nextString()));
			}

			in.endObject();

			in.endObject();

			return new Team(id, name, owner, members, homes, kills, deaths);
		}
	}
}
