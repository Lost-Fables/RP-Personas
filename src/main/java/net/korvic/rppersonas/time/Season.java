package net.korvic.rppersonas.time;

import co.lotc.core.agnostic.Sender;
import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Season {

	SUMMER("Summer", RPPersonas.PRIMARY_DARK + "The days grow long and the sun beams strong. Anoma enters the season of " +                                                     RPPersonas.SECONDARY_DARK + "Summer" + RPPersonas.PRIMARY_DARK + ", marking a new Age."),
	AUTUMN("Autumn", RPPersonas.PRIMARY_DARK + "The leaves fall and many animals begin prepping for their hibernation. Anoma enters the season of " +                           RPPersonas.SECONDARY_DARK + "Autumn" + RPPersonas.PRIMARY_DARK + ", marking a new Age."),
	WINTER("Winter", RPPersonas.PRIMARY_DARK + "Cold winds blow in from the ice belt, crops have a tough time surviving, and animals are sparse. Anoma enters the season of " + RPPersonas.SECONDARY_DARK + "Winter" + RPPersonas.PRIMARY_DARK + ", marking a new Age."),
	SPRING("Spring", RPPersonas.PRIMARY_DARK + "The long cold eases up, snow melts and flora and animals alike begin to flourish once more. Anoma enters the season of " +      RPPersonas.SECONDARY_DARK + "Spring" + RPPersonas.PRIMARY_DARK + ", marking a new Age and a new Era.");

	@Getter private String name;
	@Getter private String changeMessage;

	private Season(String name, String changeMessage) {
		this.name = name;
		this.changeMessage = changeMessage;
	}

	public static Season getByName(String name) {
		for (Season season : values()) {
			if (season.name.equalsIgnoreCase(name)) {
				return season;
			}
		}
		return null;
	}

	public Season getNext() {
		List<Season> season = Arrays.asList(values());
		int index = season.indexOf(this);
		index++;
		if (index >= season.size()) {
			index -= season.size();
		}
		return season.get(index);
	}

	public Season getLast() {
		List<Season> season = Arrays.asList(values());
		int index = season.indexOf(this);
		index--;
		if (index < 0) {
			index += season.size();
		}
		return season.get(index);
	}

	public static List<String> getAvailable(Sender player) {
		ArrayList<String> list = new ArrayList<>();
		for (Season season : values()) {
			list.add(season.name);
		}
		return list;
	}

}
