package org.opencloudware.hibernate.model;


import org.exoplatform.services.database.DBObject;

/**
 * Created with IntelliJ IDEA.
 * User: Romain Denarie
 * Date: 19/08/13
 * Time: 14:11
 * To change this template use File | Settings | File Templates.
 */

public class ProviderIAAS extends DBObject {

	private String providerIAASName = null;

	private String providerIAASVendor = null;

	private String providerIAASLogin = null;

	private String providerIAASPassword = null;

	private Organization organization = null;

    private String providerPublicNetworkName = null;

    private String providerEndPoint = null;

    private String providerTenantName = null;

    private String providerGlanceUrl = null;

    private String providerOrganizationName = null;

    private String vdcName = null;

    private String providerCatalogName = null;

    private String providerSmaEndpoint = null;

    private String providerSmaLogin = null;

    private String providerSmaPassword = null;

    private String id;


	public ProviderIAAS() {
	}

	public String getProviderIAASName() {
		return providerIAASName;
	}

	public void setProviderIAASName(String providerIAASName) {
		this.providerIAASName = providerIAASName;
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

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "ProviderIAAS{" +
				"providerIAASName='" + providerIAASName + '\'' +
				", providerIAASLogin='" + providerIAASLogin + '\'' +
				", providerIAASPassword='******'" +
				", providerIAASVendor='" + providerIAASVendor + '\'' +
				//", organization=" + organization.getOrganizationName() +
                ", publicNetworkName=" + providerPublicNetworkName +
				", id=" + id +
				'}';
	}

	public String getProviderIAASVendor() {
		return providerIAASVendor;
	}

	public void setProviderIAASVendor(String providerIAASVendor) {
		this.providerIAASVendor = providerIAASVendor;
	}

    public String getProviderPublicNetworkName() {
        return providerPublicNetworkName;
    }

    public void setProviderPublicNetworkName(String providerPublicNetworkName) {
        this.providerPublicNetworkName = providerPublicNetworkName;
    }

    public String getProviderEndPoint() {
        return providerEndPoint;
    }

    public void setProviderEndPoint(String providerEndPoint) {
        this.providerEndPoint = providerEndPoint;
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



    public String getProviderOrganizationName() {
        return providerOrganizationName;
    }

    public void setProviderOrganizationName(String providerOrganizationName) {
        this.providerOrganizationName = providerOrganizationName;
    }

    public String getProviderCatalogName() {
        return providerCatalogName;
    }

    public void setProviderCatalogName(String providerCatalogName) {
        this.providerCatalogName = providerCatalogName;
    }

    public String getProviderSmaEndpoint() {
        return providerSmaEndpoint;
    }

    public void setProviderSmaEndpoint(String providerSmaEndpoint) {
        this.providerSmaEndpoint = providerSmaEndpoint;
    }

    public String getProviderSmaLogin() {
        return providerSmaLogin;
    }

    public void setProviderSmaLogin(String providerSmaLogin) {
        this.providerSmaLogin = providerSmaLogin;
    }

    public String getProviderSmaPassword() {
        return providerSmaPassword;
    }

    public void setProviderSmaPassword(String providerSmaPassword) {
        this.providerSmaPassword = providerSmaPassword;
    }
}
