package org.ow2.opencloudware.commons.vamp;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Set;

/**
 * Java front-end to the VAMP deployment manager, wrapping its REST API.
 * Should be moved to the VAMP project as a Java SDK.
 * 
 * @author Bruno Dillenseger
 */
public class DeploymentManager
{
	static final int POLLING_PERIOD_S = 10;
	static final String STATE_DEPLOYED = "DEPLOYED";

    private String url;
    private String id;
    private String name;
    private VampManager vamp;

    /**
     * Creates a wrapper to a VAMP deployment manager.
     * @param vamp the VAMP manager which created this deployment manager
     * @param name the name of the deployed application
     * @param id the application identifier attributed by VAMP
     * @param url the URL to the deployment manager. If null, it will be resolved later
     * on a lazy fashion by asking the VAMP manager.
     */
    public DeploymentManager(VampManager vamp, String name, String id, String url)
    {
    	this.url = url;
    	this.name = name;
    	this.id = id;
    	this.vamp = vamp;
    }

    /**
     * Gets information about the status of the application deployment
     * @param wait when true, this method returns when the deployment manager's
     * URL is available (which may take some time...); when false, the method immediately
     * @return an XML string derived from the OVF description of the application,
     * giving status information about the deployment of all components of the application
     * @throws Exception the call to the deployment manager REST API failed
     */
    public String getApplicationInfo(boolean wait)
    throws Exception
    {
        Client client = Client.create();
        if (getUrl(wait) != null) {
            WebResource webResource = client.resource(getUrl(wait) + "/application");

            ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).get(ClientResponse.class);
            client.destroy();
            return response.getEntity(String.class);
        }
        return null;
    }

    /**
     * Gets the application name.
     * @return the application name, as given when submitting the application's
     * OVF description to the VAMP manager for deployment.
     */
    public String getName()
    {
    	return name;
    }

    /**
     * Checks whether the application managed by this deployment manager
     * is deployed or not at the moment. Subsequent calls may return different
     * results according to the current application state.
     * @return true if the application is deployed, false otherwise.
     * @throws Exception getting the application information from the
     * deployment manager failed.
     *
     */
    public boolean isDeployed()
    throws Exception
    {
        String applicationInfo = getApplicationInfo(false);
        if (applicationInfo!=null) {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document xdoc = parser.parse(new InputSource(new StringReader(applicationInfo)));
            Element envelope = xdoc.getDocumentElement();
            return envelope.getAttribute("ocw:state").equalsIgnoreCase(STATE_DEPLOYED);
        } else {
            return false;
        }
    }

    /**
     * Deletes the application managed by the deployment manager.
     * This method triggers the application deletion but returns before
     * the application is actually deleted.
     * @throws Exception the application deletion could not be initiated.
     */
    public void delete()
    throws Exception
    {
    	Client client = Client.create();
        WebResource webResource = client.resource(getUrl(true) + "/application");
        webResource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).delete();
        client.destroy();
    }

    /**
     * Gets the application identifier.
     * @return the application identifier, as attributed by the VAMP manager to this
     * application
     */
    public String getId()
    {
    	return id;
    }

    /**
     * Gets the deployment manager URL.
     * @param wait when true, this method returns when the deployment manager's
     * URL is available (which may take some time...); when false, the method immediately
     * returns, returning the URL or null if it is not available yet
     * @return the URL of the deployment manager responsible for deploying this application,
     * or null if the deployment manager URL is not available yet
     * @throws Exception the URL could not be resolved from the VAMP manager
     */
    public String getUrl(boolean wait)
    throws Exception
    {
    	boolean retry = true;
		while (url == null && retry)
    	{
			if (! wait)
			{
				retry = false;
			}
    		Set<DeploymentManager> dms = vamp.getApplications(id);
    		if (dms.isEmpty())
    		{
    			throw new Exception("Unknown application id " + id);
    		}
    		else
    		{
    			url = dms.iterator().next().url;
            }
			if (url == null && wait)
			{
				Thread.sleep(POLLING_PERIOD_S * 1000);
			}
    	}
    	return url;
    }

    /**
     * Fancy printing of this deployment manager.
     * @return string "vApp::<application name>::<application identifier>::<deployment manager url>"
     */
    public String toString()
    {
    	return "vApp::" + name + "::" + id + "::" + url;
    }
}