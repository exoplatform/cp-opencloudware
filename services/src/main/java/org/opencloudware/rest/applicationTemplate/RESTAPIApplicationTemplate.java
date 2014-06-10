package org.opencloudware.rest.applicationTemplate;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.model.Application;
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
@Path("/opencloudware/applicationTemplates/")
public class RESTAPIApplicationTemplate implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(RESTAPIApplicationTemplate.class);


    private RESTAPIResources restApiResources_;

    private OcwDataService ocwDataService_;

    public RESTAPIApplicationTemplate(RESTAPIResources restapiResources, OcwDataService ocwDataService) {
        restApiResources_=restapiResources;
        ocwDataService_=ocwDataService;

    }

    @GET
    @Path("{applicationTemplateId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApplicationTemplateById(@PathParam("applicationTemplateId") String applicationTemplateId, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        if (!restApiResources_.isAuthorizedUser(sc, uriInfo)) {
            return Response.status(HTTPStatus.FORBIDDEN).build();
        }
        try {
            Application applicationTemplate = ocwDataService_.getApplicationDAO().findApplicationById(applicationTemplateId);
            return OCWUtil.renderJSON(new RESTApplicationTemplate(applicationTemplate));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(HTTPStatus.INTERNAL_ERROR).build();

        }
    }


}
