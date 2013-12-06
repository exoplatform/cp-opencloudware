package org.ow2.opencloudware.portlets.application;

import juzu.*;
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.dao.ApplicationDAO;
import org.opencloudware.hibernate.model.Application;
import org.opencloudware.hibernate.model.Organization;
import org.opencloudware.hibernate.model.Project;
import org.ow2.opencloudware.portlets.common.Flash;

import javax.inject.Inject;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Romain Denarie
 * Date: 12/11/13
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */

@SessionScoped
public class ApplicationManagement {
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
	@Path("addApplication.gtmpl")
	Template addApplication;

	@Inject
	@Path("editApplication.gtmpl")
	Template editApplication;

	@Inject
	@Path("managersFragment.gtmpl")
	Template managersFragment;



	@Inject
	Flash flash;

	int currentPage;
	int nbPages;
	int nbResults;


	static int PAGE_SIZE = 3;


	OrganizationService organizationService_;
	OcwDataService ocwDataService_;
	String currentOrganizationId;
	String currentProjectId;


	@Inject
	public ApplicationManagement(OrganizationService organizationService, OcwDataService ocwDataService) {
		organizationService_=organizationService;
		ocwDataService_=ocwDataService;
		currentPage = 1;

		if (currentOrganizationId == null) {
			currentOrganizationId = "";
		}

		if (currentProjectId == null) {
			currentProjectId="";
		}
	}

	@View
	public void index() {
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
				ApplicationManagement_.index();

			} else {
				String organizationName = group.getLabel();

				Organization organizationObject = ocwDataService_.getOrganizationDAO().findOrganizationByName(organizationName);
				changeOrganization(organizationObject.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
			ApplicationManagement_.index();
		}


	}


