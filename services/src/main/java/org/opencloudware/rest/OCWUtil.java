package org.opencloudware.rest;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 03/06/14.
 */
public class OCWUtil {
    public static Response renderJSON(Object result) {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);

        return Response.ok(result, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    }
}
