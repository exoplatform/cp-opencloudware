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


	private String id;

    private Application application;

    private String providerIAASId;

    private byte[] modeleAtRuntime;

	public ApplicationInstance() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String toString() {
		return "ApplicationInstance{" +
				"id='" + id+ '\'' +
                //", applicationTemplate='" + application.getApplicationName()+
                "}";
	}

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getProviderIAASId() {
        return providerIAASId;
    }

    public void setProviderIAASId(String providerIAASId) {
        this.providerIAASId = providerIAASId;
    }

    public byte[] getModeleAtRuntime() {
        return modeleAtRuntime;
    }

    public void setModeleAtRuntime(byte[] modeleAtRuntime) {
        this.modeleAtRuntime = modeleAtRuntime;
    }
}


