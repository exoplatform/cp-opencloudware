package org.opencloudware.rest.resources;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 03/06/14.
 */
public class ResourcesDefinition {

    private List<OCWResource> resources=new ArrayList<OCWResource>();


    public List<OCWResource> getResources() { return resources; }

    public void setResources(List<OCWResource> resources) {
        this.resources = resources;
    }

}
