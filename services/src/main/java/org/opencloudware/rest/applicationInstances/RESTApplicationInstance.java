package org.opencloudware.rest.applicationInstances;

import org.opencloudware.hibernate.model.ApplicationInstance;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 04/06/14.
 */
public class RESTApplicationInstance {

    private Long applicationTemplateId;

    private String id;

    public RESTApplicationInstance(ApplicationInstance applicationInstance) {
        this.id=applicationInstance.getId();
        this.applicationTemplateId=applicationInstance.getApplication().getId();

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getApplicationTemplateId() {
        return applicationTemplateId;
    }

    public void setApplicationTemplateId(Long applicationTemplateId) {
        this.applicationTemplateId = applicationTemplateId;
    }
}
