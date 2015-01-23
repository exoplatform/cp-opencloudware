package org.ow2.opencloudware.portlets.project;


import juzu.*;
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.json.JSONObject;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.dao.ApplicationDAO;
import org.opencloudware.hibernate.dao.ApplicationInstanceDAO;
import org.opencloudware.hibernate.dao.ProjectDAO;
import org.opencloudware.hibernate.model.*;
import org.opencloudware.hibernate.model.Application;
import org.opencloudware.rest.OCWUtil;
import org.ow2.opencloudware.commons.ApplicationDesc;
import org.ow2.opencloudware.commons.vamp.DeploymentManager;
import org.ow2.opencloudware.commons.vamp.VampManager;
import org.ow2.opencloudware.portlets.application.ApplicationManagement_;
import org.ow2.opencloudware.portlets.common.Flash;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
    @Path("clifUrlFragment.gtmpl")
    Template clifUrlFragment;

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


            Set<ProviderIAAS> providers = organizationObject.getProviderIAASes();
            parameters.put("providers", providers);

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

                boolean  isDeployedClif = project.isDeployedClif();

                Map<String, Object> parameters = new HashMap<String, Object>();

                Set<ProviderIAAS> providers = organizationObject.getProviderIAASes();
                parameters.put("providers", providers);
				parameters.put("usersNames", usersNames);
				parameters.put("managersNames", managersNames);
                parameters.put("isDeployedClif",isDeployedClif);
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
											String inputManagersNames,
                                            String inputProjectDeployClif,
                                            String deploymentTarget) {


		try {

			ProjectDAO projectDAO= ocwDataService_.getProjectDAO();


            boolean isDeployedClif = inputProjectDeployClif!= null && inputProjectDeployClif.equals("on");

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

            if (isDeployedClif) {
                deployClif(project, deploymentTarget);
            }

			flash.setSuccess("Project \""+inputProjectName+"\" added.");
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}


		return ProjectManagement_.index();

	}

    private void deployClif(Project project, String deploymentTarget) {

        try {
            ApplicationDAO applicationDAO = ocwDataService_.getApplicationDAO();
            org.opencloudware.hibernate.model.Application application = applicationDAO.createApplicationInstance();
            application.setApplicationName("Clif for " + project.getProjectName());
            application.setDescription("The benchmarking application");


            InputStream clifModele = ProjectManagement.class.getResourceAsStream("/ovfData/vamp-clifaas.ovf");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = clifModele.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }



            application.setModele(buffer.toByteArray());


            Set<String> managers = project.getManagers();
            Set<String> newManagers = new HashSet<String>();
            newManagers.addAll(managers);
            application.setManagers(newManagers);
            application.setProject(ocwDataService_.getProjectDAO().findProjectById(project.getId().toString()));
            applicationDAO.createApplication(application);


            project.setProjectClifApplicationId(application.getId().toString());
            project.setDeployedClif(true);
            project.setProjectClifProviderId(deploymentTarget);
            ocwDataService_.getProjectDAO().saveProject(project);

            try {
                ApplicationInstanceDAO applicationInstanceDAO = ocwDataService_.getApplicationInstanceDAO();
                ApplicationInstance applicationInstance = new ApplicationInstance();

                applicationInstance.setProviderIAASId(deploymentTarget);
                applicationInstance.setApplication(application);
                if (!applicationInstanceDAO.createInstanceApplication(applicationInstance)) {
                    flash.setError("Unable to deploy Clif application");
                } else {

                    flash.setSuccess("Clif Deployment started.");
                }
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                flash.setError("Unable to create Provider");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Action
	@Route("/editProject")
	public Response.View editProject(String inputProjectId, String inputProjectName,
									 String inputProjectDescription,String inputUsersNames,
									 String inputManagersNames,
                                     String inputProjectDeployClif,
                                     String deploymentTarget) {




		try {

			ProjectDAO projectDAO= ocwDataService_.getProjectDAO();
			Project project = projectDAO.findProjectById(inputProjectId);
			if (project == null) {
				flash.setError("This project with id \""+inputProjectId+"\" doesn't exist.");
				flash.setSuccess("");
				return ProjectManagement_.index();
			}


            boolean isDeployedClif = inputProjectDeployClif!= null && inputProjectDeployClif.equals("on");

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

            if (project.isDeployedClif() && !isDeployedClif) {
                //need to undeploy
                undeployClif(project);
            } else if (!project.isDeployedClif() && isDeployedClif) {
                //need to deploy
                deployClif(project, deploymentTarget);
            }


			flash.setSuccess("Project \""+inputProjectName+"\" modified.");
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		return ProjectManagement_.index();
	}

    private void undeployClif(Project project) {
        try {

            try {

                ApplicationDAO applicationDAO = ocwDataService_.getApplicationDAO();
                ApplicationInstanceDAO applicationInstanceDAO = ocwDataService_.getApplicationInstanceDAO();

                Application application = applicationDAO.findApplicationById(project.getProjectClifApplicationId());
                for (ApplicationInstance applicationInstance : application.getApplicationsInstance()) {
                    applicationInstanceDAO.removeApplicationInstance(applicationInstance);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            String applicationCliffId=project.getProjectClifApplicationId();
            project.setDeployedClif(false);
            project.setProjectClifApplicationId(null);
            project.setProjectClifProviderId(null);
            ocwDataService_.getProjectDAO().saveProject(project);

            ApplicationDAO applicationDAO = ocwDataService_.getApplicationDAO();

            applicationDAO.removeApplication(applicationCliffId);



        } catch (Exception e) {
            e.printStackTrace();
        }
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


    @Ajax
    @Resource
    @Route("/getClifUrlFragment")
    public void getClifUrlFragment(String projectId) {
        String result = "";

        try {




            Project project=ocwDataService_.getProjectDAO().findProjectById(projectId);
            Application clifApplication = ocwDataService_.getApplicationDAO().findApplicationById(project.getProjectClifApplicationId());
            //TODO add some check for next()
            ApplicationInstance clifInstance = clifApplication.getApplicationsInstance().iterator().next();

            try {
                //1) recuperation de la ressource multi-cloud-iaas
                JSONObject resource = OCWUtil.getResource("deployment");
                if (resource != null) {
                    String resourceEndPoint = resource.getString("resourceEndpoint");
                    VampManager vampManager = new VampManager(resourceEndPoint);
                    Set<DeploymentManager> deploymentManagers = vampManager.getApplications(clifInstance.getId());
                    if (deploymentManagers.size() == 1) {
                        //TODO add some check for next()
                        DeploymentManager deploymentManager = deploymentManagers.iterator().next();
                        ApplicationDesc appDescr = deploymentManager.getAppDesc(false);
                        //TODO add some check for next()

                        String ip = appDescr.getAllVMinfo().iterator().next().getAddress();
                        if (ip != null) {
                            result = "<a href='http://" + ip +":8080' target='_blank'>http://"+ip+":8080</a>";
                        } else {
                            result = "Not yet Deployed";
                        }
                    } else {
                        result = "Not yet deployed";
                    }

                } else {
                    result = "Not yet deployed";
                }
            } catch (Exception e) {
                e.printStackTrace();
                result = "Not yet deployed";
            }


        } catch (Exception e) {

            e.printStackTrace();
            result = "Not yet deployed";
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("url", result);
        clifUrlFragment.render(parameters);
    }




}
