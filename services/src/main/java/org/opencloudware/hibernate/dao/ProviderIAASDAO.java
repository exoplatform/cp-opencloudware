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
import org.opencloudware.hibernate.model.ProviderIAAS;

public class ProviderIAASDAO {


	public static final String queryFindProviderById =
			"from org.opencloudware.hibernate.model.ProviderIAAS where id = :id";

   private HibernateService service_;

   private ExoCache<Long, ProviderIAAS> cache_;

   public ProviderIAASDAO(HibernateService service, CacheService cservice)
   {
      service_ = service;
      cache_ = cservice.getCacheInstance(ProviderIAAS.class.getName());
   }

   /**
    * {@inheritDoc}
    */
   public ProviderIAAS createProviderIAASInstance()
   {
      return new ProviderIAAS();
   }

   /**
    * {@inheritDoc}
    */
   public void createProviderIAAS(ProviderIAAS providerIAAS) throws Exception
   {
      final Session session = service_.openSession();
      session.save(providerIAAS);
      session.flush();
   }

   /**
    * {@inheritDoc}
    */
   public void saveProviderIAAS(ProviderIAAS providerIAAS) throws Exception
   {
      Session session = service_.openSession();
      session.merge(providerIAAS);
      session.flush();
      cache_.put(providerIAAS.getId(), providerIAAS);
   }


   /**
    * {@inheritDoc}
    */
   public LazyPageList<ProviderIAAS> getProviderIAASPageList(int pageSize) throws Exception
   {
      return new LazyPageList<ProviderIAAS>(findAllProviderIAAS(), pageSize);
   }

   /**
    * {@inheritDoc}
    */
   public ListAccess<ProviderIAAS> findAllProviderIAAS() throws Exception
   {
      String findQuery = "from o in class " + ProviderIAAS.class.getName();
      String countQuery = "select count(o) from " + ProviderIAAS.class.getName() + " o";

      return new HibernateListAccess<ProviderIAAS>(service_, findQuery, countQuery);
   }

	public ListAccess<ProviderIAAS> findProvidersOfAnOrganization(String organizationId) throws Exception
	{
		String findQuery = "from o in class " + ProviderIAAS.class.getName() +" where ORGANIZATION_ID = '"+organizationId+"'";
		String countQuery = "select count(o) from " + ProviderIAAS.class.getName() + " o where ORGANIZATION_ID = '"+organizationId+"'";

		return new HibernateListAccess<ProviderIAAS>(service_, findQuery, countQuery);
	}

	/**
	 * {@inheritDoc}
	 */
	public LazyPageList<ProviderIAAS> getProviderIAASPageListByOrganization(int pageSize, String organizationId) throws Exception
	{
		return new LazyPageList<ProviderIAAS>(findProvidersOfAnOrganization(organizationId), pageSize);
	}


	/**
	 * {@inheritDoc}
	 */
	public ProviderIAAS removeProvider(String providerId) throws Exception
	{
		Session session = service_.openSession();
		ProviderIAAS foundProvider = findProviderById(providerId, session);

		if (foundProvider == null)
			return null;

		session.delete(foundProvider);
		session.flush();
		cache_.remove(providerId);
		return foundProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	public ProviderIAAS findProviderById(String providerId) throws Exception
	{
		ProviderIAAS provider = cache_.get(providerId);
		if (provider != null)
			return provider;
		Session session = service_.openSession();
		provider = findProviderById(providerId, session);
		if (provider != null)
			cache_.put(provider.getId(), provider);
		return provider;
	}

	public ProviderIAAS findProviderById(String providerId, Session session) throws Exception
	{
		ProviderIAAS provider = (ProviderIAAS)service_.findOne(session, queryFindProviderById, providerId);
		return provider;
	}



}
