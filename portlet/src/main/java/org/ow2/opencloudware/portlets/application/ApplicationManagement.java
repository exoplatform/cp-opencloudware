package org.ow2.opencloudware.portlets.application;

import juzu.*;
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.apache.commons.fileupload.FileItem;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.json.JSONObject;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.dao.ApplicationDAO;
import org.opencloudware.hibernate.dao.ApplicationInstanceDAO;
import org.opencloudware.hibernate.model.Application;
import org.opencloudware.hibernate.model.*;
import org.opencloudware.rest.OCWUtil;
import org.ow2.opencloudware.commons.ApplicationDesc;
import org.ow2.opencloudware.commons.vamp.DeploymentManager;
import org.ow2.opencloudware.commons.vamp.VampManager;
import org.ow2.opencloudware.portlets.common.Flash;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
    @Path("instances.gtmpl")
    Template instances;

    @Inject
    @Path("instancesList.gtmpl")
    Template instancesList;

    @Inject
    @Path("instancesPage.gtmpl")
    Template instancesPage;

    @Inject
    @Path("endPointFragment.gtmpl")
    Template endPointFragment;

    @Inject
    @Path("statusFragment.gtmpl")
    Template statusFragment;

    @Inject
    @Path("applicationInstanceInformation.gtmpl")
    Template applicationInstanceInformation;

    @Inject
    @Path("costEvaluation.gtmpl")
    Template costEvaluation;
    @Inject
    @Path("costResults.gtmpl")
    Template costResults;

	@Inject
	Flash flash;

	int currentPage;
	int nbPages;
	int nbResults;


	static int PAGE_SIZE = 10;


	OrganizationService organizationService_;
	OcwDataService ocwDataService_;
	String currentOrganizationId;
	String currentProjectId;
    String currentApplicationId;
    String currentApplicationInstanceId;


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
        if (currentApplicationId == null) {
            currentApplicationId="";
        }
        if (currentApplicationInstanceId == null) {
            currentApplicationInstanceId="";
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
										   String inputManagersNames, FileItem inputApplicationModele,
                                           FileItem inputScalabilityRules, FileItem inputAlternativeModele1) {


		try {

			ApplicationDAO applicationDAO= ocwDataService_.getApplicationDAO();


			Application application = applicationDAO.createApplicationInstance();
			application.setApplicationName(inputApplicationName);
			application.setDescription(inputApplicationDescription);

            application.setModele(inputApplicationModele.get());

            Set<byte[]> alternativesModeles = new HashSet<byte[]>();
            alternativesModeles.add(inputAlternativeModele1.get());
            application.setAlternativeModeles(alternativesModeles);

            application.setRules(inputScalabilityRules.get());

            String[] managers = inputManagersNames.split(",");

			Set<String> managersSet = new HashSet<String>();

			for (String manager : managers) {
				managersSet.add(manager);
			}
			application.setManagers(managersSet);

			application.setProject(ocwDataService_.getProjectDAO().findProjectById(currentProjectId));


            //actually, I have a problem to get multiple alternative files.
            //So I test with one but work as we have a list
            List<FileItem> alternativeModeles = new ArrayList<FileItem>();
            alternativeModeles.add(inputAlternativeModele1);
			applicationDAO.createApplication(application, inputApplicationModele, inputScalabilityRules, alternativeModeles);

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
										 String inputManagersNames, FileItem inputApplicationModele) {
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
            if (inputApplicationModele.getSize()!=0) {
                application.setModele(inputApplicationModele.get());
            }

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

            if (!result.equals("")) {
                result = result.substring(0, result.length() - 2);
            }
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("managers", result);
			managersFragment.render(parameters);

		} catch (Exception e) {

			e.printStackTrace();
			index();
		}
	}

    @View
    public void displayInstances() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("flash", flash);
        instances.render(parameters);
        flash.setError("");
        flash.setSuccess("");

    }

    @View
    public void displayInstances(String applicationId) {
        this.currentApplicationId=applicationId;
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("flash", flash);
        instances.render(parameters);
        flash.setError("");
        flash.setSuccess("");

    }

    @View
    public void displayApplicationInstanceInformation(String applicationInstanceId) {
        this.currentApplicationInstanceId=applicationInstanceId;
        try {
            //1) recuperation de la ressource multi-cloud-iaas
            JSONObject resource = OCWUtil.getResource("deployment");
            if (resource != null) {
                String resourceEndPoint = resource.getString("resourceEndpoint");
                ApplicationInstance applicationInstance = ocwDataService_.getApplicationInstanceDAO().findApplicationInstanceById(applicationInstanceId);
                VampManager vampManager = new VampManager(resourceEndPoint);
                Set<DeploymentManager> deploymentManagers = vampManager.getApplications(applicationInstanceId);
                if (deploymentManagers.size() == 1) {
                    DeploymentManager deploymentManager = deploymentManagers.iterator().next();
                    ApplicationDesc appDescr = deploymentManager.getAppDesc(false);
                    System.out.println(appDescr);
                    Map<String, Object> parameters = new HashMap<String, Object>();
                    parameters.put("appDescr", appDescr);
                    parameters.put("currentApplicationId", currentApplicationId);
                    applicationInstanceInformation.render(parameters);
                } else {
                    ApplicationManagement_.displayInstances(currentApplicationId);

                }

            } else {
                ApplicationManagement_.displayInstances(currentApplicationId);

            }
        }catch (Exception e) {
            e.printStackTrace();
            ApplicationManagement_.displayInstances(currentApplicationId);
        }
    }


    @Ajax
    @Resource
    @Route("/getInstancePageList/")
    public void getInstancePageList() {


        Map<String, Object> parameters = new HashMap<String, Object>();
        try {
            Application application = ocwDataService_.getApplicationDAO().findApplicationById(currentApplicationId);
            List<ApplicationInstance> instances= new ArrayList<ApplicationInstance>();
            instances.addAll(application.getApplicationsInstance());
            parameters.put("instances",instances);
            parameters.put("currentApplicationName",application.getApplicationName());
            parameters.put("isDeployable", application.getModele()!=null);
            Project project = application.getProject();
            //refresh from cache
            project=ocwDataService_.getProjectDAO().findProjectById(project.getId().toString());
            Organization organization = project.getOrganization();
            organization=ocwDataService_.getOrganizationDAO().findOrganizationById(organization.getId());
            Set<ProviderIAAS> providers = organization.getProviderIAASes();
            parameters.put("providers",providers);

            instancesList.render(parameters);
            flash.setError("");
            flash.setSuccess("");
        } catch (Exception e) {
            e.printStackTrace();
            ApplicationManagement_.index();
        }


    }



    @Ajax
    @Resource
    @Route("/instancePage/{pageToDisplay}")
    public void getInstancePage(String pageToDisplay) {
        List<ApplicationInstance> currentList = new ArrayList<ApplicationInstance>();

        Integer pageWanted = new Integer(pageToDisplay);



        try {
            PageList pageList = ocwDataService_.getApplicationInstanceDAO().getApplicationInstancePageListByApplication(PAGE_SIZE, currentApplicationId);
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
        instancesPage.render(parameters);
    }

    @Ajax
    @Resource
    @Route("/deploy/{provider}")
    public void deploy(String provider, String activateMonitoring) {
        System.out.println("Deploy function : provider="+provider+", activateMonitoring = "+activateMonitoring);

        try {
            ApplicationInstanceDAO applicationInstanceDAO = ocwDataService_.getApplicationInstanceDAO();
            ApplicationInstance applicationInstance = new ApplicationInstance();
            applicationInstance.setProviderIAASId(provider);
            Application application = ocwDataService_.getApplicationDAO().findApplicationById(currentApplicationId);
            applicationInstance.setApplication(application);
            if (!applicationInstanceDAO.createInstanceApplication(applicationInstance)) {
                flash.setError("Unable to deploy application");
            } else {

                flash.setSuccess("Deployment launched.");
            }
        } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                flash.setError("Unable to create Provider");
        }

        getInstancePage(""+currentPage);
    }

    @Ajax
    @Resource
    @Route("/getEndPointFragement")
    public void getEndPointFragment(String applicationInstanceId) {
        String creationDate = getCreationDate(applicationInstanceId);
        String formattedDate = "Not Deployed";
        if (creationDate!=null) {
            Timestamp tm = new Timestamp(new Long(creationDate).longValue());
            Date date = new Date(tm.getTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd '-' HH:mm:ss");
            formattedDate = dateFormat.format(date);
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("creationDate",formattedDate);
        endPointFragment.render(parameters);

    }

    @Ajax
    @Resource
    @Route("/getStatusFragement")
    public void getStatusFragment(String applicationInstanceId) {

        boolean deployed = isDeployed(applicationInstanceId);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("deployed",deployed);
        statusFragment.render(parameters);

    }



    @Ajax
    @Resource
    @Route("/isDeployed/{applicationId}")
    public boolean isDeployed(String applicationInstanceId) {
        try {

            //1) recuperation de la ressource multi-cloud-iaas
            JSONObject resource = OCWUtil.getResource("deployment");
            if (resource==null) return false;
            String resourceEndPoint=resource.getString("resourceEndpoint");
            ApplicationInstance applicationInstance = ocwDataService_.getApplicationInstanceDAO().findApplicationInstanceById(applicationInstanceId);
            VampManager vampManager = new VampManager(resourceEndPoint);
            Set<DeploymentManager> deploymentManagers = vampManager.getApplications(applicationInstanceId);
            if (deploymentManagers.size()==1) {
                DeploymentManager deploymentManager = deploymentManagers.iterator().next();
                boolean result=deploymentManager.isDeployed();
                return result;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Ajax
    @Resource
    @Route("/getCreationDate/{applicationId}")
    public String getCreationDate(String applicationInstanceId) {
        try {

            //1) recuperation de la ressource multi-cloud-iaas
            JSONObject resource = OCWUtil.getResource("deployment");
            if (resource==null) return null;
            String resourceEndPoint=resource.getString("resourceEndpoint");
            ApplicationInstance applicationInstance = ocwDataService_.getApplicationInstanceDAO().findApplicationInstanceById(applicationInstanceId);
            VampManager vampManager = new VampManager(resourceEndPoint);
            Set<DeploymentManager> deploymentManagers = vampManager.getApplications(applicationInstanceId);
            if (deploymentManagers.size()==1) {
                DeploymentManager deploymentManager = deploymentManagers.iterator().next();
                String result=deploymentManager.getCreationDate();
                return result;
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    @Action
    @Route("/deleteApplicationInstance/{inputApplicationInstanceId}")
    public Response.View deleteApplicationInstance(String inputApplicationInstanceId) {
        try {

            ApplicationInstanceDAO applicationInstanceDAO = ocwDataService_.getApplicationInstanceDAO();
            ApplicationInstance applicationInstance = applicationInstanceDAO.findApplicationInstanceById(inputApplicationInstanceId);
            if (applicationInstance == null) {
                flash.setError("This instance doesn't exist.");
                flash.setSuccess("");
                return ApplicationManagement_.index();
            }

            if (applicationInstanceDAO.removeApplicationInstance(applicationInstance)) {
                flash.setSuccess("Application Instance undeployed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // a voir pour réafficher la liste des instance plutot
        return ApplicationManagement_.displayInstances(currentApplicationId);

    }

    @View
    public void displayCostEvaluation(String cost) {
        try {

            Map<String, Object> parameters = new HashMap<String, Object>();
            Application application = ocwDataService_.getApplicationDAO().findApplicationById(currentApplicationId);
            List<ApplicationInstance> instances= new ArrayList<ApplicationInstance>();
            if (application!=null) {
                instances.addAll(application.getApplicationsInstance());
                parameters.put("currentApplicationName", application.getApplicationName());
                parameters.put("currentApplicationId", application.getId().toString());

                if (!parameters.containsKey("cost")) {
                    parameters.put("cost", null);
                }
                if (cost!=null) {
                    parameters.put("cost",cost);
                    parameters.put("flash",flash);
                }  else {
                    flash.setError("");
                    flash.setSuccess("");
                    parameters.put("flash",flash);
                }

                Project project = application.getProject();
                Organization organization = project.getOrganization();
                Set<ProviderIAAS> providers = organization.getProviderIAASes();
                parameters.put("providers", providers);

                costEvaluation.render(parameters);
                flash.setError("");
                flash.setSuccess("");
            } else {
                ApplicationManagement_.index();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ApplicationManagement_.index();
        }

    }

    @Action
    @Route("/evaluateCost")
    public Response.View evaluateCost(String inputApplicationId, String inputDuration, String deploymentTarget, FileItem scalabilityConstraints) {


        
        String cost="";


        //1) recuperation de la ressource boucle decision
        JSONObject resource = OCWUtil.getResource("orchestrator");
        if (resource == null) {
            flash.setError("Error when evaluating cost");
            flash.setSuccess("");
        } else {
            try {
                String resourceEndPoint = resource.getString("resourceEndpoint");
                String endPointURI = "/business/cost";

                ProviderIAAS provider = ocwDataService_.getProviderIAASDAO().findProviderById(deploymentTarget);

                String constraints = new String(scalabilityConstraints.get());
                String postData = "duration=" + inputDuration + "&iaasId=" + provider.getProviderIAASVendor()+"&scalabilityConstraints="+constraints+"&placementConstraints=&sla=";

                String username = resource.getString("resourceLogin").equals("null") ? null : resource.getString("resourceLogin");
                String password = resource.getString("resourcePassword").equals("null") ? null : resource.getString("resourcePassword");
                Map<String, String> headers = new HashMap<String, String>();

                System.out.println("send  cost request");
                String responseData = OCWUtil.doPost(resourceEndPoint + endPointURI, username, password, postData,headers,"text/plain; charset=utf8");
                System.out.println("receive cost response");
                if (responseData == null) {
                    flash.setError("Error when evaluating cost");
                    flash.setSuccess("");
                } else {
                    JSONObject costJson = new JSONObject(responseData);
                    Double min = costJson.getJSONObject("ok").getJSONObject("cost").getDouble("min");
                    Double max = costJson.getJSONObject("ok").getJSONObject("cost").getDouble("max");
                    Double avg = costJson.getJSONObject("ok").getJSONObject("cost").getDouble("avg");


                    DecimalFormat df = new DecimalFormat("###.##");
                    String minStr = df.format(min);
                    String maxStr = df.format(max);
                    String avgStr = df.format(avg);
                    cost = "<h4 class=\"opencloudwareTitle\">Cost Evaluation Results</h4>\nDeploying this application on IAAS "+provider.getProviderIAASName()+" during "+inputDuration+" days will cost between <strong>"+minStr+"$</strong> and <strong>"+maxStr+"$</strong> with an average of <strong>"+avgStr+"$</strong>";

                }

            } catch (Exception e) {
                e.printStackTrace();
                flash.setError("Error when evaluating cost");
                flash.setSuccess("");
            }
        }
        return ApplicationManagement_.displayCostEvaluation(cost);
    }

}
