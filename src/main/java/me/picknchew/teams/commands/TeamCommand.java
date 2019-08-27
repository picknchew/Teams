package me.picknchew.teams.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.contexts.OnlinePlayer;
import com.google.common.collect.ImmutableMap;
import me.picknchew.teams.Team;
import me.picknchew.teams.TeamHandler;
import me.picknchew.teams.Teams;
import me.picknchew.teams.messages.Message;
import me.picknchew.teams.players.TeamPlayer;
import me.picknchew.teams.players.TeamPlayers;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.stream.Collectors;

@CommandAlias("team")
public class TeamCommand extends BaseCommand {
	private static final TeamPlayers TEAM_PLAYERS = Teams.getTeamPlayers();
	private static final TeamHandler TEAM_HANDLER = Teams.getTeamHandler();

	private final int characterLimit;
	private final Set<String> blacklist;

	public TeamCommand(FileConfiguration config) {
		characterLimit = config.getInt("character_limit");
		blacklist = config.getStringList("blacklist").stream().map(String::toLowerCase).collect(Collectors.toSet());
	}

	@Default
	public void onDefault(CommandSender sender) {
		Message.HELP.send(sender);
	}

	@Subcommand("create")
	public void onCreateTeam(Player player, String teamName) {
		TeamPlayer teamPlayer = TEAM_PLAYERS.getPlayer(player);

		if (teamPlayer.getTeam() != null) {
			Message.ALREADY_IN_TEAM.send(player);
		}

		if (blacklist.contains(teamName)) {
			Message.BLACKLISTED.send(player);
			return;
		}

		if (teamName.length() > characterLimit) {
			Message.EXCEEDS_LIMIT.send(player);
			return;
		}

		if (TEAM_HANDLER.getTeamByName(teamName) == null) {
			TEAM_HANDLER.createNewTeam(teamName, teamPlayer);

			Message.CREATE_TEAM.send(player, ImmutableMap.<String, String>builder().put("%team%", teamName).build());
			return;
		}

		Message.TEAM_ALREADY_EXISTS.send(player);
	}

	@Subcommand("disband")
	public void onDisbandTeam(Player player, @Optional Team team) {
		TeamPlayer teamPlayer = TEAM_PLAYERS.getPlayer(player);

		if (team != null && player.hasPermission("teams.disband.others")) {
			TEAM_HANDLER.disband(team);
			Message.ADMIN_DISBAND.send(player, ImmutableMap.<String, String>builder().put("%team%", team.getName()).build());
			return;
		}

		if ((team = teamPlayer.getTeam()) != null) {
			TEAM_HANDLER.disband(team);
			Message.DISBAND_TEAM.send(player);
			return;
		}

		Message.NO_TEAM.send(player);
	}

	@Subcommand("leave")
	public void onLeave(Player player) {
		TeamPlayer teamPlayer = TEAM_PLAYERS.getPlayer(player);
		Team team = teamPlayer.getTeam();

		if (team != null) {
			if (teamPlayer == team.getOwner()) {
				TEAM_HANDLER.disband(team);
				Message.LEAVE_TEAM.send(player);
				Message.DISBAND_TEAM.send(player);
				return;
			}

			teamPlayer.resetTeamData();
			team.removePlayer(teamPlayer);

			TEAM_PLAYERS.saveToDisk(teamPlayer);
			TEAM_HANDLER.saveToDisk(team);

			Message.PLAYER_LEFT.send(team.getMembers(), ImmutableMap.<String, String>builder().put("%player%", player.getName()).build());
			Message.LEAVE_TEAM.send(player);

			return;
		}

		Message.NO_TEAM.send(player);
	}

	@Subcommand("info")
	public void showTeamInfo(Player player, @Optional Team team) {
		TeamPlayer teamPlayer = TEAM_PLAYERS.getPlayer(player);

		if (team == null) {
			team = teamPlayer.getTeam();
		}

		if (team != null) {
			Set<TeamPlayer> membersOnline = team.getOnlineMembers();

			Message.TEAM_INFO.send(player, ImmutableMap.<String, String>builder()
					.put("%team%", team.getName())
					.put("%owner%", team.getOwner().getName())
					.put("%amount_online%", Integer.toString(membersOnline.size()))
					.put("%total_members%", Integer.toString(team.getMembers().size()))
					.put("%max_members%", Integer.toString(team.getOwner().getMaxMembers()))
					.put("%kills%", Integer.toString(team.getKills())) // TODO
					.put("%deaths%", Integer.toString(team.getDeaths()))
					.put("%kdr%", team.getKillDeathRatio())
					.put("%members_online%", String.join(", ", membersOnline.stream().map(TeamPlayer::getName).collect(Collectors.toSet())))
					.build());
			return;
		}

		Message.NO_TEAM.send(player);
	}

	@Subcommand("chat")
	public void toggleChat(Player player) {
		TeamPlayer teamPlayer = TEAM_PLAYERS.getPlayer(player);

		if (teamPlayer.getTeam() == null) {
			Message.NO_TEAM.send(player);
			return;
		}

		if (teamPlayer.toggleTeamChat()) {
			Message.TEAM_CHAT.send(player);
			return;
		}

		Message.ALL_CHAT.send(player);
	}

