package org.opencloudware.rest.vm;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencloudware.rest.OCWUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 21/09/15.
 */
@Path("/opencloudware/vm/")
public class RESTAPIVm implements ResourceContainer {



    @GET
    @Path("getBilling/{vmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBilling(@PathParam("vmId") String vmId,@QueryParam("start_date") String startDate,@QueryParam("end_date") String endDate, @Context SecurityContext sc, @Context UriInfo uriInfo) {
        try {
            JSONObject resource = OCWUtil.getResource("billing");

            String resourceEndPoint = resource.getString("resourceEndpoint");
            String endPointURI = "/charging";
            String username=resource.getString("resourceLogin").equals("null") ? null : resource.getString("resourceLogin");
            String password=resource.getString("resourcePassword").equals("null") ? null : resource.getString("resourcePassword");
            Map<String,String> headers= new HashMap<String,String>();
            JSONObject data = new JSONObject();

                data.put("project_id", "ea08cc13-1c54-4044-bb67-b0529cf2e634");
                data.put("resource_id",vmId);
                data.put("template", "opencloudware_perday_rate");

                if (startDate != null) {
                    JSONObject period = new JSONObject();
                    period.put("start_date", startDate);
                    period.put("end_date", endDate);
                    data.put("period",period);
                }

            System.out.println("getBilling with data : " + data);

            String result = OCWUtil.doPost(resourceEndPoint+endPointURI,username,password,data.toString(),headers,"application/json");
            System.out.print(result);
            return OCWUtil.renderJSON(result);


        } catch (JSONException jse) {
            jse.printStackTrace();
        }
        return Response.status(HTTPStatus.INTERNAL_ERROR).build();

    }
}
