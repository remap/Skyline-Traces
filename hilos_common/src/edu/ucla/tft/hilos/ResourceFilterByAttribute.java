package edu.ucla.tft.hilos;

/**
 * A {@link ResourceFilter} which accepts resources matching the input
 * attributes. Each attribute is a key-value pair given as
 * <code>[-]&lt;key&gt;[:&lt;value&gt;]</code>, where the portion in square
 * brackets is optional. A '-' before an attribute key means that a resource
 * with the attribute should <i>not</i> be accepted. All attribute constraints
 * must be satisfied for a resource to be accepted.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class ResourceFilterByAttribute implements ResourceFilter {
	private String[] attributeKeys;
	private String[] attributeValues;

	public ResourceFilterByAttribute(String... attributeKeyValues) {
		attributeKeys = new String[attributeKeyValues.length];
		attributeValues = new String[attributeKeyValues.length];
		for (int i = 0; i < attributeKeyValues.length; i++) {
			String kv = attributeKeyValues[i];
			int n = kv.indexOf(":");
			if (n < 0) {
				attributeKeys[i] = kv;
			} else {
				attributeKeys[i] = kv.substring(0, n);
				attributeValues[i] = kv.substring(n + 1);
			}
		}
	}

	@Override
	public boolean accept(Resource resource) {
		for (int i = 0; i < attributeKeys.length; i++) {
			String key = attributeKeys[i];
			String value = attributeValues[i];
			boolean exclude = false;
			if (key.charAt(0) == '-') {
				key = key.substring(1);
				exclude = true;
			}
			if (value == null) {
				if (!resource.containsAttributeKey(key) ^ exclude) {
					return false;
				}
			} else {
				if (!resource.matchesAttribute(key, value) ^ exclude) {
					return false;
				}
			}
		}
		return true;
	}
}