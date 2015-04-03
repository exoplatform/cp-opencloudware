package org.opencloudware.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.exoplatform.container.xml.ValueParam;
import org.picocontainer.Startable;

import org.apache.http.entity.mime.MultipartEntity;

import java.io.*;

import org.exoplatform.container.xml.InitParams;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 26/03/15.
 */
public class FileUploadService implements Startable {
    private static final String URL_STRING = "urlString";

    private String urlString = "";


    public FileUploadService(InitParams initParams) {
        if (initParams != null) {
            ValueParam urlString = initParams.getValueParam(URL_STRING);
            if (urlString != null)
                this.urlString = urlString.getValue();
        }
    }


    /**
     * A generic method to execute any type of Http Request and constructs a response object
     * @param requestBase the request that needs to be exeuted
     * @return server response as <code>String</code>
     */
    private static String executeRequest(HttpRequestBase requestBase){
        String responseString = "" ;

        InputStream responseStream = null ;
        HttpClient client = new DefaultHttpClient() ;
        try{
            HttpResponse response = client.execute(requestBase) ;
            if (response != null){
                HttpEntity responseEntity = response.getEntity() ;

                if (responseEntity != null){
                    responseStream = responseEntity.getContent() ;
                    if (responseStream != null){
                        BufferedReader br = new BufferedReader (new InputStreamReader(responseStream)) ;
                        String responseLine = br.readLine() ;
                        String tempResponseString = "" ;
                        while (responseLine != null){
                            tempResponseString = tempResponseString + responseLine + System.getProperty("line.separator") ;
                            responseLine = br.readLine() ;
                        }
                        br.close() ;
                        if (tempResponseString.length() > 0){
                            responseString = tempResponseString ;
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if (responseStream != null){
                try {
                    responseStream.close() ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        client.getConnectionManager().shutdown() ;

        return responseString ;
    }

    /**
     * Method that builds the multi-part form data request
     * @param file the actual file instance that needs to be uploaded
     * @param fileName name of the file, just to show how to add the usual form parameters
     * @param fileDescription some description for the file, just to show how to add the usual form parameters
     * @return server response as <code>String</code>
     */
    public String uploadFile(File file, String fileName, String fileDescription) {

        HttpPost postRequest = new HttpPost (urlString) ;
        try{

            MultipartEntity multiPartEntity = new MultipartEntity () ;

            //The usual form parameters can be added this way
            multiPartEntity.addPart("fileDescription", new StringBody(fileDescription != null ? fileDescription : "")) ;
            multiPartEntity.addPart("fileName", new StringBody(fileName != null ? fileName : file.getName())) ;

            /*Need to construct a FileBody with the file that needs to be attached and specify the mime type of the file. Add the fileBody to the request as an another part.
            This part will be considered as file part and the rest of them as usual form-data parts*/
            FileBody fileBody = new FileBody(file) ;
            multiPartEntity.addPart("attachment", fileBody) ;

            postRequest.setEntity(multiPartEntity) ;
        }catch (UnsupportedEncodingException ex){
            ex.printStackTrace() ;
        }

        return executeRequest (postRequest) ;
        //return("200");
        
    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
