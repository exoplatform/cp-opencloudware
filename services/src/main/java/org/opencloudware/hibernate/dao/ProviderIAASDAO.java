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
import org.opencloudware.hibernate.model.ProviderIAAS;
import org.opencloudware.rest.OCWUtil;

import java.util.HashMap;
import java.util.Map;

public class ProviderIAASDAO {


	public static final String queryFindProviderById =
			"from org.opencloudware.hibernate.model.ProviderIAAS where id = :id";

   private HibernateService service_;

   private ExoCache<String, ProviderIAAS> cache_;

    private OcwDataService ocwDataService_;

   public ProviderIAASDAO(HibernateService service, CacheService cservice, OcwDataService ocwDataService)
   {
      service_ = service;
      cache_ = cservice.getCacheInstance(ProviderIAAS.class.getName());
       ocwDataService_=ocwDataService;
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
   public boolean createProviderIAAS(ProviderIAAS providerIAAS) throws Exception
   {
      final Session session = service_.openSession();

       //1) recuperation de la ressource multi-cloud-iaas
       JSONObject resource = OCWUtil.getResource("multi-cloud-iaas");
        if (resource==null) return false;
       String resourceEndPoint=resource.getString("resourceEndpoint");
       String endPointURI="/providers/accounts";
       JsonProviderObject jsonProviderObject = new JsonProviderObject(providerIAAS);
       String username=resource.getString("resourceLogin").equals("null") ? null : resource.getString("resourceLogin");
       String password=resource.getString("resourcePassword").equals("null") ? null : resource.getString("resourcePassword");
       Map<String,String> headers= new HashMap<String,String>();
       headers.put("tenantName","trial");
       System.out.println("Will send post data on "+resourceEndPoint+endPointURI+"with username="+username+", data="+jsonProviderObject.toString());

       String responseData = OCWUtil.doPostCreateProvider(resourceEndPoint+endPointURI,username,password,jsonProviderObject.toString(),headers,"application/json; charset=utf8");
       if (responseData==null) return false;
       JSONObject result = new JSONObject(responseData);
       String providerId=result.getString("id");
       providerIAAS.setId(providerId);

//il faut aller chercher l'id du provider
//            Provider creation
//
//            This operation creates a new IaaS provider account.
//
//            Method	Endpoint URI	Description
//            POST	 /providers/accounts	Provider Account creation
//                    Request
//
//            {
//                "name": "OW2Stack",
//                "description": "OW2Stack",
//                "type": "openstack",
//                "endpoint": "http://ow2-04.xsalto.net:5000/v2.0",
//                "location":
//                {
//                    "iso3166_1": "FR",
//                    "countryName": "France"
//                },
//
//                "identity": "sirocco",
//                "credential": "cregDyk3",
//                "properties":
//                {
//                    "tenantName": "opencloudware"
//                }
//            }
//            Response
//
//            {
//                "id" : "0e177df7-0f51-4c2b-9aee-f20542c2980b",
//                    "href" : "http://localhost:8080/cimi/providers/9623452f-0888-4ad8-83e6-49c8a14e1654/accounts/0e177df7-0f51-4c2b-9aee-f20542c2980b",
//                    "properties" : {
//                "tenantName" : "opencloudware"
//            },
//                "providerId" : "9623452f-0888-4ad8-83e6-49c8a14e1654",
//                    "identity" : "sirocco",
//                    "credential" : "cregDyk3"
//            }




      session.save(providerIAAS);
      ocwDataService_.getOrganizationDAO().invalidateCache(providerIAAS.getOrganization());

       session.flush();
       return true;
   }

   /**
    * {@inheritDoc}
    */
   public void saveProviderIAAS(ProviderIAAS providerIAAS) throws Exception
   {
      Session session = service_.openSession();
      session.merge(providerIAAS);
       ocwDataService_.getOrganizationDAO().invalidateCache(providerIAAS.getOrganization());

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
        ocwDataService_.getOrganizationDAO().invalidateCache(foundProvider.getOrganization());
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


    public class JsonProviderObject extends JSONObject {
//        {
//                "name": "OW2Stack",
//                "description": "OW2Stack",
//                "type": "openstack",
//                "endpoint": "http://ow2-04.xsalto.net:5000/v2.0",
//                "location":
//                {
//                    "iso3166_1": "FR",
//                    "countryName": "France"
//                },
//
//                "identity": "sirocco",
//                "credential": "cregDyk3",
//                "properties":
//                {
//                    "tenantName": "opencloudware"
//                }
//            }



        public JsonProviderObject(ProviderIAAS provider) {
            try {
                this.put("name", provider.getProviderIAASName());
                this.put("description", provider.getProviderIAASName());
                this.put("type", provider.getProviderIAASVendor());
                this.put("endpoint",provider.getProviderEndPoint());
                this.put("identity",provider.getProviderIAASLogin());
                this.put("credential",provider.getProviderIAASPassword());
                Map<String,String> properties=new HashMap<String,String>();
                if (provider.getProviderTenantName() != null) {
                    properties.put("tenantName", provider.getProviderTenantName());
                }
                if (provider.getProviderPublicNetworkName() != null) {

                    properties.put("publicNetworkName", provider.getProviderPublicNetworkName());
                    properties.put("orgName", provider.getProviderOrganizationName());
                    properties.put("vdcName",provider.getVdcName());

                }
                if (provider.getProviderSmaEndpoint() != null) {
                    properties.put("smaEndpoint", provider.getProviderSmaEndpoint());
                    properties.put("smaLogin", provider.getProviderSmaLogin());
                    properties.put("smaPassword", provider.getProviderSmaPassword());

                }

                Map<String,String> location = new HashMap<String, String>();
                location.put("iso3166_1","FR");
                location.put("countryName","France");
                this.put("location",location);

                this.put("properties",properties);
            } catch (Exception e) {
                e.printStackTrace();

            }

        }

    }


}
