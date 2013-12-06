package org.opencloudware.hibernate;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.database.HibernateService;
import org.opencloudware.hibernate.dao.ApplicationDAO;
import org.opencloudware.hibernate.dao.OrganizationDAO;
import org.opencloudware.hibernate.dao.ProjectDAO;
import org.opencloudware.hibernate.dao.ProviderIAASDAO;
import org.picocontainer.Startable;

/**
 * Created with IntelliJ IDEA.
 * User: Romain Denarie
 * Date: 19/08/13
 * Time: 14:41
 * To change this template use File | Settings | File Templates.
 */
public class OcwDataService  implements Startable {


	private OrganizationDAO organizationDAO;
	private ProviderIAASDAO providerIAASDAO;
	private ProjectDAO projectDAO;
	private ApplicationDAO applicationDAO;

	public OcwDataService(HibernateService hibernateService, CacheService cacheService) {

		organizationDAO = new OrganizationDAO(hibernateService, cacheService);
		providerIAASDAO = new ProviderIAASDAO(hibernateService, cacheService);
		applicationDAO = new ApplicationDAO(hibernateService, cacheService);
		projectDAO = new ProjectDAO(hibernateService,cacheService);

	}

	public OrganizationDAO getOrganizationDAO() {
		return organizationDAO;
	}


	public ApplicationDAO getApplicationDAO() {
		return applicationDAO;
	}

	public ProviderIAASDAO getProviderIAASDAO() {
		return providerIAASDAO;
	}

	@Override
	public void start() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void stop() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public ProjectDAO getProjectDAO() {
		return projectDAO;
	}

}
