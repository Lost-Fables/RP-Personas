package net.korvic.rppersonas.players;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RPPlayer {

	////////////////
	//// STATIC ////
	////////////////

	private static Map<Player, RPPlayer> playerMap = new HashMap<>();
	private static List<Player> loadBlocked = new ArrayList<>();

	/**
	 * @param player The player in question.
	 * @return An Account object which represents the given Lost Fables account ID.
	 */
	public static RPPlayer get(Player player) {
		RPPlayer rpp = null;
		if (player != null && !loadBlocked.contains(player)) {
			rpp = playerMap.get(player);
			if (rpp == null) {
				rpp = new RPPlayer(player);
				playerMap.put(player, rpp);
			}
		}
		return rpp;
	}

	/**
	 * @param player Forcefully unload the given player and it's linked account, if it exists. This may
	 *               kick players back to the main menu and/or to the lobby itself.
	 */
	public static void unload(Player player) {
		loadBlocked.add(player);
		RPPlayer rpp = playerMap.get(player);
		if (rpp != null) {
			rpp.unload();
			playerMap.remove(player);
		}
		loadBlocked.remove(player);
	}


	//////////////////
	//// INSTANCE ////
	//////////////////

	@Getter Account account;
	@Getter Persona persona;
	@Getter Player player;

	RPPlayer(Player player) {
		this.account = Account.getAccount(player);
		this.player = player;
	}

	private void unload() {
		// This should only be called when a player leaves the server.
	}

}
