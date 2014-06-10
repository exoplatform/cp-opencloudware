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
import org.opencloudware.hibernate.model.Application;
import org.opencloudware.hibernate.model.ApplicationInstance;

public class ApplicationInstanceDAO {
   public static final String queryFindApplicationInstanceById =
      "from org.opencloudware.hibernate.model.ApplicationInstance where id = :id";

   private HibernateService service_;

   private ExoCache<String, ApplicationInstance> cache_;

   public ApplicationInstanceDAO(HibernateService service, CacheService cservice)
   {
      service_ = service;
      cache_ = cservice.getCacheInstance(ApplicationInstance.class.getName());
   }

   /**
    * {@inheritDoc}
    */
   public ApplicationInstance createApplicationInstance()
   {
      return new ApplicationInstance();
   }

   /**
    * {@inheritDoc}
    */
   public void createInstanceApplication(ApplicationInstance applicationInstance) throws Exception
   {
      final Session session = service_.openSession();
      session.save(applicationInstance);
      session.flush();
   }

   /**
    * {@inheritDoc}
    */
   public void saveApplicationInstance(ApplicationInstance applicationInstance) throws Exception
   {
      Session session = service_.openSession();
      session.merge(applicationInstance);
      session.flush();
      cache_.put(applicationInstance.getId().toString(), applicationInstance);
   }

   /**
    * {@inheritDoc}
    */
   public ApplicationInstance removeApplicationInstance(String applicationInstanceId) throws Exception
   {
      Session session = service_.openSession();
      ApplicationInstance foundInstance= findApplicationInstanceById(applicationInstanceId, session);

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
   public ApplicationInstance findApplicationInstanceById(String applicationInstanceId) throws Exception
   {
      ApplicationInstance applicationInstance = cache_.get(applicationInstanceId);
      if (applicationInstance != null)
         return applicationInstance;
      Session session = service_.openSession();
      applicationInstance = findApplicationInstanceById(applicationInstanceId, session);
      if (applicationInstance != null)
         cache_.put(applicationInstanceId, applicationInstance);
      return applicationInstance;
   }

   public ApplicationInstance findApplicationInstanceById(String applicationInstanceId, Session session) throws Exception
   {
       ApplicationInstance applicationInstance = (ApplicationInstance)service_.findOne(session, queryFindApplicationInstanceById, applicationInstanceId);
      return applicationInstance;
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

}
