package me.picknchew.teams;

import me.picknchew.teams.messages.Message;
import me.picknchew.teams.players.TeamPlayer;
import me.picknchew.teams.players.TeamPlayers;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.regex.Pattern;

public class PlayerListener implements Listener {
	private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
	private static final TeamPlayers TEAM_PLAYERS = Teams.getTeamPlayers();

	private final int defaultMaxMembers;
	private final int defaultMaxHomes;

	public PlayerListener(FileConfiguration config) {
		defaultMaxMembers = config.getInt("default_max_members");
		defaultMaxHomes = config.getInt("default_max_homes");
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		TeamPlayer teamPlayer = TEAM_PLAYERS.getPlayer(player);

		int maxHomes = getAmountFromPermission(player, "teams.homes.", defaultMaxHomes);
		int maxMembers = getAmountFromPermission(player, "teams.members.", defaultMaxMembers);

		if (teamPlayer == null) {
			TEAM_PLAYERS.createPlayer(player, maxHomes, maxMembers);
			return;
		}

		teamPlayer.setMaxHomes(maxHomes);
		teamPlayer.setMaxMembers(maxMembers);
		teamPlayer.setName(player.getName());
		TEAM_PLAYERS.saveToDisk(teamPlayer);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		TeamPlayer teamPlayer = TEAM_PLAYERS.getPlayer(event.getPlayer());

		if (TEAM_PLAYERS.removePlayer(teamPlayer)) {
			TEAM_PLAYERS.saveToDisk(teamPlayer);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player deadPlayer = event.getEntity();
		Player killer = deadPlayer.getKiller();

		if (killer != null) {
			Team team = TEAM_PLAYERS.getPlayer(killer).getTeam();

			if (team != null) {
				team.incrementKills();
			}
		}

		Team team = TEAM_PLAYERS.getPlayer(deadPlayer).getTeam();

		if (team != null) {
			team.incrementDeaths();
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		TeamPlayer teamPlayer = TEAM_PLAYERS.getPlayer(event.getPlayer());

		if (teamPlayer.inTeamChat()) {
			event.getRecipients().clear();
			teamPlayer.getTeam().getOnlineMembers().forEach(player -> event.getRecipients().add(player.getPlayer()));
			event.setFormat(Message.CHAT_FORMAT.toString().replace("%player%", teamPlayer.getName()).replace("%message%", event.getMessage()));
		}
	}

	private static int getAmountFromPermission(Player player, String permissionPrefix, int defaultValue) {
		for (PermissionAttachmentInfo attachmentInfo : player.getEffectivePermissions()) {
			String permission = attachmentInfo.getPermission();

			if (permission.startsWith(permissionPrefix)) {
				return Integer.parseInt(DOT_PATTERN.split(permission)[2]);
			}
		}

		return defaultValue;
	}
}
