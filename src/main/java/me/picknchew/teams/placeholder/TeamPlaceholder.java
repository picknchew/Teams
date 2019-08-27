package me.picknchew.teams.placeholder;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import me.picknchew.teams.Teams;
import me.picknchew.teams.players.TeamPlayer;
import me.picknchew.teams.players.TeamPlayers;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class TeamPlaceholder extends EZPlaceholderHook {
	private final TeamPlayers players;
	private final FileConfiguration config;

	public TeamPlaceholder(Teams instance) {
		super(instance, "teams");
		players = Teams.getTeamPlayers();
		config = instance.getConfig();
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		if (player == null) {
			return null;
		}

		if (identifier.equals("team")) {
			TeamPlayer teamPlayer = players.getPlayer(player);

			if (teamPlayer.getTeam() != null) {
				return ChatColor.translateAlternateColorCodes('&', config.getString("placeholder")).replace("%team%", teamPlayer.getTeam().getName());
			}

			return "";
		}

		if (identifier.equals("team_scoreboard")) {
			TeamPlayer teamPlayer = players.getPlayer(player);

			if (teamPlayer.getTeam() != null) {
				return teamPlayer.getTeam().getName();
			}

			return "";
		}

		return null;
	}
}
