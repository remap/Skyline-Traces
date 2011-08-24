package edu.ucla.tft.hilos;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class grouping together all models contained in a 3D scene.
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 *
 */
public class Scene {
	public static class Vec2 {
		public float x;
		public float y;

		public Vec2() {
			this(0, 0);
		}

		public Vec2(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public JSONObject toJSON() throws JSONException {
			JSONObject json = new JSONObject();
			json.put("x", x);
			json.put("y", y);
			return json;
		}

		public static Vec2 fromJSON(JSONObject json) throws JSONException {
			if (json == null) {
				return new Vec2(0, 0);
			}
			return new Vec2((float) json.getDouble("x"),
					(float) json.getDouble("y"));
		}
	}

	public static class Vec3 {
		public float x;
		public float y;
		public float z;

		public Vec3() {
			this(0, 0, 0);
		}

		public Vec3(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public JSONObject toJSON() throws JSONException {
			JSONObject json = new JSONObject();
			json.put("x", x);
			json.put("y", y);
			json.put("z", z);
			return json;
		}

		public static Vec3 fromJSON(JSONObject json) throws JSONException {
			if (json == null) {
				return new Vec3(0, 0, 0);
			}
			return new Vec3((float) json.getDouble("x"),
					(float) json.getDouble("y"), (float) json.getDouble("z"));
		}
	}

	public static class Model {
		public static final Vec3 DEFAULT_SCALE = new Vec3(1, 1, 1);
		public static final Vec3 DEFAULT_ANCHOR = new Vec3(0, 0, 0);
		public static final Vec2 DEFAULT_LOCATION = new Vec2(34.067360f,
				-118.233747f);

		public String src;
		public String title;
		public Vec2 location;
		public Vec3 scale;
		public Vec3 anchor;

		public Model() {
		}

		public Model(String src, String title,
				Vec2 location, Vec3 scale, Vec3 anchor) {
			this.src = src;
			this.title = title;
			this.location = location;
			this.scale = scale;
			this.anchor = anchor;
		}

		private static void put(JSONObject obj, String key, Object val)
				throws JSONException {
			if (val == null) {
				return;
			}
			obj.put(key, val);
		}

		public JSONObject toJSON() throws JSONException {
			JSONObject json = new JSONObject();
			put(json, "src", src);
			put(json, "title", title);
			put(json, "location", location == null ? null : location.toJSON());
			put(json, "scale", scale == null ? null : scale.toJSON());
			put(json, "anchor", anchor == null ? null : anchor.toJSON());
			return json;
		}

		public static Model fromJSON(JSONObject json) throws JSONException {
			Model model = new Model();
			model.src = json.optString("src");
			model.title = json.optString("title");
			model.location = Vec2.fromJSON(json.optJSONObject("location"));
			model.scale = Vec3.fromJSON(json.optJSONObject("scale"));
			model.anchor = Vec3.fromJSON(json.optJSONObject("anchor"));
			return model;
		}
	}

	public List<Model> models = new LinkedList<Model>();

	public JSONObject toJSON() throws JSONException {
		JSONArray jsonModels = new JSONArray();
		for (Model model : models) {
			jsonModels.put(model);
		}
		JSONObject jsonScene = new JSONObject();
		jsonScene.put("models", jsonModels);
		return jsonScene;
	}

	public static Scene fromJSON(JSONObject json) throws JSONException {
		Scene scene = new Scene();
		JSONArray jsonModels = json.getJSONArray("models");
		for (int i = 0; i < jsonModels.length(); i++) {
			JSONObject jsonModel = jsonModels.getJSONObject(i);
			scene.models.add(Model.fromJSON(jsonModel));
		}
		return scene;
	}
}
