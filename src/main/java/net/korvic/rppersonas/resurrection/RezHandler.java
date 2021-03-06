package net.korvic.rppersonas.resurrection;

import co.lotc.core.util.DataMapFilter;
import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.RezAppSQL;
import org.bukkit.Bukkit;

import java.util.HashMap;

public class RezHandler {

	private RPPersonas plugin;

	@Getter	private HashMap<Integer, RezApp> rezAppList = new HashMap<>();

	public RezHandler(RPPersonas plugin) {
		this.plugin = plugin;
	}

	public void addApp(RezApp app) {
		rezAppList.put(app.getPersonaID(), app);
		Bukkit.broadcast(RPPersonas.PRIMARY_DARK + "A new rez app has been submitted.", RPPersonas.PERMISSION_START + ".rez");
	}

	public void accept(int id) {
		RezApp app = rezAppList.get(id);
		rezAppList.remove(id);
		plugin.getRezAppSQL().deleteByID(id);
		app.accept();
	}

	public void deny(int id) {
		rezAppList.remove(id);
		DataMapFilter data = new DataMapFilter().put(RezAppSQL.PERSONAID, id).put(RezAppSQL.DENIED, true);
		plugin.getRezAppSQL().registerOrUpdate(data);
	}

	public void undeny(int id) {
		DataMapFilter data = new DataMapFilter().put(RezAppSQL.PERSONAID, id).put(RezAppSQL.DENIED, false);
		plugin.getRezAppSQL().registerOrUpdate(data);
		addApp(new RezApp(plugin.getRezAppSQL().getData(id)));
	}
}
