package org.opencloudware.rest.providers;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.model.ProviderIAAS;
import org.opencloudware.rest.OCWUtil;
import org.opencloudware.rest.resources.RESTAPIResources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 03/06/14.
 */
@Path("/opencloudware/providers/")
public class RESTAPIProviders implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(RESTAPIProviders.class);


    private RESTAPIResources restApiResources_;

    private OcwDataService ocwDataService_;

    public RESTAPIProviders (RESTAPIResources restapiResources, OcwDataService ocwDataService) {
        restApiResources_=restapiResources;
        ocwDataService_=ocwDataService;

    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProviders(@QueryParam("userId") String userId, @QueryParam("organizationId") String organizationId, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        if (!restApiResources_.isAuthorizedUser(sc, uriInfo)) {
            return Response.status(HTTPStatus.FORBIDDEN).build();
        }

        if (userId == null||userId.equals("")) {
            if (organizationId == null||organizationId.equals("")) {
                return getAllProviders();
            } else {
                return getProvidersByOrganization(organizationId);
            }
         } else {
            return getOrganizationByUser(userId);
        }


    }

    @GET
    @Path("{providerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProvidersById(@PathParam("providerId") String providerId, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        if (!restApiResources_.isAuthorizedUser(sc, uriInfo)) {
            return Response.status(HTTPStatus.FORBIDDEN).build();
        }
        try {
            ProviderIAAS provider = ocwDataService_.getProviderIAASDAO().findProviderById(providerId);
            return OCWUtil.renderJSON(new RESTProviderIAAS(provider));
        } catch (Exception e) {
            return Response.status(HTTPStatus.INTERNAL_ERROR).build();

        }
    }

    private Response getOrganizationByUser(String userId) {
        return null;
    }

    private Response getProvidersByOrganization(String organizationId) {
        try {
            List<RESTProviderIAAS> result = new ArrayList<RESTProviderIAAS>();

            LazyPageList<ProviderIAAS> providersPageList = ocwDataService_.getProviderIAASDAO().getProviderIAASPageListByOrganization(10, organizationId);
            for (ProviderIAAS provider : providersPageList.getAll()) {
                result.add(new RESTProviderIAAS(provider));
            }
            return OCWUtil.renderJSON(result);
        } catch (Exception e) {
            return Response.status(HTTPStatus.INTERNAL_ERROR).build();

        }
    }

    private Response getAllProviders() {
        try {
            List<RESTProviderIAAS> result = new ArrayList<RESTProviderIAAS>();

            LazyPageList<ProviderIAAS> providersPageList = ocwDataService_.getProviderIAASDAO().getProviderIAASPageList(10);
            for (ProviderIAAS provider : providersPageList.getAll()) {
                result.add(new RESTProviderIAAS(provider));
            }
            return OCWUtil.renderJSON(result);
        } catch (Exception e) {
            return Response.status(HTTPStatus.INTERNAL_ERROR).build();

        }
    }


}
