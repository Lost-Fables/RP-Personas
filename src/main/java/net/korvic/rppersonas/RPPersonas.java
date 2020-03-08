package net.korvic.rppersonas;

import net.korvic.rppersonas.accounts.AccountHandler;
import net.korvic.rppersonas.listeners.JoinQuitListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class RPPersonas extends JavaPlugin {

	public static AccountHandler handler;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
		handler = new AccountHandler(this);
		// Get MySQL Connection

	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
}
