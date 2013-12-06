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
import org.exoplatform.services.database.ObjectQuery;
import org.exoplatform.services.organization.hibernate.HibernateListAccess;
import org.hibernate.Session;
import org.opencloudware.hibernate.model.Organization;
public class OrganizationDAO {
   public static final String queryFindOrganizationByName =
      "from org.opencloudware.hibernate.model.Organization where organizationName = :id";

	public static final String queryFindOrganizationById =
			"from org.opencloudware.hibernate.model.Organization where id = :id";
   private HibernateService service_;

   private ExoCache<String, Organization> cache_;

   public OrganizationDAO(HibernateService service, CacheService cservice)
   {
      service_ = service;
      cache_ = cservice.getCacheInstance(Organization.class.getName());
   }

   /**
    * {@inheritDoc}
    */
   public Organization createOrganizationInstance()
   {
      return new Organization();
   }

   /**
    * {@inheritDoc}
    */
   public Organization createOrganizationInstance(String orgName)
   {
      return new Organization(orgName);
   }

   /**
    * {@inheritDoc}
    */
   public void createOrganization(Organization organization) throws Exception
   {
      final Session session = service_.openSession();
      session.save(organization);
      session.flush();
   }

   /**
    * {@inheritDoc}
    */
   public void saveOrganization(Organization organization) throws Exception
   {
      Session session = service_.openSession();
      session.merge(organization);
      session.flush();
      cache_.put(organization.getOrganizationName(), organization);
	  cache_.put(organization.getId(), organization);
   }

   /**
    * {@inheritDoc}
    */
   public Organization removeOrganization(String organizationId) throws Exception
   {
      Session session = service_.openSession();
      Organization foundOrganization = findOrganizationById(organizationId, session);

      if (foundOrganization == null)
         return null;

      session.delete(foundOrganization);
      session.flush();
      cache_.remove(organizationId);
      return foundOrganization;
   }

   /**
    * {@inheritDoc}
    */
   public Organization findOrganizationByName(String organizationName) throws Exception
   {
      Organization organization = cache_.get(organizationName);
      if (organization != null)
         return organization;
      Session session = service_.openSession();
      organization = findOrganizationByName(organizationName, session);
      if (organization != null)
         cache_.put(organizationName, organization);
      return organization;
   }


	/**
	 * {@inheritDoc}
	 */
	public Organization findOrganizationById(String organizationId) throws Exception
	{
		Organization organization = cache_.get(organizationId);
		if (organization != null)
			return organization;
		Session session = service_.openSession();
		organization = findOrganizationById(organizationId, session);
		if (organization != null)
			cache_.put(organizationId, organization);
		return organization;
	}


	public Organization findOrganizationByName(String organizationName, Session session) throws Exception
   {
      Organization organization = (Organization)service_.findOne(session, queryFindOrganizationByName, organizationName);
      return organization;
   }
	public Organization findOrganizationById(String organizationId, Session session) throws Exception
	{
		Organization organization = (Organization)service_.findOne(session, queryFindOrganizationById, organizationId);
		return organization;
	}

   /**
    * {@inheritDoc}
    */
   public LazyPageList<Organization> getOrganizationPageList(int pageSize) throws Exception
   {
      return new LazyPageList<Organization>(findAllOrganizations(), pageSize);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<Organization> findAllOrganizations() throws Exception
   {
      String findQuery = "from o in class " + Organization.class.getName();
      String countQuery = "select count(o) from " + Organization.class.getName() + " o";

      return new HibernateListAccess<Organization>(service_, findQuery, countQuery);
   }


   /**
    * {@inheritDoc}
    */
   public LazyPageList<Organization> findOrganizations(Query q) throws Exception
   {
      return new LazyPageList<Organization>(findOrganizationsByQuery(q), 20);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<Organization> findOrganizationsByQuery(Query q) throws Exception
   {
      ObjectQuery oq = new ObjectQuery(Organization.class);
      if (q.getOrganizationName_() != null)
      {
         oq.addLIKE("UPPER(organizationName)", addAsterisk(q.getOrganizationName_().toUpperCase()));
      }
      if (q.getCreditCardNumber() != null)
      {
         oq.addLIKE("UPPER(creditCardNumber)", q.getCreditCardNumber().toUpperCase());
      }
      if (q.getGroupId() != null)
      {
         oq.addLIKE("UPPER(groupId)", q.getGroupId().toUpperCase());
      }
	   if (q.getAddress() != null)
	   {
		   oq.addLIKE("UPPER(address)", q.getAddress().toUpperCase());
	   }

      return new HibernateListAccess<Organization>(service_, oq.getHibernateQueryWithBinding(),
         oq.getHibernateCountQueryWithBinding(), oq.getBindingFields());
   }


   private String addAsterisk(String s)
   {
      StringBuffer sb = new StringBuffer(s);
      if (!s.startsWith("*"))
      {
         sb.insert(0, "*");
      }
      if (!s.endsWith("*"))
      {
         sb.append("*");
      }

      return sb.toString();

   }

}
