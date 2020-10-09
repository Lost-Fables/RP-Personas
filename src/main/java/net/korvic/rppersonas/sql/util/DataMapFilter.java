package net.korvic.rppersonas.sql.util;

import net.korvic.rppersonas.RPPersonas;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DataMapFilter {

	// STATIC //
	private static Map<String, String> DATA_MAP = new HashMap<>(); // input string to output string
	private static Map<String, Class> CLASS_MAP = new HashMap<>(); // output string to class type

	public static void addFilter(String[] inputs, String output, Class clazz) {
		for (String str : inputs) {
			addFilter(str, output, clazz);
		}
	}

	public static void addFilter(String input, String output, Class clazz) {
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

	public DataMapFilter putAllData(DataMapFilter data) {
		for (Entry<String, Object> entry : data.data.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
		return this;
	}

	public DataMapFilter putAllObject(Map<Object, Object> data) {
		for (Entry<Object, Object> entry : data.entrySet()) {
			if (entry.getKey() instanceof String) {
				put((String) entry.getKey(), entry.getValue());
			} else if (RPPersonas.DEBUGGING) {
				RPPersonas.get().getLogger().warning("Failed to translate " + entry.getKey().toString() + " to a string. Had value " + entry.getValue().toString());
			}
		}
		return this;
	}

	public DataMapFilter putAll(Map<String, Object> data) {
		for (Entry<String, Object> entry : data.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
		return this;
	}

	public DataMapFilter put(String input, Object value) {
		String output = DATA_MAP.get(input);
		Class clazz = CLASS_MAP.get(output);
		try {
			if (value == null || clazz.isInstance(value)) {
				data.put(output, value);
			} else {
				RPPersonas.get().getLogger().warning("Wrong data type submitted for '" + output + "'. Expected instance of " + clazz.toString() + ", received " + value.getClass().toString());
			}
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				String clazzString = null;
				String valueString = null;

				if (clazz != null) {
					clazzString = clazz.toString();
				}

				if (value != null) {
					valueString = value.getClass().toString();
				}

				RPPersonas.get().getLogger().warning("Wrong data type submitted for '" + output + "'. Expected instance of " + clazzString + ", received " + valueString);
				e.printStackTrace();
			}
		}

		return this;
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public Object get(String key) {
		return data.get(key);
	}

	public boolean containsKey(String key) {
		return data.containsKey(key);
	}

}
