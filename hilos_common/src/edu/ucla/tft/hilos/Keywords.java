package edu.ucla.tft.hilos;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class storing the keywords of a {@link Resource}. Keywords can be used to
 * convey information about the content of a resource, for example images of
 * trains can be tagged with a keyword 'Trains'. Keywords can be used to link
 * related resources together.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Keywords {
	/**
	 * Class representing a single keyword. Each keyword has a corresponding
	 * weight which determines the importance of that keyword for a resource
	 * relative to others.
	 * 
	 * @author Jasleen Singh (jasleen@cs.ucla.edu)
	 * 
	 */
	public static class Keyword {
		public static Comparator<Keyword> ByWeight = new Comparator<Keyword>() {
			@Override
			public int compare(Keyword k1, Keyword k2) {
				return (int) Math.signum(k1.weight - k2.weight);
			}
		};
		public String text;
		public double weight;

		public Keyword(String text, double weight) {
			this.text = text;
			this.weight = weight;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Keyword)) {
				return false;
			}
			Keyword kw = (Keyword) o;
			return (text != null) && text.equals(kw.text);
		}
	}

	private List<Keyword> list = new LinkedList<Keyword>();
	private double masterWeight = 0.5;

	public Keywords() {
	}

	public Keywords(Keywords attrs) {
		putAll(attrs);
	}

	public static Keywords parse(String line) {
		if (line == null) {
			return null;
		}
		Keywords keywords = new Keywords();
		String[] toks = line.trim().split("[\\s]*[;,][\\s]*");
		for (String tok : toks) {
			int n = tok.indexOf("=");
			if (n < 0) {
				continue;
			}
			double weight;
			try {
				weight = Double.parseDouble(tok.substring(n + 1));
			} catch (NumberFormatException e) {
				continue;
			}
			keywords.put(tok.substring(0, n), weight);
		}
		return keywords;
	}

	public void put(String key, double value) {
		if ("mw".equals(key)) {
			masterWeight = value;
			return;
		}
		list.add(new Keyword(key, value));
	}

	public void putAll(Keywords keywords) {
		if (keywords == null) {
			return;
		}
		list.addAll(keywords.list);
	}

	public Keyword[] toArray() {
		return list.toArray(new Keyword[0]);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		int c = 0;
		for (Keyword kw : list) {
			sb.append((c++ > 0 ? "; " : "") + kw.text + ":" + kw.weight);
		}
		return sb.toString();
	}

	public JSONObject toJSONObject() {
		Map<String, Double> map = new HashMap<String, Double>();
		map.put("mw", masterWeight);
		for (Keyword kw : list) {
			map.put(kw.text, kw.weight);
		}
		return new JSONObject(map);
	}

	public static Keywords fromJSONObject(JSONObject json) {
		try {
			Keywords keywords = new Keywords();
			Iterator<String> keys = json.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				keywords.put(key, json.getDouble(key));
			}
			return keywords;
		} catch (JSONException e) {
			return null;
		}
	}
}
