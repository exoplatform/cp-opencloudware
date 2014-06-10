package org.opencloudware.rest.resources;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 03/06/14.
 */
public class OCWResource {
    private String resourceType;
    private String resourceImplType;
    private String resourceEndpoint;
    private String resourceDescription;
    private String resourceName;
    private String resourceLogin;
    private String resourcePassword;


    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceImplType() {
        return resourceImplType;
    }

    public void setResourceImplType(String resourceImplType) {
        this.resourceImplType = resourceImplType;
    }

    public String getResourceEndpoint() {
        return resourceEndpoint;
    }

    public void setResourceEndpoint(String resourceEndpoint) {
        this.resourceEndpoint = resourceEndpoint;
    }

    public String getResourceDescription() {
        return resourceDescription;
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceLogin() {
        return resourceLogin;
    }

    public void setResourceLogin(String resourceLogin) {
        this.resourceLogin = resourceLogin;
    }

    public String getResourcePassword() {
        return resourcePassword;
    }

    public void setResourcePassword(String resourcePassword) {
        this.resourcePassword = resourcePassword;
    }
}