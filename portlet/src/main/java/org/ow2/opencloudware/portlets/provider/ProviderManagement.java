package org.ow2.opencloudware.portlets.provider;


import juzu.*;
import juzu.plugin.ajax.Ajax;
import juzu.request.RenderContext;
import juzu.template.Template;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.dao.ProviderIAASDAO;
import org.opencloudware.hibernate.model.Organization;
import org.opencloudware.hibernate.model.ProviderIAAS;
import org.ow2.opencloudware.portlets.common.Flash;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Romain Denarie
 * Date: 28/10/13
 * Time: 11:39
 * To change this template use File | Settings | File Templates.
 */
@SessionScoped
public class ProviderManagement {

	@Inject
	@Path("global.gtmpl")
	Template global;

	@Inject
	@Path("list.gtmpl")
	Template list;

	@Inject
	@Path("page.gtmpl")
	Template page;


	@Inject
	@Path("addProvider.gtmpl")
	Template addProvider;

	@Inject
	@Path("editProvider.gtmpl")
	Template editProvider;

    @Inject
    @Path("customForm.gtmpl")
    Template customForm;


    @Inject
    @Path("customEditForm.gtmpl")
    Template customEditForm;

    @Inject
	Flash flash;


    Map<String,List<String>> providerParameters;

	int currentPage;
	int nbPages;
	int nbResults;


	static int PAGE_SIZE = 10;


	OrganizationService organizationService_;
	OcwDataService ocwDataService_;
	String currentOrganizationId;



	@Inject
	public ProviderManagement(OrganizationService organizationService, OcwDataService ocwDataService) {
		organizationService_=organizationService;
		ocwDataService_=ocwDataService;
		currentPage = 1;

		if (currentOrganizationId == null) {
			currentOrganizationId = "";
		}

        providerParameters = new HashMap<String,List<String>>();


        List<String> parameters = new ArrayList<String>();
        //OpenStack
//        endpoint	 Keystone API endpoint
//        identity	 OpenStack user account login
//        credential	 OpenStack user account password
//        tenantName	 OpenStack tenant
        parameters.add("Endpoint");
        parameters.add("GlanceUrl");
        parameters.add("Identity");
        parameters.add("Credential");
        parameters.add("TenantName");


        providerParameters.put("openstack",parameters);

        parameters = new ArrayList<String>();
        //OpenStack
        //OpenStackters.clear();
//        .2 vCloud Director
//
//        Parameter 	Description
//        endpoint	 vCloud URL
        parameters.add("Endpoint");
//        identity	 user name
        parameters.add("Identity");
//        credential	 password
        parameters.add("Credential");
//        orgName	 organization name
        //pas de parametre, on le connait
//        vdcName	 vDC name
        parameters.add("VdcName");
//        publicNetworkName	 name of public organisation name
        parameters.add("PublicNetworkName");


        providerParameters.put("vcloud",parameters);


        parameters = new ArrayList<String>();
        //OpenStack
//        2.3 Amazon EC2
//
//        Parameter 	Description
//        identity	AWS Access Key ID
        parameters.add("Identity");
//        credential	 AWS Secret Key
        parameters.add("Credential");
        providerParameters.put("AmazonEC2",parameters);

        parameters = new ArrayList<String>();
        //OpenStack
//        2.4 CloudStack
//
//        Parameter 	Description
//        endpoint	 API endpoint
        parameters.add("Endpoint");
//        identity	API Key
        parameters.add("Identity");
//        credential	 Secret Key
        parameters.add("Credential");
        providerParameters.put("cloudstack",parameters);

        parameters = new ArrayList<String>();
        //OpenStack
//        2.1 Microsoft SPF
//
//        Parameter 	Description
//        endpoint	 API endpoint
        parameters.add("Endpoint");
//        identity	user name
        parameters.add("Identity");
//        credential	 password
        parameters.add("Credential");
        providerParameters.put("Microsoft SPF",parameters);



    }



	@View
	public void index(RenderContext renderContext) {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("flash", flash);
		global.render(parameters);
		flash.setError("");
		flash.setSuccess("");

	}


	@Ajax
	@Resource
	@Route("/changeOrganizationByGroupId/{groupId}")
	public void changeOrganizationByGroupId(String groupId) {

		try {
			Group group = organizationService_.getGroupHandler().findGroupById(groupId);
			if (group == null) {
				ProviderManagement_.index();

			} else {
				String organizationName = group.getLabel();

				Organization organizationObject = ocwDataService_.getOrganizationDAO().findOrganizationByName(organizationName);
				changeOrganization(organizationObject.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
			ProviderManagement_.index();
		}


	}


	@Ajax
	@Resource
	@Route("/changeOrganization/{organizationId}")
	public void changeOrganization(String organizationId) {

		currentOrganizationId = organizationId;

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("currentOrganizationId", currentOrganizationId);
		try {
			parameters.put("currentOrganizationName", ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId).getOrganizationName());
		} catch (Exception e) {
			parameters.put("currentOrganizationName", "");
		}
		list.render(parameters);
		flash.setError("");
		flash.setSuccess("");

	}


    @Ajax
    @Resource
    public void getCustomForm(String vendorName) {
        Map<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("providerParameters", providerParameters.get(vendorName));
        customForm.render(parameters);
    }

	@View
	public void displayAddForm() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("providerParameters", providerParameters);
        addProvider.render(parameters);
	}


