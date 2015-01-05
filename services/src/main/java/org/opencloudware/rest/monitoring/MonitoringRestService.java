package org.opencloudware.rest.monitoring;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.opencloudware.hibernate.model.ProviderIAAS;
import org.opencloudware.rest.OCWUtil;
import org.opencloudware.rest.resources.OCWResource;
import org.opencloudware.rest.resources.RESTAPIResources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 01/12/14.
 */
@Path("/opencloudware/monitoring/")
public class MonitoringRestService implements ResourceContainer {
    private static final Log LOG = ExoLogger.getLogger(MonitoringRestService.class);


    private RESTAPIResources restApiResources_;

    public MonitoringRestService (RESTAPIResources restapiResources) {
        restApiResources_=restapiResources;

    }

    @GET
    @Path("{resourceId}/{key}")
    public Response getMetric(@PathParam("resourceId") String resourceId,@PathParam("key") String key) {


        OCWResource monitoringResource = restApiResources_.getResourceByType("metering");

        String resourceEndPoint = monitoringResource.getResourceEndpoint();
        String endPointURI = "/metric";

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("resource",resourceId);
        headers.put("key",key);

        String username = monitoringResource.getResourceLogin();
        String password = monitoringResource.getResourcePassword();

        String responseData = OCWUtil.doGet(resourceEndPoint + endPointURI, username, password,headers,"text/plain; charset=utf8");
        if (responseData!=null) {
            System.out.println("receive metering response " + responseData);
            return Response.ok(responseData).build();
        }else {
            return Response.serverError().build();
        }
    }



}
