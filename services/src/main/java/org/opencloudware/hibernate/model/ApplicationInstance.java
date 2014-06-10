package org.opencloudware.hibernate.model;


import org.exoplatform.services.database.DBObject;

/**
 * Created with IntelliJ IDEA.
 * User: Romain Denarie
 * Date: 19/08/13
 * Time: 14:11
 * To change this template use File | Settings | File Templates.
 */

public class ApplicationInstance extends DBObject {


	private Long id;

    private Application application;

	public ApplicationInstance() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String toString() {
		return "ApplicationInstance{" +
				"id='" + id+ '\'' +
				", applicationTemplate='" + application.getApplicationName()+'}';
	}

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}