	@Ajax
	@Resource
	@Route("/page/{pageToDisplay}")
	public void getPage(String pageToDisplay) {
		List<ProviderIAAS> currentList = new ArrayList<ProviderIAAS>();

		Integer pageWanted = new Integer(pageToDisplay);



		try {
			PageList pageList = ocwDataService_.getProviderIAASDAO().getProviderIAASPageListByOrganization(PAGE_SIZE, currentOrganizationId);
			pageList.setPageSize(PAGE_SIZE);

			nbPages = pageList.getAvailablePage();
			nbResults=pageList.getAvailable();
			currentPage=pageWanted;
			if (pageWanted>nbPages) currentPage=nbPages;
			if (pageWanted<1) currentPage=1;
			currentList= pageList.getPage(currentPage);


		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("currentList", currentList);
		parameters.put("nbResults", nbResults);
		parameters.put("nbPages", nbPages);
		parameters.put("currentPage", currentPage);
		parameters.put("pageSize", PAGE_SIZE);
		flash.setError("");
		flash.setSuccess("");
		parameters.put("flash", flash);
		page.render(parameters);
	}

	@Action
	@Route("/deleteProvider/{inputProviderId}")
	public Response deleteProvider(String inputProviderId) {

		try {

			ProviderIAASDAO providerDAO = ocwDataService_.getProviderIAASDAO();
			ProviderIAAS provider = providerDAO.findProviderById(inputProviderId);
			if (provider == null) {
				flash.setError("This provider doesn't exist.");
				flash.setSuccess("");
				return ProviderManagement_.index();
			}

			providerDAO.removeProvider(inputProviderId);


			flash.setSuccess("Provider \""+provider.getProviderIAASName()+"\" removed.");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return ProviderManagement_.index();

	}

	@View
	public void displayEditForm(String inputProviderId) {
		ProviderIAAS provider = null;
		try {


			ProviderIAASDAO providerIAASDAODAO = ocwDataService_.getProviderIAASDAO();
			provider = providerIAASDAODAO.findProviderById(inputProviderId);

		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		if (provider==null) {
			flash.setError("Provider with id \""+inputProviderId+"\" not founded.");
			ProviderManagement_.index();
		} else {
			Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("providerParameters", providerParameters);
            parameters.put("provider", provider);
			editProvider.render(parameters);
		}
	}

    @Ajax
    @Resource
    public void getCustomEditForm(String vendorName) {

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("providerParameters", providerParameters.get(vendorName));
            customEditForm.render(parameters);
    }

	@Action
	@Route("/createProvider")
	public Response.View createProvider(String inputProviderName,
											String inputProviderIdentity,
											String inputProviderCredential,
											String inputProviderVendor,
											String inputProviderEndpoint,
                                            String inputProviderGlanceUrl,
                                            String inputProviderTenantName,
                                            String inputProviderPublicNetworkName, String inputProviderVdcName) {


		try {



			ProviderIAASDAO providerIAASDAO= ocwDataService_.getProviderIAASDAO();
            ProviderIAAS provider = providerIAASDAO.createProviderIAASInstance();
            provider.setProviderIAASLogin(inputProviderIdentity);
            provider.setProviderIAASName(inputProviderName);
            provider.setProviderIAASVendor(inputProviderVendor);
            provider.setProviderIAASPassword(inputProviderCredential);
            provider.setProviderEndPoint(inputProviderEndpoint);
            provider.setProviderPublicNetworkName(inputProviderPublicNetworkName);
            provider.setProviderTenantName(inputProviderTenantName);
            provider.setProviderGlanceUrl(inputProviderGlanceUrl);

            provider.setVdcName(inputProviderVdcName);




			Organization organization = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);
			provider.setOrganization(organization);
			if (!providerIAASDAO.createProviderIAAS(provider)) {
                flash.setError("Service provider creation not reachable");
            } else {

                flash.setSuccess("Provider \"" + inputProviderName + "\" added.");
            }

		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            flash.setError("Unable to create Provider");
		}

		return ProviderManagement_.index();

	}


	@Action
	@Route("/editProvider")
	public Response.View editProvider(String inputProviderId,String oldProviderName,
                                      String inputProviderName,
                                      String inputProviderIdentity,
                                      String inputProviderCredential,
                                      String inputProviderVendor,
                                      String inputProviderEndpoint,
                                      String inputProviderGlanceUrl,
                                      String inputProviderTenantName,
                                      String inputProviderPublicNetworkName, String inputVdcName) {




		try {

			ProviderIAASDAO providerIAASDAO= ocwDataService_.getProviderIAASDAO();
			ProviderIAAS oldProvider = providerIAASDAO.findProviderById(inputProviderId);
			if (oldProvider == null) {
				flash.setError("This provider \""+oldProviderName+"\" doesn't exist.");
				flash.setSuccess("");
				return ProviderManagement_.index();
			}


            oldProvider.setProviderIAASLogin(inputProviderIdentity);
            oldProvider.setProviderIAASName(inputProviderName);
            oldProvider.setProviderIAASVendor(inputProviderVendor);
            oldProvider.setProviderIAASPassword(inputProviderCredential);
            oldProvider.setProviderEndPoint(inputProviderEndpoint);
            oldProvider.setProviderPublicNetworkName(inputProviderPublicNetworkName);
            oldProvider.setProviderTenantName(inputProviderTenantName);
            oldProvider.setProviderGlanceUrl(inputProviderGlanceUrl);
            oldProvider.setVdcName(inputVdcName);

			providerIAASDAO.saveProviderIAAS(oldProvider);

			flash.setSuccess("Provider \""+inputProviderName+"\" modified.");
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		return ProviderManagement_.index();
	}





}
