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

public class Application extends DBObject {

	private String applicationName = null;

	private String description = null;
	private Project project = null;

	private Set<String> managers = new HashSet<String>(0);
	private Long id;




    private byte[] modele;
    private byte[] rules;


    private Set<byte[]> alternativeModeles = new HashSet<byte[]>(0);


    private Set<ApplicationInstance> applicationsInstance =
            new HashSet<ApplicationInstance>(0);


	public Application() {
	}
	public Application(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
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

	public String toString() {
		return "Application{" +
				"applicationName='" + applicationName + '\'' +
				", author='" + description + '\'' +
				//", project='" + project.getProjectName() + '\'' +
                //", applicationsInstance='" + applicationsInstance + '\'' +
                ", id=" + id +
				'}';
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Set<String> getManagers() {
		return managers;
	}

	public void setManagers(Set<String> managers) {
		this.managers = managers;
	}

    public Set<ApplicationInstance> getApplicationsInstance() {
        return applicationsInstance;
    }

    public void setApplicationsInstance(Set<ApplicationInstance> applicationsInstance) {
        this.applicationsInstance = applicationsInstance;
    }

    public byte[] getModele() {
        return modele;
    }

    public void setModele(byte[] modele) {
        this.modele = modele;
    }

    public byte[] getRules() {
        return rules;
    }

    public void setRules(byte[] rules) {
        this.rules = rules;
    }

    public Set<byte[]> getAlternativeModeles() {
        return alternativeModeles;
    }

    public void setAlternativeModeles(Set<byte[]> alternativeModeles) {
        this.alternativeModeles = alternativeModeles;
    }
}


