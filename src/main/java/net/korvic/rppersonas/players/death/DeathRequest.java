package net.korvic.rppersonas.players.death;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.util.MessageUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.players.Persona;
import net.korvic.rppersonas.sql.DeathSQL;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DeathRequest {

	private Player killer;
	private Persona killerPersona;

	private Player victim;
	private Persona victimPersona;

	private Location location;

	public DeathRequest(Player killer, Player victim) {
		this.killer = killer;
		this.killerPersona = Persona.getPersona(killer);

		this.victim = victim;
		this.victimPersona = Persona.getPersona(victim);

		this.location = victim.getLocation().toBlockLocation().add(0, 1, 0);
	}

	public Player getKiller() {
		return killer;
	}
	public Player getVictim() {
		return victim;
	}
	public Location getLocation() {
		return location;
	}

	public void ping() {
		if (victim.isOnline()) {
			victim.sendMessage(RPPersonas.SECONDARY_DARK + killerPersona.getNickname() + RPPersonas.PRIMARY_DARK + " is attempting to execute you.");
			BaseComponent command = MessageUtil.CommandButton("Use " + RPPersonas.SECONDARY_DARK + "/persona executeaccept " + killer.getName() + RPPersonas.PRIMARY_DARK + " to accept.",
															  "/persona executeaccept " + killer.getName(),
															  RPPersonas.SECONDARY_LIGHT + "Click here!",
															  RPPersonas.PRIMARY_DARK,
															  RPPersonas.SECONDARY_DARK);
			victim.sendMessage(command);
		}
	}

	public void complete(boolean staffInflicted) {
		RPPersonas plugin = RPPersonas.get();

		saveDeathSQL(plugin, staffInflicted);

		victim.teleport(plugin.getDeathLocation());
		dropCorpse(plugin, location);
		savePersona(plugin);
		plugin.getDeathHandler().deleteRequest(victim);
	}

	private void saveDeathSQL(RPPersonas plugin, boolean staffInflicted) {
		DataMapFilter data = new DataMapFilter();
		data.put(DeathSQL.VICTIM_PERSONAID, this.victimPersona.getPersonaID())
			.put(DeathSQL.VICTIM_ACCOUNTID, this.victimPersona.getAccountID())
			.put(DeathSQL.VICTIM_UUID, this.victim.getUniqueId())

			.put(DeathSQL.KILLER_PERSONAID, this.killerPersona.getPersonaID())
			.put(DeathSQL.KILLER_ACCOUNTID, this.killerPersona.getAccountID())
			.put(DeathSQL.KILLER_UUID, this.killer.getUniqueId())

			.put(DeathSQL.LOCATION, this.location)
			.put(DeathSQL.STAFF, staffInflicted);

		plugin.getDeathSQL().registerOrUpdate(data);
	}

	private void dropCorpse(RPPersonas plugin, Location loc) {
		InventoryUtil.addOrDropItem(victim, plugin.getCorpseHandler().createCorpse(victim).getItem());
		ItemStack[] items = victim.getInventory().getContents();
		victim.getInventory().clear();
		for (ItemStack item : items) {
			if (item != null) {
				loc.getWorld().dropItemNaturally(loc, item);
			}
		}
	}

	private void savePersona(RPPersonas plugin) {
		DataMapFilter data = new DataMapFilter();
		data.put(PersonasSQL.ALIVE, false);
		data.put(PersonasSQL.PERSONAID, victimPersona.getPersonaID());

		// In case we ever differentiate between SQL db data filters for some reason,
		// currently just applies the same data twice.
		//data.put(PersonaAccountsMapSQL.ALIVE, false);
		//data.put(PersonaAccountsMapSQL.PERSONAID, victimPersona.getPersonaID());
		plugin.getPersonasSQL().registerOrUpdate(data);
		plugin.getPersonaAccountMapSQL().registerOrUpdate(data);

		victimPersona.setAlive(false);
	}

}
