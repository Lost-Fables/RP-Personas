package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.util.MessageUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.resurrection.RezApp;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class RezCommands extends BaseCommand {

	RPPersonas plugin;

	public RezCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value="Get a list of current Rez Apps.")
	public void check() {
		List<TextComponent> list = new ArrayList<>();
		list.add(new TextComponent(RPPersonas.SECONDARY_DARK + "" + ChatColor.UNDERLINE + "----------- Rez Apps -----------\n"));

		for (RezApp app : plugin.getRezHandler().getRezAppList().values()) {
			TextComponent text = new TextComponent("ID: " + app.getPersonaID() + " Karma: " + app.getKarma() + " Kills/Deaths: " + app.getKills() + "/" + app.getDeaths());
			ClickEvent event = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rpp rez info " + app.getPersonaID());

			text.setHoverEvent(MessageUtil.hoverEvent("Click to see their responses."));
			text.setClickEvent(event);
			list.add(text);
		}

		for (TextComponent text : list) {
			msg(text);
		}
	}

	@Cmd(value="Get full details on a specific rez app.")
	public void info(CommandSender sender, int id) {
		RezApp app = plugin.getRezHandler().getRezAppList().get(id);
		List<TextComponent> list = new ArrayList<>();
		list.add(new TextComponent(RPPersonas.SECONDARY_DARK + "" + ChatColor.UNDERLINE + "----------- Rez Apps -----------\n"));

		list.add(new TextComponent(RPPersonas.PRIMARY_DARK + "Why are you seeking resurrection?\n"));
		for (String string : app.getResponses().getResponsesAsList(1)) {
			list.add(new TextComponent(RPPersonas.SECONDARY_LIGHT + string));
		}
		list.add(new TextComponent(""));

		list.add(new TextComponent(RPPersonas.PRIMARY_DARK + "Have you lived an honest life?\n"));
		for (String string : app.getResponses().getResponsesAsList(2)) {
			list.add(new TextComponent(RPPersonas.SECONDARY_LIGHT + string));
		}
		list.add(new TextComponent(""));

		list.add(new TextComponent(RPPersonas.PRIMARY_DARK + "What is the meaning of your life?\n"));
		for (String string : app.getResponses().getResponsesAsList(3)) {
			list.add(new TextComponent(RPPersonas.SECONDARY_LIGHT + string));
		}
		list.add(new TextComponent(""));

		list.add(new TextComponent(RPPersonas.PRIMARY_DARK + "If you believe this persona worth rezzing, please use " + RPPersonas.SECONDARY_DARK + "/rpp rez accept " + app.getPersonaID() + RPPersonas.PRIMARY_DARK +
								   ".\n Otherwise, to deny this persona resurrection use " + RPPersonas.SECONDARY_DARK + "/rpp rez deny " + app.getPersonaID()));

		for (TextComponent text : list) {
			msg(text);
		}
	}

	@Cmd(value="Accept a resurrection app.")
	public void accept(CommandSender sender, int id) {
		plugin.getRezHandler().accept(id);
	}

	@Cmd(value="Deny a resurrection app.")
	public void deny(CommandSender sender, int id) {
		plugin.getRezHandler().deny(id);
	}

	@Cmd(value="Remove the denial of a resurrection app.")
	public void undeny(CommandSender sender, int id) {
		plugin.getRezHandler().undeny(id);
	}

}
