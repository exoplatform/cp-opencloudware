package org.opencloudware.rest.users;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.rest.resource.ResourceContainer;
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
@Path("/opencloudware/users/")
public class RESTAPIUsers implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(RESTAPIUsers.class);


    private RESTAPIResources restApiResources_;

    private OrganizationService organizationService_;

    public RESTAPIUsers(RESTAPIResources restapiResources, OrganizationService organizationService) {
        restApiResources_=restapiResources;
        organizationService_=organizationService;

    }


    @GET
    @Path("{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam("userId") String userId, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        if (!restApiResources_.isAuthorizedUser(sc, uriInfo)) {
            return Response.status(HTTPStatus.FORBIDDEN).build();
        }
        try {

            User user = organizationService_.getUserHandler().findUserByName(userId);
            return OCWUtil.renderJSON(user);
        } catch (Exception e) {
            return Response.status(HTTPStatus.INTERNAL_ERROR).build();

        }
    }



}
