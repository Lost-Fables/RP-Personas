package net.korvic.rppersonas.time;

import co.lotc.core.agnostic.Sender;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Season {

	SUMMER("Summer"),
	AUTUMN("Autumn"),
	WINTER("Winter"),
	SPRING("Spring");

	@Getter private String name;

	private Season(String name) {
		this.name = name;
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
