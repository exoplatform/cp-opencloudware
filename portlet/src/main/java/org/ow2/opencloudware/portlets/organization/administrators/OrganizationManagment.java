package org.ow2.opencloudware.portlets.organization.administrators;

/**
 * Created with IntelliJ IDEA.
 * User: Romain Denarie
 * Date: 05/08/13
 * Time: 15:02
 * To change this template use File | Settings | File Templates.
 */

import juzu.*;
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.dao.OrganizationDAO;
import org.opencloudware.hibernate.model.Organization;
import org.ow2.opencloudware.portlets.common.Flash;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SessionScoped
public class OrganizationManagment {

	@Inject
	@Path("list.gtmpl")
	Template list;

	@Inject
	@Path("page.gtmpl")
	Template page;


	@Inject
	@Path("addOrganization.gtmpl")
	Template addOrganization;

	@Inject
	@Path("editOrganization.gtmpl")
	Template editOrganization;

	@Inject
	Flash flash;

	int currentPage;
	int nbPages;
    int nbResults;


	static int PAGE_SIZE = 10;


	OrganizationService organizationService_;
	OcwDataService ocwDataService_;

	@Inject
	public void OrganizationManagment(OrganizationService organizationService, OcwDataService ocwDataService) {
		organizationService_=organizationService;
		ocwDataService_=ocwDataService;
		currentPage = 1;
	}



	@View
	public void index() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("flash", flash);
		list.render(parameters);
		flash.setError("");
		flash.setSuccess("");

	}

	@View
	public void displayAddForm() {
		addOrganization.render();
	}

	@View
	public void displayEditForm(String inputOrganizationId) {
		Organization organization = null;
		try {


			OrganizationDAO organizationDAO = ocwDataService_.getOrganizationDAO();
			organization = organizationDAO.findOrganizationById(inputOrganizationId);

		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		if (organization==null) {
			flash.setError("Organization with id \""+inputOrganizationId+"\" not founded.");
			index();
		} else {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("organization", organization);
			editOrganization.render(parameters);
		}
	}

	@Ajax
	@Resource
	@Route("/page/{pageToDisplay}")
	public void getPage(String pageToDisplay) {
		List<Organization> currentList = new ArrayList<Organization>();

		Integer pageWanted = new Integer(pageToDisplay);



		try {
			PageList pageList = ocwDataService_.getOrganizationDAO().getOrganizationPageList(PAGE_SIZE);
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


	@Ajax
	@Resource
	@Route("/isValidOrganizationName")
	public Response.Content isValidOrganizationName(String inputOrganizationName) {
		GroupHandler groupHandler=organizationService_.getGroupHandler();

		String sanitizedOrganizationName = "/opencloudware/"+Utils.cleanString(inputOrganizationName);
		try {

			if (groupHandler.findGroupById(sanitizedOrganizationName) == null) {
				return Response.ok("true").withMimeType("text/html; charset=UTF-8").withHeader("Cache-Control", "no-cache");

			}
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return Response.ok("false").withMimeType("text/html; charset=UTF-8").withHeader("Cache-Control", "no-cache");

	}


	@Action
	@Route("/createOrganization")
	public Response.View createOrganization(String inputOrganizationName,
									String inputOrganizationAddress) {


		GroupHandler groupHandler = organizationService_.getGroupHandler();

		String sanitizedOrganizationName = "/opencloudware/"+Utils.cleanString(inputOrganizationName);
		try {

			OrganizationDAO organizationDAO = ocwDataService_.getOrganizationDAO();
			Organization organization = organizationDAO.findOrganizationByName(inputOrganizationName);
			if (organization != null) {
				flash.setError("This organization already exist.");
				flash.setSuccess("");
				return OrganizationManagment_.index();
			}

			Group newGroup = groupHandler.findGroupById(sanitizedOrganizationName);
			if (newGroup != null) {
				flash.setError("This organization already exist.");
				flash.setSuccess("");
				return OrganizationManagment_.index();
			}

			newGroup = groupHandler.createGroupInstance();
			newGroup.setGroupName(Utils.cleanString(inputOrganizationName));
			newGroup.setLabel(inputOrganizationName);

			Group opencloudareGroup = groupHandler.findGroupById("/opencloudware");
			groupHandler.addChild(opencloudareGroup,newGroup,true);


			organization = organizationDAO.createOrganizationInstance(inputOrganizationName);
			organization.setCreditCardNumber("");
			organization.setGroupId(newGroup.getId());
			organization.setAddress(inputOrganizationAddress);
			organizationDAO.createOrganization(organization);


			//MembershipType membershipTypeManager = organizationService_.getMembershipTypeHandler().findMembershipType("manager");
			//organizationService_.getMembershipHandler().linkMembership(user, newGroup, membershipTypeManager, true);

			//MembershipType membershipTypeMember = organizationService_.getMembershipTypeHandler().findMembershipType("member");
			//organizationService_.getMembershipHandler().linkMembership(user, newGroup, membershipTypeMember, true);



			flash.setSuccess("Organization \""+inputOrganizationName+"\" added.");
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}


		return OrganizationManagment_.index();

	}


	@Action
	@Route("/deleteOrganization/{inputOrganizationId}")
	public Response deleteOrganization(String inputOrganizationId) {
		GroupHandler groupHandler = organizationService_.getGroupHandler();

		try {

			OrganizationDAO organizationDAO = ocwDataService_.getOrganizationDAO();
			Organization organization = organizationDAO.findOrganizationById(inputOrganizationId);
			if (organization == null) {
				flash.setError("This organization doesn't exist.");
				flash.setSuccess("");
				return OrganizationManagment_.index();
			}

			Group groupToDelete = groupHandler.findGroupById(organization.getGroupId());
			groupHandler.removeGroup(groupToDelete, true);

			organizationDAO.removeOrganization(inputOrganizationId);


			flash.setSuccess("Organization \""+organization.getOrganizationName()+"\" removed.");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return OrganizationManagment_.index();

	}


	@Action
	@Route("/editOrganization")
	public Response.View editOrganization(String inputOrganizationId,
										  String inputOrganizationName,
											String inputOrganizationAddress) {



		GroupHandler groupHandler = organizationService_.getGroupHandler();

		try {

			OrganizationDAO organizationDAO = ocwDataService_.getOrganizationDAO();
			Organization organization = organizationDAO.findOrganizationById(inputOrganizationId);
			if (organization == null) {
				flash.setError("This organization with id \""+inputOrganizationId+"\" doesn't exist.");
				flash.setSuccess("");
				return OrganizationManagment_.index();
			}



			organization.setOrganizationName(inputOrganizationName);
			organization.setAddress(inputOrganizationAddress);


			Group group = groupHandler.findGroupById(organization.getGroupId());
			//on ne peut pas changer le nom d'un group => on change que le label
			group.setLabel(inputOrganizationName);
			groupHandler.saveGroup(group,true);

			organizationDAO.saveOrganization(organization);

			flash.setSuccess("Organization \""+inputOrganizationName+"\" modified.");
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		return OrganizationManagment_.index();
	}


	public int getCurrentPage() {

		return currentPage;
	}
}