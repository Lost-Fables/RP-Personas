package net.korvic.rppersonas.resurrection;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.util.DataMapFilter;
import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.conversation.RezAppConvo.RezAppResponses;
import net.korvic.rppersonas.death.Altar;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.RezAppSQL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class RezApp {

	@Getter private int personaID;
	@Getter private RezAppResponses responses;
	@Getter private int karma;
	@Getter private int kills;
	@Getter private int deaths;
	@Getter private Altar altar;

	public RezApp(DataMapFilter data) {
		if (data.containsKey(RezAppSQL.PERSONAID)) {
			personaID = (int) data.get(RezAppSQL.PERSONAID);
		}
		if (data.containsKey(RezAppSQL.RESPONSES)) {
			responses = (RezAppResponses) data.get(RezAppSQL.RESPONSES);
		}
		if (data.containsKey(RezAppSQL.KARMA)) {
			karma = (int) data.get(RezAppSQL.KARMA);
		}
		if (data.containsKey(RezAppSQL.KILLS)) {
			kills = (int) data.get(RezAppSQL.KILLS);
		}
		if (data.containsKey(RezAppSQL.DEATHS)) {
			deaths = (int) data.get(RezAppSQL.DEATHS);
		}
		if (data.containsKey(RezAppSQL.ALTAR)) {
			altar = (Altar) data.get(RezAppSQL.ALTAR);
		}
	}

	public void accept() {
		RPPersonas plugin = RPPersonas.get();

		DataMapFilter personaData = new DataMapFilter();
		personaData.put(PersonasSQL.PERSONAID, personaID);
		personaData.put(PersonasSQL.LIVES, RPPersonas.DEFAULT_REZ_LIVES);
		personaData.put(PersonasSQL.ALTARID, altar.getAltarID());
		personaData.put(PersonasSQL.CORPSEINV, null);

		plugin.getPersonasSQL().registerOrUpdate(personaData);
		plugin.getPersonaAccountMapSQL().registerOrUpdate(personaData);
		plugin.getCorpseSQL().deleteByPersonaID(personaID);

		Persona pers = plugin.getPersonaHandler().getLoadedPersona(personaID);
		if (pers != null && pers.getUsingPlayer().isOnline()) {
			pers.getUsingPlayer().sendMessage(RPPersonas.PRIMARY_DARK + "Your soul is being pulled back to it's body...");
			new BukkitRunnable() {
				private int passes = 0;

				@Override
				public void run() {
					if (passes > 3) {
						if (pers.getUsingPlayer().isOnline()) {
							plugin.getPersonaHandler().swapToPersona(pers.getUsingPlayer(), pers.getAccountID(), personaID, false); // Reload the force-saved data from above.
							pers.getUsingPlayer().teleportAsync(altar.getTPLocation());
						}
						this.cancel();
					} else {
						passes++;
					}
				}
			}.runTaskTimer(plugin, 20, 20);
		} else {
			int accountID = plugin.getPersonaAccountMapSQL().getAccountOf(personaID);
			for (UUID uuid : plugin.getUuidAccountMapSQL().getUUIDsOf(accountID)) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null && p.isOnline()) {
					p.sendMessage(RPPersonas.PRIMARY_DARK + "Your persona " + RPPersonas.SECONDARY_DARK + plugin.getPersonasSQL().getName(personaID) + RPPersonas.PRIMARY_DARK + " has been resurrected.");
				}
			}
		}
	}

}
