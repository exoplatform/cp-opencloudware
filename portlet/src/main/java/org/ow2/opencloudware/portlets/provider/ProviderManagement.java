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
	Flash flash;

	int currentPage;
	int nbPages;
	int nbResults;


	static int PAGE_SIZE = 3;


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


	@View
	public void displayAddForm() {
		addProvider.render();
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
			parameters.put("provider", provider);
			editProvider.render(parameters);
		}
	}

	@Action
	@Route("/createProvider")
	public Response.View createProvider(String inputProviderName,
											String inputProviderLogin,
											String inputProviderPassword,
											String inputProviderVendor,
											String inputConfirmPassword) {


		try {

			ProviderIAASDAO providerIAASDAO= ocwDataService_.getProviderIAASDAO();


			ProviderIAAS provider = providerIAASDAO.createProviderIAASInstance();
			provider.setProviderIAASLogin(inputProviderLogin);
			provider.setProviderIAASName(inputProviderName);
			provider.setProviderIAASVendor(inputProviderVendor);
			if (!inputProviderPassword.equals(inputConfirmPassword)) {
				flash.setError("Password and confirm password don't match.");
				flash.setSuccess("");
				return ProviderManagement_.index();
			}
			provider.setProviderIAASPassword(inputProviderPassword);

			Organization organization = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);
			provider.setOrganization(organization);
			providerIAASDAO.createProviderIAAS(provider);

			flash.setSuccess("Provider \""+inputProviderName+"\" added.");
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}


		return ProviderManagement_.index();

	}


	@Action
	@Route("/editProvider")
	public Response.View editProvider(String inputProviderId,String oldProviderName,
										  String inputProviderName,
										  String inputProviderLogin,
										  String inputProviderVendor,
										  String inputProviderPassword,
										  String inputConfirmPassword) {




		try {

			ProviderIAASDAO providerIAASDAO= ocwDataService_.getProviderIAASDAO();
			ProviderIAAS oldProvider = providerIAASDAO.findProviderById(inputProviderId);
			if (oldProvider == null) {
				flash.setError("This provider \""+oldProviderName+"\" doesn't exist.");
				flash.setSuccess("");
				return ProviderManagement_.index();
			}



			oldProvider.setProviderIAASName(inputProviderName);
			oldProvider.setProviderIAASVendor(inputProviderVendor);
			oldProvider.setProviderIAASLogin(inputProviderLogin);

			if (!inputProviderPassword.equals("")  && !inputProviderPassword.equals(inputConfirmPassword)) {
				flash.setError("Password and confirm password don't match.");
				flash.setSuccess("");
				return ProviderManagement_.index();
			}

			if (!inputProviderPassword.equals("") && inputProviderPassword.equals(inputConfirmPassword) ) {
				oldProvider.setProviderIAASPassword(inputProviderPassword);
			}


			providerIAASDAO.saveProviderIAAS(oldProvider);

			flash.setSuccess("Provider \""+inputProviderName+"\" modified.");
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		return ProviderManagement_.index();
	}

}
