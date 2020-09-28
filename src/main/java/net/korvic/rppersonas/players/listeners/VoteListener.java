package net.korvic.rppersonas.players.listeners;

import co.lotc.core.util.MojangCommunicator;
import com.vexsoftware.votifier.model.VotifierEvent;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class VoteListener implements Listener {

	private RPPersonas plugin;

	public VoteListener(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onVotifierEvent(VotifierEvent event) {
		String username = event.getVote().getUsername();
		UUID uuid = null;
		try {
			uuid = MojangCommunicator.requestPlayerUUID(username);
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}

		if (uuid != null) {
			int accountID = plugin.getUuidAccountMapSQL().getAccountID(uuid);
			plugin.getAccountsSQL().incrementVotes(accountID);
			plugin.getLogger().info(RPPersonas.PRIMARY_LIGHT + "Vote registered for " + RPPersonas.SECONDARY_LIGHT + username + RPPersonas.PRIMARY_LIGHT + ". Account ID: " + RPPersonas.SECONDARY_LIGHT + accountID);
		}
	}

}
