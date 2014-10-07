package org.opencloudware.filter;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 25/08/14.
 */
public class DebugHeaderValve extends ValveBase {

    public void invoke(Request request, Response response) throws IOException,
            ServletException {
        System.out.println("=== DEBUG Headers ===");
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        Enumeration headerNames = httpServletRequest.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName=(String) headerNames.nextElement();
            System.out.println("Header name "+ headerName + "="+httpServletRequest.getHeader(headerName));
        }

        
        getNext().invoke(request, response);


    }
}
