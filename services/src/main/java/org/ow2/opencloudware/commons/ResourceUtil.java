package org.ow2.opencloudware.commons;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 * Utility library to invoke the OpenCloudware portal's database
 * with regard to resources.
 * Basically just a Java SDK wrapping the portal's REST services.
 * @author Bruno Dillenseger
 */
public class ResourceUtil
{
	// JSON array containing the list of available resources/services with all associated information
	// (as returned by the REST method /resources)
	private JsonArray resources;

	/**
	 * Creates a convenience front-end wrapping REST access to the portal's database
	 * with regard to available resources. Basically, the full list of resources
	 * is retrieved by this constructor, and then, all methods are utility methods
	 * to perform various look-ups of some pieces of information about these resources.
	 * @param portalURL the URL to the login page of the OpenCloudware portal, such as http://host:port/portal/login
	 * @param login user name for identification by the portal
	 * @param password for authentication by the portal
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public ResourceUtil(String portalURL, String login, String password)
	throws MalformedURLException, IOException
	{
		URL url = new URL(portalURL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setInstanceFollowRedirects(false);
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "text/plain");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		BufferedWriter writer = new BufferedWriter(
			new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
/* 
		if (url.getUserInfo() != null)
		{
		    String basicAuth = "Basic " + new String(DatatypeConverter.printBase64Binary(url.getUserInfo().getBytes()));
		    conn.setRequestProperty("Authorization", basicAuth);
		}
*/
		writer.write(
			"username=" + login
			+ "&password=" + password
			+ "&initialURI=/portal/rest/opencloudware/resources?type=deployment");
		writer.close();

		int status = conn.getResponseCode();
		while (status / 100 == 3) // redirection
		{
			String location = conn.getHeaderField("Location");
			String params = location.substring(location.indexOf("?"));
			location = location.substring(0, location.indexOf(";"));
			url = new URL(location + params);
			String cookies = conn.getHeaderField("Set-Cookie");
			conn.disconnect();
			conn = (HttpURLConnection)url.openConnection();
			conn.setRequestProperty("Cookie", cookies);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
			conn.setInstanceFollowRedirects(false);
			status = conn.getResponseCode();
		}
		if (status != 200)
		{
			throw new RuntimeException("HTTP error " + conn.getResponseCode() + ": " + conn.getResponseMessage());
		}
		InputStream is = conn.getInputStream();
		JsonReader jr = Json.createReader(is);
	    resources = jr.readArray();
		conn.disconnect();
	}

	/**
	 * Gets the list of the supported resource types.
	 * @return a set of string, each string giving a resource type represented by
	 * an available resource registered in the portal. Even when several implementations
	 * are available for a given resource type, the returned set contains a single
	 * occurrence of this resource type.
	 */
	public Set<String> getResourceTypes()
	{
		Set<String> result = new HashSet<String>(resources.size());
		for (JsonObject instance : resources.getValuesAs(JsonObject.class))
		{
			result.add(instance.getString("resourceType"));
		}
		return result;
	}

	/**
	 * Get the list of implementation types supported for a given resource type.
	 * @param resourceType the resource type of interest
	 * @return a list of resource implementation types available for this resource
	 * type. When no implementation is available for this type, the returned list is empty.
	 * When several implementations are available, they are all returned in the list,
	 * possibly with several occurrences of the same implementation type if several
	 * instances are available.
	 */
	public List<String> getResourceImpl(String resourceType)
	{
		List<String> result = new ArrayList<String>(resources.size());
		for (JsonObject instance : resources.getValuesAs(JsonObject.class))
		{
			String type = instance.getString("resourceType");
			if (type.equalsIgnoreCase(resourceType))
			{
				instance.getString("resourceImplType");
			}
		}
		return result;
	}

	/**
	 * Get the full records of information for a given resource implementation type
	 * of a given resource type.
	 * @param resourceType the resource type of interest
	 * @param resourceImplType the resource implementation type of interest
	 * @return Given a resource type and a resource implementation type, returns the
	 * list of records of information describing each implementation (description, end point, etc.).
	 * This list is empty if no such resource is available. This list contains more than one record
	 * when several instances of the given implementation type are available for the given resource type.
	 */
	public List<Map<String,String>> getResourceEntry(String resourceType, String resourceImplType)
	{
		List<Map<String,String>> result = new ArrayList<Map<String,String>>(1);
		for (JsonObject instance : resources.getValuesAs(JsonObject.class))
		{
			String type = instance.getString("resourceType");
			String impl = instance.getString("resourceImplType");
			if (type.equalsIgnoreCase(resourceType) && impl.equalsIgnoreCase(resourceImplType))
			{
				Map<String,String> element = new HashMap<String,String>();
				for (Map.Entry<String,JsonValue> entry : instance.entrySet())
				{
					element.put(entry.getKey(), entry.getValue().toString());
				}
				result.add(element);
			}
		}
		return result;
	}

	/**
	 * Get the end points of a given implementation type for a given resource type.
	 * @param resourceType
	 * @param resourceImplType
	 * @return Given a resource type and a resource implementation type, returns the
	 * list of end points giving access to all available instances.
	 * This list is empty if no such resource is available. This list contains more
	 * than one end point when several instances of the given implementation type
	 * are available for the given resource type.
	 */
	public List<String> getResourceEndPointURI(String resourceType, String resourceImplType)
	{
		List<String> result = new ArrayList<String>(1);
		for (JsonObject instance : resources.getValuesAs(JsonObject.class))
		{
			String implType = instance.getString("resourceImplType");
			if (implType.equalsIgnoreCase(resourceImplType))
			{
				result.add(instance.getString("resourceEndpoint"));
			}
		}
		return result;
	}
}
