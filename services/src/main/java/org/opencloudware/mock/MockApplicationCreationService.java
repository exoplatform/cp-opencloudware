package org.opencloudware.mock;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.opencloudware.hibernate.model.ApplicationInstance;
import org.opencloudware.rest.OCWUtil;
import org.opencloudware.rest.applicationInstances.RESTApplicationInstance;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 10/07/15.
 */
@Path("/opencloudware/createAppFromBuildFile/")
public class MockApplicationCreationService implements ResourceContainer {



    @POST
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response createAppFromBuildFile(@Context SecurityContext sc, @Context UriInfo uriInfo) {
        try {
            
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("scalable-springoo-with-probe.ovf.original").getFile());

            return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"") //optional
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }


}