	@Ajax
	@Resource
	@Route("/changeOrganization/{organizationId}")
	public void changeOrganization(String organizationId) {


		//TODO : manage case when no projects

		currentOrganizationId = organizationId;

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("currentOrganizationId", currentOrganizationId);
		try {
			Organization organization = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);
			parameters.put("currentOrganizationName", organization.getOrganizationName());
			List<Project> projects= new ArrayList<Project>();
			projects.addAll(organization.getProjects());
			parameters.put("projects",projects);
			if (currentProjectId == null || currentProjectId.equals("")) {
				currentProjectId = projects.get((0)).getId().toString();
			}
			parameters.put("currentProjectId",currentProjectId);

		} catch (Exception e) {
			parameters.put("currentOrganizationName", "");
		}
		list.render(parameters);
		flash.setError("");
		flash.setSuccess("");

	}

	@Ajax
	@Resource
	@Route("/page/{pageToDisplay}")
	public void getPage(String pageToDisplay) {
		List<Application> currentList = new ArrayList<Application>();

		Integer pageWanted = new Integer(pageToDisplay);



		try {
			PageList pageList = ocwDataService_.getApplicationDAO().getApplicationPageListByProject(PAGE_SIZE, currentProjectId);
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
	@Route("/changeProject/{projectId}")
	public void changeProject(String projectId) {
		currentProjectId = projectId;
		changeOrganization(currentOrganizationId);

	}


	@View
	public void displayAddForm() {
		try {
			Organization organizationObject = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);
			ListAccess<User> users = organizationService_.getUserHandler().findUsersByGroupId(organizationObject.getGroupId());
			Map<String, String> usersNames = new HashMap<String,String>();
			Map<String, String> managersNames = new HashMap<String,String>();
			//first string = uid
			//second string = full name

			//here, we load all users of the org
			//if we have big lists it can be a perf hole.
			// in this case : implement "more" button and load only firsts users (100 for example)
			// or do not propose role management in organization, only in user tab.
			User[] loadedUsers = users.load(0, users.getSize());
			for (User user : loadedUsers) {
				String fullName = "";
				if (user.getLastName()== null  || user.getFirstName() == null) {
					fullName = user.getUserName();
				} else {
					fullName = user.getFirstName() + " " + user.getLastName();
				}

				usersNames.put(user.getUserName(), fullName);


			}
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("usersNames", usersNames);
			parameters.put("managersNames", managersNames);
			addApplication.render(parameters);
		} catch (Exception e) {
			e.printStackTrace();
			index();
		}

	}

	@View
	public void displayEditForm(String inputApplicationId) {
		Application application = null;
		try {


			ApplicationDAO applicationDAO = ocwDataService_.getApplicationDAO();
			application = applicationDAO.findApplicationById(inputApplicationId);


			if (application==null) {
				flash.setError("Application with id \""+inputApplicationId+"\" not founded.");
				ApplicationManagement_.index();
			} else {
				Organization organizationObject = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);
				ListAccess<User> users = organizationService_.getUserHandler().findUsersByGroupId(organizationObject.getGroupId());
				Map<String, String> usersNames = new HashMap<String,String>();
				Map<String, String> managersNames = new HashMap<String,String>();
				//first string = uid
				//second string = full name

				//here, we load all users of the org
				//if we have big lists it can be a perf hole.
				// in this case : implement "more" button and load only firsts users (100 for example)
				// or do not propose role management in organization, only in user tab.
				User[] loadedUsers = users.load(0, users.getSize());
				for (User user : loadedUsers) {
					String fullName = "";
					if (user.getLastName()== null  || user.getFirstName() == null) {
						fullName = user.getUserName();
					} else {
						fullName = user.getFirstName() + " " + user.getLastName();
					}

					if (application.getManagers().contains(user.getUserName())) {
						managersNames.put(user.getUserName(),fullName);
					} else {
						usersNames.put(user.getUserName(), fullName);
					}


				}
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("usersNames", usersNames);
				parameters.put("managersNames", managersNames);
				parameters.put("application", application);
				editApplication.render(parameters);
			}
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			index();
		}
	}


	@Action
	@Route("/createApplication")
	public Response.View createApplication(String inputApplicationName,
										   String inputApplicationDescription,
										   String inputUsersNames,
										   String inputManagersNames) {


		try {

			ApplicationDAO applicationDAO= ocwDataService_.getApplicationDAO();


			Application application = applicationDAO.createApplicationInstance();
			application.setApplicationName(inputApplicationName);
			application.setDescription(inputApplicationDescription);

			String[] managers = inputManagersNames.split(",");

			Set<String> managersSet = new HashSet<String>();

			for (String manager : managers) {
				managersSet.add(manager);
			}
			application.setManagers(managersSet);

			application.setProject(ocwDataService_.getProjectDAO().findProjectById(currentProjectId));
			applicationDAO.createApplication(application);

			flash.setSuccess("Application \""+inputApplicationName+"\" added.");
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}


		return ApplicationManagement_.index();

	}


	@Action
	@Route("/editApplication")
	public Response.View editApplication(String inputApplicationId, String inputApplicationName,
										 String inputApplicationDescription,String inputUsersNames,
										 String inputManagersNames) {




		try {

			ApplicationDAO applicationDAO= ocwDataService_.getApplicationDAO();
			Application application = applicationDAO.findApplicationById(inputApplicationId);
			if (application == null) {
				flash.setError("This application with id \""+inputApplicationId+"\" doesn't exist.");
				flash.setSuccess("");
				return ApplicationManagement_.index();
			}



			application.setApplicationName(inputApplicationName);
			application.setDescription(inputApplicationDescription);

			String[] managers = inputManagersNames.split(",");

			Set<String> managersSet = application.getManagers();
			managersSet.clear();
			for (String manager : managers) {
				managersSet.add(manager);
			}
			application.setManagers(managersSet);

			applicationDAO.saveApplication(application);

			flash.setSuccess("Application \""+inputApplicationName+"\" modified.");
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		return ApplicationManagement_.index();
	}



	@Action
	@Route("/deleteApplication/{inputApplicationId}")
	public Response deleteApplication(String inputApplicationId) {

		try {

			ApplicationDAO applicationDAO = ocwDataService_.getApplicationDAO();
			Application application = applicationDAO.findApplicationById(inputApplicationId);
			if (application == null) {
				flash.setError("This application doesn't exist.");
				flash.setSuccess("");
				return ApplicationManagement_.index();
			}

			applicationDAO.removeApplication(inputApplicationId);


			flash.setSuccess("Application \""+application.getApplicationName()+"\" removed.");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return ApplicationManagement_.index();

	}

	@Ajax
	@Resource
	@Route("/getManagerFragement")
	public void getManagerFragement(String applicationId) {
		try {
			String result = "";
			Set<String> managers = ocwDataService_.getApplicationDAO().findApplicationById(applicationId).getManagers();
			for (String m : managers) {
				User user=organizationService_.getUserHandler().findUserByName(m);
				if (user != null) {
					String fullName = "";
					if (user.getLastName()== null  || user.getFirstName() == null) {
						fullName = user.getUserName();
					} else {
						fullName = user.getFirstName() + " " + user.getLastName();
					}
					result+=fullName+", ";
				}
			}
			result = result.substring(0,result.length()-2);
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("managers", result);
			managersFragment.render(parameters);

		} catch (Exception e) {

			e.printStackTrace();
			index();
		}
	}

}
