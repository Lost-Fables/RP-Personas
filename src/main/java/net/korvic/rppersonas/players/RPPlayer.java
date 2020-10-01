package net.korvic.rppersonas.players;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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
		RPPlayer rpplayer = null;
		if (player != null) {
			if (!loadBlocked.contains(player)) {
				rpplayer = playerMap.get(player);
			}
			if (rpplayer == null) {
				rpplayer = new RPPlayer(player);
				playerMap.put(player, rpplayer);
			}
		}
		return rpplayer;
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
	@Getter @Setter(AccessLevel.PROTECTED) Persona persona;
	@Getter Player player;

	RPPlayer(Player player) {
		this.account = Account.getAccount(player);
		this.player = player;
	}

	private void unload() {
		loadBlocked.add(player);
		// This should only be called when a player leaves the server, really.
		loadBlocked.remove(player);
	}

}
