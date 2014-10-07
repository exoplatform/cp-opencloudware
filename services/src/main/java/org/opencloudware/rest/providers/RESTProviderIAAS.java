package org.opencloudware.rest.providers;

import org.opencloudware.hibernate.model.ProviderIAAS;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 04/06/14.
 */
public class RESTProviderIAAS {

    private String providerIAASName = null;

    private String providerIAASVendor = null;

    private String providerIAASLogin = null;

    private String providerIAASPassword = null;

    private String organizationId = null;


    private String providerPublicNetworkName = null;

    private String providerEndpoint = null;
    private String providerGlanceUrl = null;

    private String providerTenantName = null;

    private String vdcName = null;
    private String id;

    public RESTProviderIAAS(ProviderIAAS providerIAAS) {
        this.providerIAASName=providerIAAS.getProviderIAASName();
        this.providerIAASVendor=providerIAAS.getProviderIAASVendor();
        this.providerIAASLogin=providerIAAS.getProviderIAASLogin();
        this.providerIAASPassword=providerIAAS.getProviderIAASPassword();
        this.organizationId=providerIAAS.getOrganization().getId();
        this.providerPublicNetworkName=providerIAAS.getProviderPublicNetworkName();
        this.providerEndpoint=providerIAAS.getProviderEndPoint();
        this.providerTenantName=providerIAAS.getProviderTenantName();
        this.providerGlanceUrl=providerIAAS.getProviderGlanceUrl();
        this.vdcName=providerIAAS.getVdcName();
        this.id=providerIAAS.getId();

    }

    public String getProviderIAASName() {
        return providerIAASName;
    }

    public void setProviderIAASName(String providerIAASName) {
        this.providerIAASName = providerIAASName;
    }

    public String getProviderIAASVendor() {
        return providerIAASVendor;
    }

    public void setProviderIAASVendor(String providerIAASVendor) {
        this.providerIAASVendor = providerIAASVendor;
    }

    public String getProviderIAASLogin() {
        return providerIAASLogin;
    }

    public void setProviderIAASLogin(String providerIAASLogin) {
        this.providerIAASLogin = providerIAASLogin;
    }

    public String getProviderIAASPassword() {
        return providerIAASPassword;
    }

    public void setProviderIAASPassword(String providerIAASPassword) {
        this.providerIAASPassword = providerIAASPassword;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProviderPublicNetworkName() {
        return providerPublicNetworkName;
    }

    public void setProviderPublicNetworkName(String providerPublicNetworkName) {
        this.providerPublicNetworkName = providerPublicNetworkName;
    }

    public String getProviderEndpoint() {
        return providerEndpoint;
    }

    public void setProviderEndpoint(String providerEndpoint) {
        this.providerEndpoint = providerEndpoint;
    }

    public String getProviderTenantName() {
        return providerTenantName;
    }

    public void setProviderTenantName(String providerTenantName) {
        this.providerTenantName = providerTenantName;
    }

    public String getProviderGlanceUrl() {
        return providerGlanceUrl;
    }

    public void setProviderGlanceUrl(String providerGlanceUrl) {
        this.providerGlanceUrl = providerGlanceUrl;
    }

    public String getVdcName() {
        return vdcName;
    }

    public void setVdcName(String vdcName) {
        this.vdcName = vdcName;
    }
}
