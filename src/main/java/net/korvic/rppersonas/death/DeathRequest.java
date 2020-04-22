package net.korvic.rppersonas.death;

import co.lotc.core.bukkit.util.InventoryUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.sql.DeathSQL;
import net.korvic.rppersonas.sql.PersonaAccountsMapSQL;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.extras.DataMapFilter;
import net.korvic.rppersonas.statuses.EtherealStatus;
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
		this.killerPersona = RPPersonas.get().getPersonaHandler().getLoadedPersona(killer);

		this.victim = victim;
		this.victimPersona = RPPersonas.get().getPersonaHandler().getLoadedPersona(victim);

		this.location = victim.getLocation().toBlockLocation();
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

	public void complete(boolean staffInflicted) {
		saveDeathSQL(staffInflicted);
		victimPersona.addStatus(new EtherealStatus(-1)); // Become Ghost
		dropCorpse();
		savePersona();
	}

	private void saveDeathSQL(boolean staffInflicted) {
		RPPersonas plugin = RPPersonas.get();

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

	private void dropCorpse() {
		RPPersonas plugin = RPPersonas.get();

		InventoryUtil.addOrDropItem(victim, plugin.getCorpseHandler().createCorpse(victim).getItem());
		ItemStack[] items = victim.getInventory().getContents();
		victim.getInventory().clear();
		for (ItemStack item : items) {
			if (item != null) {
				victim.getLocation().getWorld().dropItem(victim.getLocation(), item);
			}
		}
	}

	private void savePersona() {
		RPPersonas plugin = RPPersonas.get();

		plugin.getDeathHandler().deleteRequest(victim);

		DataMapFilter data = new DataMapFilter();
		data.put(PersonasSQL.ALIVE, false);
		data.put(PersonasSQL.PERSONAID, victimPersona.getPersonaID());

		// In case we ever differentiate between SQL db data filters for some reason
		//data.put(PersonaAccountsMapSQL.ALIVE, false);
		//data.put(PersonaAccountsMapSQL.PERSONAID, victimPersona.getPersonaID());
		plugin.getPersonasSQL().registerOrUpdate(data);
		plugin.getPersonaAccountMapSQL().registerOrUpdate(data);
	}

}
