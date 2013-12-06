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

public class Project extends DBObject {

	private String projectName = null;

	private String description = null;

	private Organization organization = null;

	private Set<Application> applications =
			new HashSet<Application>(0);
	private Set<String> managers = new HashSet<String>(0);



	private Long id;


	public Project() {
	}
	public Project(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}


	@Override
	public String toString() {
		return "Application{" +
				"projectName='" + projectName + '\'' +
				", description='" + description + '\'' +
				", organization=" + organization +
				", applications=" + applications +
				", id=" + id +

				'}';
	}

	public Set<Application> getApplications() {
		return applications;
	}

	public void setApplications(Set<Application> applications) {
		this.applications = applications;
	}

	public Set<String> getManagers() {
		return managers;
	}

	public void setManagers(Set<String> managers) {
		this.managers = managers;
	}
}


