package net.korvic.rppersonas.players;

import lombok.Getter;
import org.bukkit.entity.Player;

public class RPPlayer {

	//////////////////
	//// INSTANCE ////
	//////////////////

	@Getter private Player player;
	@Getter Account account;
	@Getter Persona persona;

	RPPlayer(Player player) {
		this.player = player;
		this.account = Account.getAccount(player);
	}

}
