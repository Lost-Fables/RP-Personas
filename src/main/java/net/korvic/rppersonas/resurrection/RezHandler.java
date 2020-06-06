package net.korvic.rppersonas.resurrection;

import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.RezAppSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;

import java.util.HashMap;

public class RezHandler {

	private RPPersonas plugin;

	@Getter	private HashMap<Integer, RezApp> rezAppList = new HashMap<>();

	public RezHandler(RPPersonas plugin) {
		this.plugin = plugin;
	}

	public void addApp(RezApp app) {
		rezAppList.put(app.getPersonaID(), app);
	}

	public void accept(int id) {
		RezApp app = rezAppList.get(id);
		rezAppList.remove(id);
		plugin.getRezAppSQL().deleteByID(id);
		app.accept();
	}
}
