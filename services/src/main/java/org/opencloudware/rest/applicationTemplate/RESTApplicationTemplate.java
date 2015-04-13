package org.opencloudware.rest.applicationTemplate;

import org.opencloudware.hibernate.model.Application;
import org.opencloudware.hibernate.model.ApplicationInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 04/06/14.
 */
public class RESTApplicationTemplate {

    private String applicationName = null;

    private String description = null;
    private Long projectId;

    private Long id;

    private byte[] modele;
    private byte[] rules;

    private Map<String, byte[]> alternativeModeles=
            new HashMap<String, byte[]>(0);

    private List<String> applicationsInstanceId =
            new ArrayList<String>(0);

    public RESTApplicationTemplate(Application applicationTemplate) {
        this.applicationName=applicationTemplate.getApplicationName();
        this.description=applicationTemplate.getDescription();
        this.projectId=applicationTemplate.getProject().getId();
        this.id=applicationTemplate.getId();
        this.modele=applicationTemplate.getModele();
        this.rules=applicationTemplate.getRules();
        this.alternativeModeles.putAll(applicationTemplate.getAlternativeModeles());

        //TODO get les ids des instances
        for (ApplicationInstance applicationInstance : applicationTemplate.getApplicationsInstance()) {
            applicationsInstanceId.add(applicationInstance.getId());
        }

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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getApplicationsInstanceId() {
        return applicationsInstanceId;
    }

    public void setApplicationsInstanceId(List<String> applicationsInstanceId) {
        this.applicationsInstanceId = applicationsInstanceId;
    }

    public byte[] getModele() {
        return modele;
    }

    public void setModele(byte[] modele) {
        this.modele = modele;
    }

    public Map<String, byte[]> getAlternativeModeles() {
        return alternativeModeles;
    }

    public void setAlternativeModeles(Map<String, byte[]> alternativeModeles) {
        this.alternativeModeles = alternativeModeles;
    }

    public byte[] getRules() {
        return rules;
    }

    public void setRules(byte[] rules) {
        this.rules = rules;
    }
}
