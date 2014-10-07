package org.ow2.opencloudware.portlets.project;


import juzu.*;
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.dao.ProjectDAO;
import org.opencloudware.hibernate.model.Organization;
import org.opencloudware.hibernate.model.Project;
import org.ow2.opencloudware.portlets.common.Flash;

import javax.inject.Inject;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Romain Denarie
 * Date: 28/10/13
 * Time: 11:39
 * To change this template use File | Settings | File Templates.
 */
@SessionScoped
public class ProjectManagement {

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
	@Path("addProject.gtmpl")
	Template addProject;

	@Inject
	@Path("editProject.gtmpl")
	Template editProject;

	@Inject
	@Path("managersFragment.gtmpl")
	Template managersFragment;


	@Inject
	Flash flash;

	int currentPage;
	int nbPages;
	int nbResults;


	static int PAGE_SIZE = 10;


	OrganizationService organizationService_;
	OcwDataService ocwDataService_;
	String currentOrganizationId;



	@Inject
	public ProjectManagement(OrganizationService organizationService, OcwDataService ocwDataService) {
		organizationService_=organizationService;
		ocwDataService_=ocwDataService;
		currentPage = 1;

		if (currentOrganizationId == null) {
			currentOrganizationId = "";
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
				index();

			} else {
				String organizationName = group.getLabel();

				Organization organizationObject = ocwDataService_.getOrganizationDAO().findOrganizationByName(organizationName);
				changeOrganization(organizationObject.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
			index();
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
			addProject.render(parameters);
		} catch ( Exception e) {
			e.printStackTrace();
			index();

		}


	}


	@Ajax
	@Resource
	@Route("/page/{pageToDisplay}")
	public void getPage(String pageToDisplay) {
		List<Project> currentList = new ArrayList<Project>();

		Integer pageWanted = new Integer(pageToDisplay);



		try {
			PageList pageList = ocwDataService_.getProjectDAO().getProjectPageListByOrganization(PAGE_SIZE, currentOrganizationId);
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
	@Route("/deleteProject/{inputProjectId}")
	public Response deleteProject(String inputProjectId) {

		try {

			ProjectDAO projectDAO = ocwDataService_.getProjectDAO();
			Project project = projectDAO.findProjectById(inputProjectId);
			if (project == null) {
				flash.setError("This project doesn't exist.");
				flash.setSuccess("");
				return ProjectManagement_.index();
			}

			projectDAO.removeProject(inputProjectId);


			flash.setSuccess("Project \""+project.getProjectName()+"\" removed.");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return ProjectManagement_.index();

	}

	@View
	public void displayEditForm(String inputProjectId) {
		Project project = null;
		try {


			ProjectDAO projectDAO = ocwDataService_.getProjectDAO();
			project = projectDAO.findProjectById(inputProjectId);


			if (project==null) {
				flash.setError("Project with id \""+inputProjectId+"\" not founded.");
				index();
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

					if (project.getManagers().contains(user.getUserName())) {
						managersNames.put(user.getUserName(),fullName);
					} else {
						usersNames.put(user.getUserName(), fullName);
					}


				}
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("usersNames", usersNames);
				parameters.put("managersNames", managersNames);

				parameters.put("project", project);
				editProject.render(parameters);
			}

		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			index();
		}
	}

	@Action
	@Route("/createProject")
	public Response.View createProject(String inputProjectName,
											String inputProjectDescription,
											String inputUsersNames,
											String inputManagersNames) {


		try {

			ProjectDAO projectDAO= ocwDataService_.getProjectDAO();


			Project project = projectDAO.createProjectInstance();
			project.setProjectName(inputProjectName);
			project.setDescription(inputProjectDescription);

			String[] managers = inputManagersNames.split(",");

			Set<String> managersSet = new HashSet<String>();

			for (String manager : managers) {
				managersSet.add(manager);
			}
			project.setManagers(managersSet);

			Organization organization = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);
			project.setOrganization(organization);
			projectDAO.createProject(project);

			flash.setSuccess("Project \""+inputProjectName+"\" added.");
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}


		return ProjectManagement_.index();

	}


	@Action
	@Route("/editProject")
	public Response.View editProject(String inputProjectId, String inputProjectName,
									 String inputProjectDescription,String inputUsersNames,
									 String inputManagersNames) {




		try {

			ProjectDAO projectDAO= ocwDataService_.getProjectDAO();
			Project project = projectDAO.findProjectById(inputProjectId);
			if (project == null) {
				flash.setError("This project with id \""+inputProjectId+"\" doesn't exist.");
				flash.setSuccess("");
				return ProjectManagement_.index();
			}



			project.setProjectName(inputProjectName);
			project.setDescription(inputProjectDescription);


			String[] managers = inputManagersNames.split(",");

			Set<String> managersSet = project.getManagers();
			managersSet.clear();
			for (String manager : managers) {
				managersSet.add(manager);
			}
			project.setManagers(managersSet);


			projectDAO.saveProject(project);

			flash.setSuccess("Project \""+inputProjectName+"\" modified.");
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		return ProjectManagement_.index();
	}


	@Ajax
	@Resource
	@Route("/getManagerFragement")
	public void getManagerFragement(String projectId) {
		try {
			    String result = "";
			Set<String> managers = ocwDataService_.getProjectDAO().findProjectById(projectId).getManagers();
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
