package net.korvic.rppersonas.sql.extras;

import net.korvic.rppersonas.RPPersonas;

import java.util.HashMap;
import java.util.Map;

public class DataBuffer {

	// STATIC //
	private static Map<String, String> DATA_MAP = new HashMap<>(); // input string to output string
	private static Map<String, Class> CLASS_MAP = new HashMap<>(); // output string to class type

	public static void addMapping(String[] inputs, String output, Class clazz) {
		for (String str : inputs) {
			addMapping(str, output, clazz);
		}
	}

	public static void addMapping(String input, String output, Class clazz) {
		if (DATA_MAP.containsKey(input.toLowerCase())) {
			if (RPPersonas.DEBUGGING) {
				RPPersonas.get().getLogger().info("Duplicate mapping for input '" + input.toLowerCase() + "'.");
			}
		} else {
			DATA_MAP.put(input.toLowerCase(), output);
			CLASS_MAP.put(output, clazz);
		}
	}

	// INSTANCE //
	private Map<String, Object> data = new HashMap<>();

	public DataBuffer addData(String input, Object value) {
		String output = DATA_MAP.get(input);
		Class clazz = CLASS_MAP.get(output);
		if (clazz.isInstance(value)) {
			data.put(output, value);
		} else {
			RPPersonas.get().getLogger().warning("Wrong data type submitted for '" + output + "'. Expected instance of " + clazz.toString() + ", received " + value.getClass().toString());
		}

		return this;
	}

	public Map<String, Object> getData() {
		return data;
	}

}
