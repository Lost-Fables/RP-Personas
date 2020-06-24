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
			TextComponent text = new TextComponent(RPPersonas.PRIMARY_DARK + "ID: " + RPPersonas.SECONDARY_DARK + app.getPersonaID() +
												   RPPersonas.PRIMARY_DARK + " Karma: " + RPPersonas.SECONDARY_DARK + app.getKarma() +
												   RPPersonas.PRIMARY_DARK + " Kills/Deaths: " + RPPersonas.SECONDARY_DARK + app.getKills() + "/" + app.getDeaths());
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

		list.add(new TextComponent(RPPersonas.PRIMARY_DARK + "Why are you seeking resurrection?"));
		list.add(new TextComponent(RPPersonas.SECONDARY_LIGHT + app.getResponses().getResponse(1)));
		list.add(new TextComponent(""));

		list.add(new TextComponent(RPPersonas.PRIMARY_DARK + "Have you lived an honest life?"));
		list.add(new TextComponent(RPPersonas.SECONDARY_LIGHT + app.getResponses().getResponse(2)));
		list.add(new TextComponent(""));

		list.add(new TextComponent(RPPersonas.PRIMARY_DARK + "What is the meaning of your life?"));
		list.add(new TextComponent(RPPersonas.SECONDARY_LIGHT + app.getResponses().getResponse(3)));
		list.add(new TextComponent(""));

		{
			TextComponent acceptText = new TextComponent(RPPersonas.PRIMARY_DARK + "If you believe this persona worth rezzing use/click " + ChatColor.GREEN + "/rpp rez accept " + app.getPersonaID());
			ClickEvent event = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rpp rez accept " + app.getPersonaID());
			acceptText.setHoverEvent(MessageUtil.hoverEvent("Click to accept this app."));
			acceptText.setClickEvent(event);
			list.add(acceptText);
		}

		{
			TextComponent denyText = new TextComponent(RPPersonas.PRIMARY_DARK + "Otherwise, to deny this persona resurrection use/click " + ChatColor.RED + "/rpp rez deny " + app.getPersonaID());
			ClickEvent event = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rpp rez deny " + app.getPersonaID());
			denyText.setHoverEvent(MessageUtil.hoverEvent("Click to deny this app."));
			denyText.setClickEvent(event);
			list.add(denyText);
		}

		for (TextComponent text : list) {
			msg(text);
		}
	}

	@Cmd(value="Accept a resurrection app.")
	public void accept(CommandSender sender, int id) {
		plugin.getRezHandler().accept(id);
		msg(RPPersonas.PRIMARY_DARK + "Resurrection app accepted.");
	}

	@Cmd(value="Deny a resurrection app.")
	public void deny(CommandSender sender, int id) {
		plugin.getRezHandler().deny(id);
		msg(RPPersonas.PRIMARY_DARK + "Resurrection app denied.");
	}

	@Cmd(value="Remove the denial of a resurrection app.", permission=RPPersonas.PERMISSION_START + ".rez.undeny")
	public void undeny(CommandSender sender, int id) {
		plugin.getRezHandler().undeny(id);
		msg(RPPersonas.PRIMARY_DARK + "Resurrection app un-denied.");
	}

}
