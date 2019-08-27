package me.picknchew.teams.messages;

import me.picknchew.teams.players.TeamPlayer;
import me.picknchew.teams.utilities.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public enum Message {
	JOIN_TEAM,
	LEAVE_TEAM,
	INVITE_PLAYER,
	PLAYER_LEFT,
	PLAYER_JOINED,
	CREATE_TEAM,
	DISBAND_TEAM,
	TEAM_INFO,
	PLAYER_INVITED,
	RELOAD,
	HELP,
	SET_HOME,
	CHAT_FORMAT,
	NO_TEAM,
	NO_INVITES,
	ALREADY_IN_TEAM,
	ADMIN_DISBAND,
	TEAM_DOES_NOT_EXIST,
	NOT_INVITED,
	TEAM_DISBANDED,
	TEAM_CHAT,
	ALL_CHAT,
	NOT_OWNER,
	PLAYER_NOT_FOUND,
	NOT_A_PLAYER,
	TEAM_FULL,
	HOME_NOT_FOUND,
	NO_HOMES,
	TEAM_ALREADY_EXISTS,
	CANNOT_INVITE_SELF,
	BLACKLISTED,
	EXCEEDS_LIMIT,
	PLAYER_NOT_IN_TEAM,
	TEAM_KICKED,
	PLAYER_KICKED,
	CANNOT_KICK_SELF,
	HOME_LIMIT,
	LIST_HOMES;

	private static final File file = FileUtils.exportFromJar("messages.yml");
	private static final Map<Message, String> messages = new EnumMap<>(Message.class);

	static {
		reload();
	}

	public void send(CommandSender sender) {
		sender.sendMessage(messages.get(this));
	}

	public void send(Collection<TeamPlayer> players) {
		players.stream().filter(TeamPlayer::isOnline).forEach(player -> {
			send(Bukkit.getPlayer(player.getUniqueId()));
		});
	}

	public void send(Collection<TeamPlayer> players, Map<String, String> replace) {
		players.stream().filter(TeamPlayer::isOnline).forEach(player -> {
			send(Bukkit.getPlayer(player.getUniqueId()), replace);
		});
	}

	public void send(CommandSender sender, Map<String, String> replace) {
		String message = messages.get(this);

		for (Map.Entry<String, String> entry : replace.entrySet()) {
			message = message.replace(entry.getKey(), entry.getValue());
		}

		sender.sendMessage(message);
	}

	@Override
	public String toString() {
		return messages.get(this);
	}

	public static void reload() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		messages.clear();

		for (Message value : values()) {
			messages.put(value, ChatColor.translateAlternateColorCodes('&', String.join("\n", config.getStringList(value.name().toLowerCase()))));
		}
	}
}
