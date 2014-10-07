package org.opencloudware.rest;

import org.apache.ws.commons.util.Base64;
import org.json.JSONObject;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 03/06/14.
 */
public class OCWUtil {


    private static String RESOURCE_ENDPOINT = "opencloudware.resources.endpoint";
    private static String RESOURCE_LOGIN = "opencloudware.resources.login";
    private static String RESOURCE_PASSWORD = "opencloudware.resources.password";
    private static String RESOURCE_MOCK = "opencloudware.resources.mock";
    private static String RESOURCE_SERVICE = "opencloudware.resources.service";


    public static Response renderJSON(Object result) {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);

        return Response.ok(result, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    }

    public static JSONObject getResource (String resourceType) {

        String endPoint = System.getProperty(RESOURCE_ENDPOINT);
        String login = System.getProperty(RESOURCE_LOGIN);
        String password = System.getProperty(RESOURCE_PASSWORD);
        String mock = System.getProperty(RESOURCE_MOCK);
        String service = System.getProperty(RESOURCE_SERVICE);


        String fullUrl = endPoint+service+"?type="+resourceType;

        try {

            HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl).openConnection();
            String encoded = Base64.encode(new String(login + ":" + password).getBytes());
            encoded=encoded.replace("\n","");
            connection.setRequestProperty("Authorization", "Basic " + encoded);
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                JSONObject result = new JSONObject(response.toString().substring(1,response.length()-1));
                return result;

            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }




    public static String doPost(String url, String username, String password, String postData, Map<String, String> headers, String contentType) {

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            if (username != null) {
                String encoded = Base64.encode(new String(username + ":" + password).getBytes());
                encoded = encoded.replace("\n", "");
                connection.setRequestProperty("Authorization", "Basic " + encoded);
            }
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", contentType);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Length", "" + postData.length());
            for (String key:headers.keySet()) {
                connection.setRequestProperty(key,headers.get(key));
            }

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(postData);
            System.out.println(postData);
            wr.flush();
            wr.close();
            connection.disconnect();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200  || responseCode == 201) {

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();

            } else {
                System.out.println(responseCode);
                return null;
            }

        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String doPostCreateProvider(String url, String username, String password, String postData, Map<String,String> headers, String contentType) {
        if (System.getProperty(RESOURCE_MOCK)!=null && System.getProperty(RESOURCE_MOCK).equals("true")) {
            String result="{\"id\" : \"0e177df7-0f51-4c2b-9aee-f20542c2980b\"," +
                    "\"href\" : \"http://localhost:8080/cimi/providers/9623452f-0888-4ad8-83e6-49c8a14e1654/accounts/0e177df7-0f51-4c2b-9aee-f20542c2980b\"," +
                    "\"properties\" : {\"tenantName\" : \"opencloudware\"}," +
                    "\"providerId\" : \"9623452f-0888-4ad8-83e6-49c8a14e1654\"," +
                    "\"identity\" : \"sirocco\"," +
                    "\"credential\" : \"cregDyk3\"}";
            return result;
        }
        return doPost(url,username,password,postData, headers, contentType);
    }

    public static String doPostDeploy(String url, String username, String password, String postData, Map<String,String> headers, String contentType) {
        /*if (System.getProperty(RESOURCE_MOCK)!=null && System.getProperty(RESOURCE_MOCK).equals("true")) {
            String result="{\"id\" : \"0e177df7-0f51-4c2b-9aee-f20542c2980b\"," +
                    "\"href\" : \"http://localhost:8080/cimi/providers/9623452f-0888-4ad8-83e6-49c8a14e1654/accounts/0e177df7-0f51-4c2b-9aee-f20542c2980b\"," +
                    "\"properties\" : {\"tenantName\" : \"opencloudware\"}," +
                    "\"providerId\" : \"9623452f-0888-4ad8-83e6-49c8a14e1654\"," +
                    "\"identity\" : \"sirocco\"," +
                    "\"credential\" : \"cregDyk3\"}";
            return result;
        }*/
        return doPost(url,username,password,postData, headers, contentType);
    }


}
