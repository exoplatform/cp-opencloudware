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
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.model.Project;

public class ProjectDAO {
	public static final String queryFindProjectByName =
			"from org.opencloudware.hibernate.model.Project where projectName = :id";
	public static final String queryFindProjectById =
			"from org.opencloudware.hibernate.model.Project where id = :id";

	private HibernateService service_;

   private ExoCache<Long, Project> cache_;

    private OcwDataService ocwDataService_;


    public ProjectDAO(HibernateService service, CacheService cservice, OcwDataService ocwDataService)
   {
      service_ = service;
      cache_ = cservice.getCacheInstance(Project.class.getName());
       ocwDataService_=ocwDataService;

   }

   /**
    * {@inheritDoc}
    */
   public Project createProjectInstance()
   {
      return new Project();
   }

   /**
    * {@inheritDoc}
    */
   public Project createProjectInstance(String projectName)
   {
      return new Project(projectName);
   }

   /**
    * {@inheritDoc}
    */
   public void createProject(Project project) throws Exception
   {
      final Session session = service_.openSession();
      session.save(project);
       ocwDataService_.getOrganizationDAO().invalidateCache(project.getOrganization());

       session.flush();
   }

   /**
    * {@inheritDoc}
    */
   public void saveProject(Project project) throws Exception
   {
      Session session = service_.openSession();
      session.merge(project);
       ocwDataService_.getOrganizationDAO().invalidateCache(project.getOrganization());

       session.flush();
      cache_.put(project.getId(), project);
   }

   /**
    * {@inheritDoc}
    */
   public Project removeProject(String projectId) throws Exception
   {
      Session session = service_.openSession();
      Project foundProject = findProjectById(projectId, session);

      if (foundProject == null)
         return null;


      session.delete(foundProject);

       ocwDataService_.getOrganizationDAO().invalidateCache(foundProject.getOrganization());

       session.flush();
      cache_.remove(projectId);
      return foundProject;
   }


   /**
    * {@inheritDoc}
    */
   public LazyPageList<Project> getProjectPageList(int pageSize) throws Exception
   {
      return new LazyPageList<Project>(findAllProjects(), pageSize);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<Project> findAllProjects() throws Exception
   {
      String findQuery = "from o in class " + Project.class.getName();
      String countQuery = "select count(o) from " + Project.class.getName() + " o";

      return new HibernateListAccess<Project>(service_, findQuery, countQuery);
   }

	public ListAccess<Project> findProjectsOfAnOrganization(String organizationId) throws Exception
	{
		String findQuery = "from o in class " + Project.class.getName() +" where ORGANIZATION_ID = '"+organizationId+"'";
		String countQuery = "select count(o) from " + Project.class.getName() + " o where ORGANIZATION_ID = '"+organizationId+"'";

		return new HibernateListAccess<Project>(service_, findQuery, countQuery);
	}

	/**
	 * {@inheritDoc}
	 */
	public LazyPageList<Project> getProjectPageListByOrganization(int pageSize, String organizationId) throws Exception
	{
		return new LazyPageList<Project>(findProjectsOfAnOrganization(organizationId), pageSize);
	}

	/**
	 * {@inheritDoc}
	 */
	public Project findProjectById(String projectId) throws Exception
	{
		Project project = cache_.get(projectId);
		if (project != null)
			return project;
		Session session = service_.openSession();
		project = findProjectById(projectId, session);
		if (project != null)
			cache_.put(project.getId(), project);
		return project;
	}

	public Project findProjectById(String projectId, Session session) throws Exception
	{
		Project project = (Project)service_.findOne(session, queryFindProjectById, projectId);
		return project;
	}


    public void invalidateCache(Project project) {
        cache_.remove(project.getId());
    }
}
