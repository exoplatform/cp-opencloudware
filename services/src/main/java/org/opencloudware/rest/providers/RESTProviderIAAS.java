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
    private Long id;

    public RESTProviderIAAS(ProviderIAAS providerIAAS) {
        this.providerIAASName=providerIAAS.getProviderIAASName();
        this.providerIAASVendor=providerIAAS.getProviderIAASVendor();
        this.providerIAASLogin=providerIAAS.getProviderIAASLogin();
        this.providerIAASPassword=providerIAAS.getProviderIAASPassword();
        this.organizationId=providerIAAS.getOrganization().getId();
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
