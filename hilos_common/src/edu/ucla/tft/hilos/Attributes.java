package edu.ucla.tft.hilos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class storing the attributes of a {@link Resource}. Attributes are used to
 * specify meta data for a media item, for example its type (e.g.
 * landmark:ZanjaMadre), its format (format:png) and so on.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Attributes {
	private Map<String, String> map = new HashMap<String, String>();

	public Attributes() {
	}

	public Attributes(Attributes attrs) {
		putAll(attrs);
	}

	public static Attributes parse(String line) {
		if (line == null) {
			return null;
		}
		Attributes attrs = new Attributes();
		String[] toks = line.trim().split("[\\s]*[;,][\\s]*");
		for (String tok : toks) {
			int n = tok.indexOf(":");
			if (n < 0) {
				continue;
			}
			attrs.put(tok.substring(0, n), tok.substring(n + 1));
		}
		return attrs;
	}

	public String get(String key) {
		return map.get(key);
	}

	public void put(String key, String value) {
		map.put(key, value);
	}

	public void putAll(Attributes attrs) {
		if (attrs == null) {
			return;
		}
		map.putAll(attrs.map);
	}

	public boolean containsKey(String key) {
		return map.containsKey(key);
	}

	public boolean matches(String key, String value) {
		String v = map.get(key);
		return v != null && v.equals(value);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		int c = 0;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append((c++ > 0 ? "; " : "") + entry.getKey() + ":"
					+ entry.getValue());
		}
		return sb.toString();
	}

	public JSONObject toJSONObject() {
		return new JSONObject(map);
	}

	public static Attributes fromJSONObject(JSONObject json) {
		try {
			Attributes attrs = new Attributes();
			Iterator<String> keys = json.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				attrs.map.put(key, json.getString(key));
			}
			return attrs;
		} catch (JSONException e) {
			return null;
		}
	}
}
