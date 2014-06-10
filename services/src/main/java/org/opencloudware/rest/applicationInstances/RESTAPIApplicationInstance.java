package org.opencloudware.rest.applicationInstances;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.model.Application;
import org.opencloudware.hibernate.model.ApplicationInstance;
import org.opencloudware.rest.OCWUtil;
import org.opencloudware.rest.resources.RESTAPIResources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 03/06/14.
 */
@Path("/opencloudware/applicationInstances/")
public class RESTAPIApplicationInstance implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(RESTAPIApplicationInstance.class);


    private RESTAPIResources restApiResources_;

    private OcwDataService ocwDataService_;

    public RESTAPIApplicationInstance(RESTAPIResources restapiResources, OcwDataService ocwDataService) {
        restApiResources_=restapiResources;
        ocwDataService_=ocwDataService;

    }

    @GET
    @Path("{applicationInstanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApplicationInstanceById(@PathParam("applicationInstanceId") String applicationInstanceId, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        if (!restApiResources_.isAuthorizedUser(sc, uriInfo)) {
            return Response.status(HTTPStatus.FORBIDDEN).build();
        }
        try {
            ApplicationInstance applicationInstance = ocwDataService_.getApplicationInstanceDAO().findApplicationInstanceById(applicationInstanceId);
            return OCWUtil.renderJSON(new RESTApplicationInstance(applicationInstance));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(HTTPStatus.INTERNAL_ERROR).build();

        }
    }


}
