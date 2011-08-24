package edu.ucla.tft.hilos;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class representing a single media item, which could be an image, an audio
 * file or a video file. A variety of formats are supported for each type. Each
 * resource has a number of fields along with a set of {@link Attributes} and
 * {@link Keywords}.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class Resource {
	public static final String RESOURCE_TYPE_IMAGE_JPG = "jpg",
			RESOURCE_TYPE_IMAGE_PNG = "png", RESOURCE_TYPE_IMAGE_GIF = "gif",
			RESOURCE_TYPE_AUDIO_MP3 = "mp3", RESOURCE_TYPE_VIDEO_MP4 = "mp4",
			RESOURCE_TYPE_VIDEO_OGG = "ogg";

	public static final List<String> sResourceTypeImageList = Arrays.asList(
			RESOURCE_TYPE_IMAGE_JPG, RESOURCE_TYPE_IMAGE_PNG,
			RESOURCE_TYPE_IMAGE_GIF);

	public static boolean isImageType(String resourceType) {
		return sResourceTypeImageList.contains(resourceType);
	}

	public static final List<String> sResourceTypeAudioList = Arrays
			.asList(RESOURCE_TYPE_AUDIO_MP3);

	public static boolean isAudioType(String resourceType) {
		return sResourceTypeAudioList.contains(resourceType);
	}

	public static final List<String> sResourceTypeVideoList = Arrays.asList(
			RESOURCE_TYPE_VIDEO_MP4, RESOURCE_TYPE_VIDEO_OGG);

	public static boolean isVideoType(String resourceType) {
		return sResourceTypeVideoList.contains(resourceType);
	}

	public final int id;
	public final double latitude;
	public final double longitude;
	public final Attributes attributes;
	public final Keywords keywords;
	public final String resourceType;
	public final String title;
	public final String date;
	public final String description;

	public Resource(int id, double latitude, double longitude,
			Attributes attributes, Keywords keywords, String resourceType,
			String title, String date, String description) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.attributes = attributes;
		this.keywords = keywords;
		this.resourceType = resourceType;
		this.title = title;
		this.date = date;
		this.description = description;
	}

	public Resource(Resource copy) {
		this.id = copy.id;
		this.latitude = copy.latitude;
		this.longitude = copy.longitude;
		this.attributes = copy.attributes;
		this.keywords = copy.keywords;
		this.resourceType = copy.resourceType;
		this.title = copy.title;
		this.date = copy.date;
		this.description = copy.description;
	}

	public boolean containsAttributeKey(String key) {
		return attributes == null ? false : attributes.containsKey(key);
	}

	public boolean matchesAttribute(String key, String value) {
		return attributes == null ? false : attributes.matches(key, value);
	}

	public String getStringAttributeValue(String key) {
		String val_str;
		return attributes == null || (val_str = attributes.get(key)) == null ? ""
				: val_str;
	}

	public boolean getBooleanAttributeValue(String key) {
		try {
			return Boolean.parseBoolean(attributes.get(key));
		} catch (Exception e) {
			return false;
		}
	}

	public int getIntAttributeValue(String key) {
		try {
			return Integer.parseInt(attributes.get(key));
		} catch (Exception e) {
			return 0;
		}
	}

	public double getDoubleAttributeValue(String key) {
		try {
			return Double.parseDouble(attributes.get(key));
		} catch (Exception e) {
			return 0.0;
		}
	}

	public JSONObject toJSONObject() {
		try {
			JSONObject json = new JSONObject();
			json.put("id", id);
			json.put("latitude", latitude);
			json.put("longitude", longitude);
			if (attributes != null) {
				json.put("attributes", attributes.toJSONObject());
			}
			if (keywords != null) {
				json.put("keywords", keywords.toJSONObject());
			}
			json.put("resourceType", resourceType);
			json.put("title", title);
			json.put("date", date);
			json.put("description", description);
			return json;
		} catch (JSONException e) {
			// shouldn't come here
			return null;
		}
	}

	public static Resource fromJSONObject(JSONObject json) {
		try {
			int id = json.getInt("id");
			double latitude = json.getDouble("latitude");
			double longitude = json.getDouble("longitude");
			JSONObject attrJSON = json.optJSONObject("attributes");
			Attributes attributes = attrJSON == null ? null : Attributes
					.fromJSONObject(attrJSON);
			JSONObject kwJSON = json.optJSONObject("keywords");
			Keywords keywords = kwJSON == null ? null : Keywords
					.fromJSONObject(kwJSON);
			String resourceType = json.getString("resourceType");
			String title = json.optString("title");
			String date = json.optString("date");
			String description = json.optString("description");
			return new Resource(id, latitude, longitude, attributes, keywords,
					resourceType, title, date, description);
		} catch (JSONException e) {
			e.printStackTrace();
			// shouldn't come here
			return null;
		}
	}
}