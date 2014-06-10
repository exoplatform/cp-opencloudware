package org.opencloudware.rest.resources;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.opencloudware.rest.OCWUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 03/06/14.
 */
@Path("/opencloudware/resources/")
public class RESTAPIResources implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(RESTAPIResources.class);

    private List<OCWResource> resourcesList;

    private String adminUser;


    public RESTAPIResources(InitParams initParams) {
        ObjectParameter objectParam = initParams.getObjectParam("initialResource.configuration");
        if (objectParam != null)
            resourcesList = ((ResourcesDefinition)objectParam.getObject()).getResources();

        for (OCWResource resource : resourcesList) {
            if (resource.getResourceType().equals("bdd") && resource.getResourceImplType().equals("exoplatform")) {
                if (resource.getResourceLogin()!=null) {
                    adminUser = resource.getResourceLogin();
                }

            }
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResourceByType(@QueryParam("type") String type, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        if (!isAuthorizedUser(sc,uriInfo)) {
            return Response.status(HTTPStatus.FORBIDDEN).build();
        }

        if (type == null || type.equals("")) {
            return OCWUtil.renderJSON(resourcesList);
        }

        List<OCWResource> result = new ArrayList<OCWResource>();
        for (OCWResource res : resourcesList) {
            if (res.getResourceType().equals(type)) {
                result.add(res);
            }
        }
        return OCWUtil.renderJSON(result);

    }

    public boolean isAuthorizedUser(SecurityContext sc, UriInfo uriInfo) {

        //return true;
        String userId = getUserId(sc, uriInfo);

        return userId == null ? false : userId.equals(this.adminUser);

    }

    private String getUserId(SecurityContext sc, UriInfo uriInfo) {
        try {
            return sc.getUserPrincipal().getName();
        } catch (Exception e) {
            return null;
        }
    }




}
