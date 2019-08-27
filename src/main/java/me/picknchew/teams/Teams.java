package me.picknchew.teams;

import co.aikar.commands.BukkitCommandManager;
import me.picknchew.teams.commands.TeamCommand;
import me.picknchew.teams.messages.Message;
import me.picknchew.teams.placeholder.TeamPlaceholder;
import me.picknchew.teams.players.TeamPlayer;
import me.picknchew.teams.players.TeamPlayers;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Teams extends JavaPlugin {
	private static Teams INSTANCE;
	private static TeamPlayers TEAM_PLAYERS = new TeamPlayers();
	private static TeamHandler TEAM_HANDLER = new TeamHandler();

	public Teams() {
		INSTANCE = this;
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();

		BukkitCommandManager manager = new BukkitCommandManager(this);

		manager.getCommandContexts().registerContext(TeamPlayer.class, c -> {
			String playerName = c.popFirstArg();
			Player player = getServer().getPlayer(playerName);

			if (player == null) {
				TEAM_PLAYERS.getPlayer(playerName);
			}

			return TEAM_PLAYERS.getPlayer(player);
		});

		manager.getCommandContexts().registerContext(Team.class, c -> TEAM_HANDLER.getTeamByName(c.popFirstArg()));
		getServer().getPluginManager().registerEvents(new PlayerListener(getConfig()), this);
		manager.registerCommand(new TeamCommand(getConfig()));

		new TeamPlaceholder(this).hook();
	}

	public static TeamPlayers getTeamPlayers() {
		return TEAM_PLAYERS;
	}

	public static TeamHandler getTeamHandler() {
		return TEAM_HANDLER;
	}

	public static Teams getInstance() {
		return INSTANCE;
	}

	public static void reload() {
		INSTANCE.reloadConfig();
		Message.reload();
	}
}
