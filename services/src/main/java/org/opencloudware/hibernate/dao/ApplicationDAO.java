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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.organization.hibernate.HibernateListAccess;
import org.hibernate.Session;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.model.Application;
import org.opencloudware.utils.FileUploadService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

public class ApplicationDAO {
   public static final String queryFindApplicationById =
      "from org.opencloudware.hibernate.model.Application where id = :id";

   private HibernateService service_;

   private ExoCache<String, Application> cache_;

    private OcwDataService ocwDataService_;

    private FileUploadService fileUploadService_;


    public ApplicationDAO(HibernateService service, CacheService cservice,  OcwDataService ocwDataService, FileUploadService fileUploadService)
   {
      service_ = service;
      cache_ = cservice.getCacheInstance(Application.class.getName());
       ocwDataService_=ocwDataService;
       fileUploadService_=fileUploadService;

   }

   /**
    * {@inheritDoc}
    */
   public Application createApplicationInstance()
   {
      return new Application();
   }

   /**
    * {@inheritDoc}
    */
   public Application createApplicationInstance(String applicationName)
   {
      return new Application(applicationName);
   }

   /**
    * {@inheritDoc}
    */
   public void createApplication(Application application) throws Exception
   {
      final Session session = service_.openSession();
      session.save(application);
      ocwDataService_.getProjectDAO().invalidateCache(application.getProject());
       session.flush();
   }

   /**
    * {@inheritDoc}
    */
   public void saveApplication(Application application) throws Exception
   {
      Session session = service_.openSession();
      session.merge(application);
       ocwDataService_.getProjectDAO().invalidateCache(application.getProject());

       session.flush();
      cache_.put(application.getId().toString(), application);
   }

   /**
    * {@inheritDoc}
    */
   public Application removeApplication(String applicationId) throws Exception
   {
      Session session = service_.openSession();
      Application foundApplication = findApplicationById(applicationId, session);

      if (foundApplication == null)
         return null;

      session.delete(foundApplication);

       ocwDataService_.getProjectDAO().invalidateCache(foundApplication.getProject());
      session.flush();
       cache_.remove(applicationId);
      return foundApplication;
   }

   /**
    * {@inheritDoc}
    */
   public Application findApplicationById(String applicationId) throws Exception
   {
      Application application = cache_.get(applicationId);
      if (application != null)
         return application;
      Session session = service_.openSession();
      application = findApplicationById(applicationId, session);
      if (application != null)
         cache_.put(applicationId, application);
      return application;
   }

   public Application findApplicationById(String applicationId, Session session) throws Exception
   {
      Application application = (Application)service_.findOne(session, queryFindApplicationById, applicationId);
      return application;
   }

   /**
    * {@inheritDoc}
    */
   public LazyPageList<Application> getApplicationPageList(int pageSize) throws Exception
   {
      return new LazyPageList<Application>(findAllApplications(), pageSize);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<Application> findAllApplications() throws Exception
   {
      String findQuery = "from o in class " + Application.class.getName();
      String countQuery = "select count(o) from " + Application.class.getName() + " o";

      return new HibernateListAccess<Application>(service_, findQuery, countQuery);
   }




	public ListAccess<Application> findApplicationOfAProject(String projectId) throws Exception
	{
		String findQuery = "from o in class " + Application.class.getName() +" where PROJECT_ID = '"+projectId+"'";
        String countQuery = "select count(o) from " + Application.class.getName() + " o where PROJECT_ID = '"+projectId+"'";

        return new HibernateListAccess<Application>(service_, findQuery, countQuery);
	}

	/**
	 * {@inheritDoc}
	 */
	public LazyPageList<Application> getApplicationPageListByProject(int pageSize, String projectId) throws Exception
	{
		return new LazyPageList<Application>(findApplicationOfAProject(projectId), pageSize);
	}

    public void invalidateCache(Application application) {
        cache_.remove(application.getId());
    }
}
