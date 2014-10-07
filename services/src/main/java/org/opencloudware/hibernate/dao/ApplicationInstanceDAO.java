/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.opencloudware.hibernate.dao;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.organization.hibernate.HibernateListAccess;
import org.hibernate.Session;
import org.json.JSONObject;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.model.ApplicationInstance;
import org.opencloudware.rest.OCWUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationInstanceDAO {
    public static final String queryFindApplicationInstanceById =
            "from org.opencloudware.hibernate.model.ApplicationInstance where id = :id";

    private HibernateService service_;

    private ExoCache<String, ApplicationInstance> cache_;

    private OcwDataService ocwDataService_;


    public ApplicationInstanceDAO(HibernateService service, CacheService cservice, OcwDataService ocwDataService) {
        service_ = service;
        cache_ = cservice.getCacheInstance(ApplicationInstance.class.getName());
        ocwDataService_ = ocwDataService;
    }

    /**
     * {@inheritDoc}
     */
    public ApplicationInstance createApplicationInstance() {
        return new ApplicationInstance();
    }

    /**
     * {@inheritDoc}
     */
    public boolean createInstanceApplication(ApplicationInstance applicationInstance) throws Exception {
        final Session session = service_.openSession();

        //1) recuperation de la ressource deployment
        JSONObject resource = OCWUtil.getResource("orchestrator");
        if (resource == null) return false;
        String resourceEndPoint = resource.getString("resourceEndpoint");
        String endPointURI = "/deploy";
        String applicationName = applicationInstance.getApplication().getApplicationName();
        String modele = new String(applicationInstance.getApplication().getModele());
        String postData = "applicationName=" + applicationName + "&applicationDesc=" + modele;

        if (applicationInstance.getProviderIAASId() != null && !applicationInstance.getProviderIAASId().equals("")) {
            postData = postData + "&iaasId=" + applicationInstance.getProviderIAASId();
        }

        String username=resource.getString("resourceLogin").equals("null") ? null : resource.getString("resourceLogin");
        String password=resource.getString("resourcePassword").equals("null") ? null : resource.getString("resourcePassword");
        Map<String,String> headers= new HashMap<String,String>();


        System.out.println("send  deploy request");
        String responseData = OCWUtil.doPost(resourceEndPoint + endPointURI, username, password, postData,headers,"text/plain; charset=utf8");
        System.out.println("receive deploy response");
        if (responseData == null) return false;

        System.out.println(responseData);

        Pattern regex = Pattern.compile("<id>(.*)</id>");
        Matcher parser = regex.matcher(responseData);
        if (parser.find()) {
            String id = parser.group(1);
            applicationInstance.setId(id);
            //applicationInstance.setModeleAtRuntime(responseData.getBytes());

//
            session.save(applicationInstance);
            ocwDataService_.getApplicationDAO().invalidateCache(applicationInstance.getApplication());
            session.flush();
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void saveApplicationInstance(ApplicationInstance applicationInstance) throws Exception {
        Session session = service_.openSession();
        session.merge(applicationInstance);
        ocwDataService_.getApplicationDAO().invalidateCache(applicationInstance.getApplication());

        session.flush();
        cache_.put(applicationInstance.getId().toString(), applicationInstance);
    }

    /**
     * {@inheritDoc}
     */
    public ApplicationInstance removeApplicationInstance(String applicationInstanceId) throws Exception {
        Session session = service_.openSession();
        ApplicationInstance foundInstance = findApplicationInstanceById(applicationInstanceId, session);

        if (foundInstance == null)
            return null;

        session.delete(foundInstance);
        session.flush();
        cache_.remove(applicationInstanceId);
        return foundInstance;
    }

    /**
     * {@inheritDoc}
     */
    public ApplicationInstance findApplicationInstanceById(String applicationInstanceId) throws Exception {
        ApplicationInstance applicationInstance = cache_.get(applicationInstanceId);
        if (applicationInstance != null)
            return applicationInstance;
        Session session = service_.openSession();
        applicationInstance = findApplicationInstanceById(applicationInstanceId, session);
        if (applicationInstance != null)
            cache_.put(applicationInstanceId, applicationInstance);
        return applicationInstance;
    }

    public ApplicationInstance findApplicationInstanceById(String applicationInstanceId, Session session) throws Exception {
        ApplicationInstance applicationInstance = (ApplicationInstance) service_.findOne(session, queryFindApplicationInstanceById, applicationInstanceId);
        return applicationInstance;
    }


    public ListAccess<ApplicationInstance> findApplicationInstancePageListByApplication(String applicationId) {
        String findQuery = "from o in class " + ApplicationInstance.class.getName() + " where APPLICATION_ID = '" + applicationId + "'";
        String countQuery = "select count(o) from " + ApplicationInstance.class.getName() + " o where APPLICATION_ID = '" + applicationId + "'";

        return new HibernateListAccess<ApplicationInstance>(service_, findQuery, countQuery);
    }


    /**
     * {@inheritDoc}
     */
    public LazyPageList<ApplicationInstance> getApplicationInstancePageListByApplication(int pageSize, String applicationId) throws Exception {
        return new LazyPageList<ApplicationInstance>(findApplicationInstancePageListByApplication(applicationId), pageSize);
    }
}


