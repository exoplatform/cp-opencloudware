package org.ow2.opencloudware.commons.vamp;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Java front-end to the VAMP manager, wrapping its REST API. Should be moved to
 * the VAMP project as a Java SDK.
 * 
 * @author Bruno Dillenseger
 */
public class VampManager
{
	private Client client = Client.create();
	private String endPoint;

	/**
	 * Creates a wrapper to a VAMP manager.
	 * @param endPoint the URL to the VAMP manager REST API
	 */
	public VampManager(String endPoint)
	{
		this.endPoint = endPoint;
	}

	/**
	 * Gets the list of deployment managers created by this VAMP manager.
	 * @return a set of objects wrapping the REST API of deployment managers
	 * @throws Exception there was a problem dealing with the REST API of the
	 * VAMP manager.
	 */
	public Set<DeploymentManager> getApplications()
	throws Exception
	{
		return getApplications(null);
	}

	/**
	 * Either find the deployment manager associated to a given application,
	 * or gets the list of all deployment managers created by this VAMP manager.
	 * @param optId the application identifier, or null to get all deployment managers
	 * @return a set containing either the deployment manager associated to the given
	 * application identifier, or all the deployment managers created by the VAMP manager.
	 * @throws Exception either the application identifier is not known by the VAMP manager, or
	 * there was a problem dealing with its REST API.
	 */
	public Set<DeploymentManager> getApplications(String optId)
	throws Exception
	{
		Set<DeploymentManager> result = new HashSet<DeploymentManager>();

        WebResource webResource = client.resource(endPoint + "/applications");
		InputStream response = webResource.get(ClientResponse.class).getEntity(InputStream.class);
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document xdoc = parser.parse(response);
		NodeList xparents = xdoc.getElementsByTagName("deployment-manager");
        boolean searchAgain = true;
		for (int i = 0; i < xparents.getLength() && searchAgain; ++i)
		{
			String name = null;
			String id = null;
			String url = null;
			NodeList xchildren = xparents.item(i).getChildNodes();

            for (int j = 0; j < xchildren.getLength(); ++j)
			{
				Node xnode = xchildren.item(j);
				if (xnode.getNodeName().equalsIgnoreCase("name"))
				{
					name = xnode.getFirstChild().getNodeValue();
				}
				else if (xnode.getNodeName().equalsIgnoreCase("id"))
				{
					id = xnode.getFirstChild().getNodeValue();
				}
				else if (xnode.getNodeName().equalsIgnoreCase("url"))
				{
					Node urlNode = xnode.getFirstChild();
					if (urlNode != null)
					{
						url = urlNode.getNodeValue();
						if (url.isEmpty())
						{
							url = null;
						}
					}
				}
			}


            if (name != null && id != null && (optId == null || optId.equals(id)))
			{
				result.add(new DeploymentManager(this, name, id, url));
				if (optId != null)
				{
					searchAgain = false;
				}
			}
		}
		response.close();
		return result;
	}

	/**
	 * Deploys an application with VAMP. On return, the application is still
	 * being deployed and should not be considered as ready. Use the returned
	 * deployment manager object to query its status.
	 * @see DeploymentManager#getApplicationInfo()
	 * @see DeploymentManager#isDeployed()
	 * @param name the application name
	 * @param ovf the VAMP OVF description of the application to deploy
	 * @param iaasId if not null, overrides the target IaaS identifier specified in
	 * the ovf parameter.
	 * @return the deployment manager associated to this deploying application
	 */
	public DeploymentManager createApplication(String name, String ovf, String iaasId) throws Exception
	{
		DeploymentManager result = null;
		WebResource webResource = client.resource(endPoint + "/applications");
		Form form = new Form();
		form.add("applicationName", name);
		form.add("applicationDesc", ovf);
		if (iaasId != null && ! iaasId.trim().isEmpty())
		{
			form.add("iaasId", iaasId);
		}
		ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
			.post(ClientResponse.class, form);
		String responseStr = response.getEntity(String.class);
		Pattern regex = Pattern.compile("<id>(.*)</id>");
		Matcher parser = regex.matcher(responseStr);
		parser.find();
		String id = parser.group(1);
		result = new DeploymentManager(this, name, id, null);
		return result;
	}
}