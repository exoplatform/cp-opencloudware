package org.opencloudware.hibernate;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.database.HibernateService;
import org.opencloudware.hibernate.dao.*;
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
	private ApplicationInstanceDAO applicationInstanceDAO;
    private ApplicationDAO applicationDAO;

	public OcwDataService(HibernateService hibernateService, CacheService cacheService) {

		organizationDAO = new OrganizationDAO(hibernateService, cacheService);
		providerIAASDAO = new ProviderIAASDAO(hibernateService, cacheService, this);
		applicationDAO = new ApplicationDAO(hibernateService, cacheService, this);
		projectDAO = new ProjectDAO(hibernateService,cacheService, this);
        applicationInstanceDAO = new ApplicationInstanceDAO(hibernateService,cacheService, this);

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

    public ApplicationInstanceDAO getApplicationInstanceDAO() {
        return applicationInstanceDAO;
    }

    public void setApplicationInstanceDAO(ApplicationInstanceDAO applicationInstanceDAO) {
        this.applicationInstanceDAO = applicationInstanceDAO;
    }
}
