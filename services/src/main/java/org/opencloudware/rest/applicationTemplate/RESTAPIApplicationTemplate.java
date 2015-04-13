package org.opencloudware.rest.applicationTemplate;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.model.Application;
import org.opencloudware.hibernate.model.ApplicationInstance;
import org.opencloudware.rest.OCWUtil;
import org.opencloudware.rest.resources.RESTAPIResources;

import javax.ws.rs.*;
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApplicationTemplateByInstanceId(@QueryParam("applicationInstanceId") String applicationInstanceId, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        if (!restApiResources_.isAuthorizedUser(sc, uriInfo)) {
            return Response.status(HTTPStatus.FORBIDDEN).build();
        }
        try {
            ApplicationInstance applicationInstance = ocwDataService_.getApplicationInstanceDAO().findApplicationInstanceById(applicationInstanceId);
            if (applicationInstance!=null) {
                Application applicationTemplate = ocwDataService_.getApplicationDAO().findApplicationById(applicationInstance.getApplication().getId().toString());
                return OCWUtil.renderJSON(new RESTApplicationTemplate(applicationTemplate));
            } else {
                return Response.status(HTTPStatus.NOT_FOUND).build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(HTTPStatus.INTERNAL_ERROR).build();

        }
    }

    @GET
    @Path("getOvfByName")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOVFFileByInstanceId(@QueryParam("applicationInstanceId") String applicationInstanceId, @QueryParam("ovfFile") String ovfFile, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        if (!restApiResources_.isAuthorizedUser(sc, uriInfo)) {
            return Response.status(HTTPStatus.FORBIDDEN).build();
        }
        try {
            ApplicationInstance applicationInstance = ocwDataService_.getApplicationInstanceDAO().findApplicationInstanceById(applicationInstanceId);
            if (applicationInstance!=null) {
                Application applicationTemplate = ocwDataService_.getApplicationDAO().findApplicationById(applicationInstance.getApplication().getId().toString());
                byte[] ovf = applicationTemplate.getAlternativeModeles().get(ovfFile);

                CacheControl cacheControl = new CacheControl();
                cacheControl.setNoCache(true);
                cacheControl.setNoStore(true);
                
                return Response.ok(ovf, MediaType.TEXT_XML).cacheControl(cacheControl).build();
            } else {
                return Response.status(HTTPStatus.NOT_FOUND).build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(HTTPStatus.INTERNAL_ERROR).build();

        }
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
