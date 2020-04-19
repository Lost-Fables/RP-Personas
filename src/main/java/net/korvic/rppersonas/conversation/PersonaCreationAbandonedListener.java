package net.korvic.rppersonas.conversation;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.listeners.JoinQuitListener;
import net.korvic.rppersonas.listeners.PersonaDisableListener;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.entity.Player;

public class PersonaCreationAbandonedListener implements ConversationAbandonedListener {

	@Override
	public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
		Player p = (Player) abandonedEvent.getContext().getForWhom();

		p.hideTitle();
		PersonaDisableListener.enablePlayer(p);
		p.sendMessage("\n" + RPPersonas.PRIMARY_DARK + "Persona creation cancelled.");
		JoinQuitListener.loadIntoPersona(p);
	}

}
