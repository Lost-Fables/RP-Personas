package net.korvic.rppersonas;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class BoardManager {

	private static ScoreboardManager manager = Bukkit.getScoreboardManager();
	private static Scoreboard nameBoard = manager.getNewScoreboard();

	private static BukkitRunnable autoClean = null;

	public static void toggleAutoClean() {
		if (autoClean != null) {
			if (autoClean.isCancelled()) {
				newAutoClean();
			} else {
				autoClean.cancel();
			}
		} else {
			newAutoClean();
		}
	}

	private static void newAutoClean() {
		autoClean = new BukkitRunnable() {
			@Override
			public void run() {
				clean();
			}
		};
		startAutoClean();
	}

	private static void startAutoClean() {
		autoClean.runTaskTimer(RPPersonas.get(), 0, 6000); // Auto clean every 5 mins
	}

	public static void clean() {
		for (Team team : nameBoard.getTeams()) {
			if (Bukkit.getPlayer(team.getName()) == null) {
				team.unregister();
			}
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			addPlayer(p);
		}
	}

	public static void addPlayer(Player p) {
		Persona pers = RPPersonas.get().getPersonaHandler().getLoadedPersona(p);
		String[] namePieces = pers.getNamePieces();
		addPlayer(p, namePieces);
	}

	public static void addPlayer(Player p, String[] namePieces) {
		Team team = nameBoard.getTeam(p.getName());
		if (team == null) {
			team = nameBoard.registerNewTeam(p.getName());
		}

		if (namePieces[0] != null) {
			team.setPrefix(namePieces[0]);
		}
		if (namePieces[2] != null) {
			team.setSuffix(namePieces[2]);
		}

		team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
		team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

		team.addEntry(namePieces[1]);
		p.setScoreboard(nameBoard);
	}

}
