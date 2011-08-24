package edu.ucla.tft.hilos;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A servlet which fetches a list of all resources available in the Gallery2
 * database. Additional parameters in the URL can specify:
 * <ul>
 * <li>A <code>filter=&lt;attribute 1&gt;[,...&lt;attribute n&gt;]</code> string
 * which specifies which resources to accept. Each attribute is a key-value pair
 * given as <code>[-]&lt;key&gt;[:&lt;value&gt;]</code>, where the portion in
 * square brackets is optional. A '-' before an attribute key means that a
 * resource with the attribute should <i>not</i> be accepted. All attribute
 * constraints must be satisfied for a resource to be accepted.</li>
 * <li>A <code>format={json|html}</code> parameter to specify the format of the
 * returned result. By default, the resource list is returned in JSON format.</li>
 * </ul>
 * 
 * @author Jasleen Singh (jasleen@cs.ucla.edu)
 * 
 */
public class GetResourceList extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String downloadResourceURL;

	@Override
	public void init() throws ServletException {
		Properties properties = new Properties();
		try {
			FileReader r = new FileReader(getServletContext().getRealPath(
					"server.properties"));
			properties.load(r);
			r.close();
			downloadResourceURL = properties.getProperty("downloadResourceURL");
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		handleRequest(req, resp);
	}

	private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {
		try {
			String lang = req.getParameter("lang");
			String filter_str = req.getParameter("filter");
			String fmt = req.getParameter("fmt");
			ResourceFilter filter = ResourceFilter.NULL;
			if (filter_str != null) {
				String[] attributeKeyValues = filter_str.split(",");
				filter = new ResourceFilterByAttribute(attributeKeyValues);
			}
			boolean error = false;
			JSONArray resources = new JSONArray();
			try {
				getAllResources(resources, filter);
			} catch (Exception e) {
				e.printStackTrace();
				error = true;
			}
			if (fmt == null || "json".equals(fmt)) {
				writeJSON(resp, resources, error);
			} else {
				writeHTML(resp, resources, error);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private void writeJSON(HttpServletResponse resp, JSONArray resources,
			boolean error) throws JSONException, IOException {
		JSONObject ret = new JSONObject();
		if (!error) {
			ret.put("resources", resources);
		} else {
			ret.put("error", Constants.s[Constants.DATABASE_ERROR]);
		}
		resp.setContentType("text/plain");
		PrintWriter pw = resp.getWriter();
		ret.write(pw);
		pw.close();
	}

	private void writeHTML(HttpServletResponse resp, JSONArray resources,
			boolean error) throws JSONException, IOException {
		resp.setContentType("text/html");
		PrintWriter pw = resp.getWriter();
		pw.println("<html><head><title>Resource List</title><head><body>");
		for (int i = 0; i < resources.length(); i++) {
			Resource res = Resource.fromJSONObject(resources.getJSONObject(i));
			pw.println(String.format(
					"%d. <a href=\"%s\">Download resource</a><br/>", (i + 1),
					String.format(downloadResourceURL, res.id)));
			pw.println(String.format("id: %d<br/>", res.id));
			pw.println(String.format("location: %f, %f<br/>", res.latitude,
					res.longitude));
			pw.println(String.format("attributes: %s<br/>", res.attributes));
			pw.println(String.format("keywords: %s<br/>", res.keywords));
			pw.println(String.format("resourceType: %s<br/>", res.resourceType));
			pw.println(String.format("title: %s<br/>", res.title));
			pw.println(String.format("date: %s<br/>", res.date));
			pw.println(String.format("description: %s<br/>", res.description));
			pw.println("<hr/>");
		}
		pw.println("</body></html>");
		pw.close();
	}

	private static String attributesCondition(List<String> list, String key) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s.g_field='weighted_keywords' and ", key));
		sb.append(" (");
		for (int i = 0; i < list.size(); i++) {
			sb.append((i > 0 ? " OR " : "")
					+ String.format("%s.g_value LIKE '%%%s:%%'", key,
							list.get(i)));
		}
		sb.append(")");
		return sb.toString();
	}

	private static String albumAttributesCondition(String key) {
		List<String> list = new LinkedList<String>();
		list.add("landmark");
		list.add("parcel");
		list.add("style");
		return attributesCondition(list, key);
	}

	private void getAllResources(JSONArray resources, ResourceFilter filter)
			throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet set = null;
		try {
			conn = Db.getConnection("lashp_g2");
			stmt = conn.createStatement();
			String query = String
					.format("select t1.g_id as id, t2.g_value as customFieldValue from g2_AlbumItem as t1, g2_CustomFieldMap as t2 where t1.g_id=t2.g_itemId and %s",
							albumAttributesCondition("t2"));
			set = stmt.executeQuery(query);
			while (set.next()) {
				int folder_id = set.getInt("id");
				String customFieldValue = set.getString("customFieldValue");
				Attributes attributes = Attributes.parse(customFieldValue);
				Keywords keywords = Keywords.parse(customFieldValue);
				getResourcesInFolder(folder_id, attributes, keywords,
						resources, filter);
			}
		} finally {
			Utils.close(set, stmt, conn);
		}
	}

	private void getResourcesInFolder(int folder_id,
			Attributes parentAttributes, Keywords parentKeywords,
			JSONArray resources, ResourceFilter filter) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet set1 = null, set2 = null;
		try {
			conn = Db.getConnection("lashp_g2");
			stmt = conn.createStatement();
			String query1;
			query1 = String
					.format("select t1.g_id as id, t6.g_value as customFieldValue, t3.g_entityType as resourceType, t5.g_value as location, t1.g_title as title, t3.g_creationTimestamp as timestamp, t1.g_description as description from g2_Item as t1 inner join g2_ChildEntity as t2 on t1.g_id=t2.g_id and t2.g_parentId=%d inner join g2_Entity as t3 on t1.g_id=t3.g_id left join g2_mapMap as t5 on t2.g_id=t5.g_itemId and t5.g_field='GPS' left join g2_CustomFieldMap as t6 on t1.g_id=t6.g_itemId and t6.g_field='weighted_keywords'",
							folder_id);
			set1 = stmt.executeQuery(query1);
			while (set1.next()) {
				int id = set1.getInt("id");
				String latlong = set1.getString("location");
				double latitude = 0, longitude = 0;
				if (latlong != null) {
					String[] latlong_tok = latlong.split(",");
					latitude = Double.parseDouble(latlong_tok[0]);
					longitude = Double.parseDouble(latlong_tok[1]);
				}
				String customFieldValue = set1.getString("customFieldValue");
				Attributes attributes = new Attributes(parentAttributes);
				attributes.putAll(Attributes.parse(customFieldValue));
				Keywords keywords = new Keywords(parentKeywords);
				keywords.putAll(Keywords.parse(customFieldValue));
				String resourceType = attributes.get("format");
				if (resourceType == null) {
					resourceType = Utils.G2(set1.getString("resourceType"));
				}
				if (resourceType == null) {
					continue;
				}
				String title = set1.getString("title");
				String date = Utils.dateToString(new Date(set1
						.getLong("timestamp")));
				String description = set1.getString("description");
				Resource resource = new Resource(id, latitude, longitude,
						attributes, keywords, resourceType, title, date,
						description);
				if (filter.accept(resource)) {
					resources.put(resource.toJSONObject());
				}
			}
			String query2 = String
					.format("select t1.g_id as id, t3.g_value as customFieldValue from g2_ChildEntity as t1 inner join g2_Entity as t2 on t1.g_id=t2.g_id and g_parentId=%d and t2.g_entityType='GalleryAlbumItem' left join g2_CustomFieldMap as t3 on t1.g_id=t3.g_itemId",
							folder_id);
			set2 = stmt.executeQuery(query2);
			while (set2.next()) {
				int id = set2.getInt("id");
				String customFieldValue = set2.getString("customFieldValue");
				Attributes newAttributes = new Attributes(parentAttributes);
				newAttributes.putAll(Attributes.parse(customFieldValue));
				Keywords newKeywords = new Keywords(parentKeywords);
				newKeywords.putAll(Keywords.parse(customFieldValue));
				getResourcesInFolder(id, newAttributes, newKeywords, resources,
						filter);
			}
		} finally {
			Utils.close(set1, set2, stmt, conn);
		}
	}
}
