package org.opencloudware.hibernate.model;


import org.exoplatform.services.database.DBObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Romain Denarie
 * Date: 19/08/13
 * Time: 14:11
 * To change this template use File | Settings | File Templates.
 */

public class Organization extends DBObject {

	private String organizationName = null;

	private String creditCardNumber = null;

	private String groupId = null;

	private String address = null;

	private Long id;


	private Set<ProviderIAAS> providerIAASes =
			new HashSet<ProviderIAAS>(0);
	private Set<Project> projects =
			new HashSet<Project>(0);
	private Set<String> managers = new HashSet<String>(0);
	private Set<String> usersRequest= new HashSet<String>(0);


	public Organization() {
	}

	public Organization(String organizationName) {
		this.organizationName = organizationName;
	}


	public String getId()
	{

		return id.toString();
	}

	public void setId(String id)
	{
		this.id = Long.valueOf(id);
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public String getCreditCardNumber() {
		return creditCardNumber;
	}

	public void setCreditCardNumber(String creditCardNumber) {

		//todo : increase this part to store credit card number securely
		this.creditCardNumber = creditCardNumber;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}


	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}


	@Override
	public String toString() {
		return "Organization{" +
				"organizationName='" + organizationName + '\'' +
				", creditCardNumber='" + creditCardNumber + '\'' +
				", groupId='" + groupId + '\'' +
				", address='" + address + '\'' +
				", id=" + id +
				", providerIAASes=" + providerIAASes +
				", projects=" + projects +
				'}';
	}

	public Set<ProviderIAAS> getProviderIAASes() {
		return providerIAASes;
	}

	public void setProviderIAASes(Set<ProviderIAAS> providerIAASSets) {
		this.providerIAASes = providerIAASSets;
	}

	public Set<Project> getProjects() {
		return projects;
	}

	public void setProjects(Set<Project> projects) {
		this.projects = projects;
	}

	public Set<String> getManagers() {
		return managers;
	}

	public void setManagers(Set<String> managers) {
		this.managers = managers;
	}

	public Set<String> getUsersRequest() {
		return usersRequest;
	}

	public void setUsersRequest(Set<String> usersRequest) {
		this.usersRequest = usersRequest;
	}
}
