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
	private Long id;


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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "ProviderIAAS{" +
				"providerIAASName='" + providerIAASName + '\'' +
				", providerIAASLogin='" + providerIAASLogin + '\'' +
				", providerIAASPassword='******'" +
				", providerIAASVendor='" + providerIAASVendor + '\'' +
				", organization=" + organization +
				", id=" + id +
				'}';
	}

	public String getProviderIAASVendor() {
		return providerIAASVendor;
	}

	public void setProviderIAASVendor(String providerIAASVendor) {
		this.providerIAASVendor = providerIAASVendor;
	}
}