	@Subcommand("invite")
	public void onInvite(Player player, OnlinePlayer targetPlayer) {
		TeamPlayer teamPlayer = TEAM_PLAYERS.getPlayer(player);
		Team team = teamPlayer.getTeam();

		if (team == null) {
			Message.NO_TEAM.send(player);
			return;
		}

		if (team.getOwner() != teamPlayer) {
			Message.NOT_OWNER.send(player);
			return;
		}

		if (targetPlayer == null) {
			Message.PLAYER_NOT_FOUND.send(player);
			return;
		}

		if (targetPlayer.getPlayer() == player) {
			Message.CANNOT_INVITE_SELF.send(player);
		}

		if (team.getMembers().contains(TEAM_PLAYERS.getPlayer(targetPlayer.getPlayer()))) {
			return;
		}

		team.invitePlayer(TEAM_PLAYERS.getPlayer(targetPlayer.getPlayer()));

		Message.INVITE_PLAYER.send(player, ImmutableMap.<String, String>builder().put("%player%", targetPlayer.getPlayer().getName()).build());
		Message.PLAYER_INVITED.send(targetPlayer.getPlayer(), ImmutableMap.<String, String>builder().put("%team%", team.getName()).build());
	}

	@Subcommand("accept")
	public void onInviteAccept(Player player, Team team) {
		TeamPlayer teamPlayer = TEAM_PLAYERS.getPlayer(player);

		if (team == null) {
			Message.TEAM_DOES_NOT_EXIST.send(player);
			return;
		}

		if (team.getMembers().size() == team.getOwner().getMaxMembers()) {
			Message.TEAM_FULL.send(player);
			return;
		}

		if (team.addPlayer(teamPlayer)) {
			teamPlayer.setTeam(team);

			Message.JOIN_TEAM.send(player, ImmutableMap.<String, String>builder().put("%team%", team.getName()).build());
			Message.PLAYER_JOINED.send(team.getMembers(), ImmutableMap.<String, String>builder().put("%player%", player.getName()).build());

			TEAM_HANDLER.saveToDisk(team);
			TEAM_PLAYERS.saveToDisk(teamPlayer);

			return;
		}

		Message.NOT_INVITED.send(player);
	}

	@Subcommand("sethome")
	public void onSethome(Player player, @Optional @Single String homeName) {
		TeamPlayer teamPlayer = TEAM_PLAYERS.getPlayer(player);
		Team team = teamPlayer.getTeam();

		if (team == null) {
			Message.NO_TEAM.send(player);
			return;
		}

		if (team.getOwner() != teamPlayer) {
			Message.NOT_OWNER.send(player);
			return;
		}

		if ((team.getOwner().getMaxHomes() == team.getHomes().size()) && !team.getHomes().containsKey(homeName.toLowerCase())) {
			Message.HOME_LIMIT.send(player);
		}

		Message.SET_HOME.send(player);

		if (homeName == null) {
			team.setHome(homeName, player.getLocation());
			TEAM_HANDLER.saveToDisk(team);
			return;
		}

		team.setHome("home", player.getLocation());
		TEAM_HANDLER.saveToDisk(team);
	}

	@Subcommand("home")
	public void onHome(Player player, @Optional @Single String homeName) {
		TeamPlayer teamPlayer = TEAM_PLAYERS.getPlayer(player);
		Team team = teamPlayer.getTeam();

		if (team == null) {
			Message.NO_TEAM.send(player);
			return;
		}

		Set<String> homes = team.getHomes().keySet();

		if (homes.size() == 0) {
			Message.NO_HOMES.send(player);
			return;
		}

		if (homeName == null && homes.size() > 1) {
			Message.LIST_HOMES.send(player, ImmutableMap.<String, String>builder().put("%homes%", String.join(", ", homes)).build());
			return;
		}

		Location home = team.getHome(homeName);

		if (home == null) {
			Message.HOME_NOT_FOUND.send(player);
			return;
		}

		player.teleport(home);
	}

	@Subcommand("kick")
	public void onKick(Player player, TeamPlayer targetPlayer) {
		TeamPlayer teamPlayer = TEAM_PLAYERS.getPlayer(player);
		Team team = teamPlayer.getTeam();

		if (team == null) {
			Message.NO_TEAM.send(player);
			return;
		}

		if (teamPlayer != team.getOwner()) {
			Message.NOT_OWNER.send(player);
		}

		if (targetPlayer == null) {
			Message.PLAYER_NOT_FOUND.send(player);
			return;
		}

		if (targetPlayer.getTeam() != team) {
			Message.PLAYER_NOT_IN_TEAM.send(player);
			return;
		}

		if (targetPlayer == team.getOwner()) {
			Message.CANNOT_KICK_SELF.send(player);
			return;
		}

		if (targetPlayer.isOnline()) {
			Message.PLAYER_KICKED.send(player);
		}

		team.removePlayer(targetPlayer);
		targetPlayer.resetTeamData();

		TEAM_HANDLER.saveToDisk(team);
		TEAM_PLAYERS.saveToDisk(targetPlayer);

		Message.TEAM_KICKED.send(team.getMembers(), ImmutableMap.<String, String>builder().put("%player%", targetPlayer.getName()).build());
	}

	@Subcommand("reload")
	public void onReload(CommandSender sender) {
		if (sender.hasPermission("teams.reload")) {
			Teams.reload();
			Message.RELOAD.send(sender);
		}

		onDefault(sender);
	}
}
