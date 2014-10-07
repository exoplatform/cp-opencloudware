package org.ow2.opencloudware.portlets.users;

import juzu.*;
import juzu.plugin.ajax.Ajax;
import juzu.request.RenderContext;
import juzu.template.Template;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.*;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.model.Organization;
import org.ow2.opencloudware.portlets.common.Flash;

import javax.inject.Inject;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Romain Denarie
 * Date: 29/08/13
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
@SessionScoped
public class UsersManagement {

	private OrganizationService organizationService_;
	private OcwDataService ocwDataService_;
	String currentOrganizationId;

	@Inject
	Flash flash;

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
	@Path("editUser.gtmpl")
	Template editUser;

	@Inject
	@Path("editRoles.gtmpl")
	Template editRoles;


	static int PAGE_SIZE = 10;
	private int nbResults;
	private int nbPages;
	private int currentPage;

	@Inject
	public void UsersManagement(OrganizationService organizationService, OcwDataService ocwDataService) {
		organizationService_=organizationService;
		ocwDataService_=ocwDataService;
		currentOrganizationId="";
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
	@Route("/getUsersFragmentByGroupId/{groupId}")
	public void getUsersFragmentByGroupId(String groupId) {
		try {
			Group group = organizationService_.getGroupHandler().findGroupById(groupId);
			if (group == null) {
				UsersManagement_.index();

			} else {
				String organizationName = group.getLabel();

				Organization organizationObject = ocwDataService_.getOrganizationDAO().findOrganizationByName(organizationName);
				getUsersFragment(organizationObject.getId());
			}
		} catch	(Exception e) {
			e.printStackTrace();
			UsersManagement_.index();
		}


	}

	@Ajax
	@Resource
	@Route("/getUsersFragment/{organizationId}")
	public void getUsersFragment(String organizationId) {
		try {
			currentOrganizationId=organizationId;

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("currentOrganization",currentOrganizationId);
			try {
				parameters.put("currentOrganizationName", ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId).getOrganizationName());
			} catch (Exception e) {
				parameters.put("currentOrganizationName", "");
			}
			list.render(parameters);
		} catch	(Exception e) {
			e.printStackTrace();
			UsersManagement_.index();
		}


	}


	@Ajax
	@Resource
	@Route("/page/{pageToDisplay}")
	public void getPage(String pageToDisplay) {
		List<User> currentList = new ArrayList<User>();
		Integer pageWanted = new Integer(pageToDisplay);

		try {
			String organizationGroup = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId).getGroupId();
			ListAccess<User> usersList = organizationService_.getUserHandler().findUsersByGroupId(organizationGroup);


			nbResults=usersList.getSize();

			nbPages = nbResults / PAGE_SIZE ;
			if (nbResults % PAGE_SIZE != 0) {
				nbPages++;
			}
			currentPage=pageWanted;
			if (pageWanted>nbPages) currentPage=nbPages;
			if (pageWanted<1) currentPage=1;
			if (nbResults<currentPage*PAGE_SIZE)
				currentList= Arrays.asList(usersList.load((currentPage-1)*PAGE_SIZE,nbResults-((currentPage-1)*PAGE_SIZE)));
			else
				currentList= Arrays.asList(usersList.load((currentPage-1)*PAGE_SIZE,PAGE_SIZE));


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
	@Route("/getRolesOfUserInCurrentOrganization")
	public Response.Content getRolesOfUserInCurrentOrganization(String userName) {

		String result = "";

		try {
			Organization org = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);
			if (org.getManagers().contains(userName)) {
				result = "Manager";
			} else {
				result = "Member";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.ok(result).withMimeType("text/html; charset=UTF-8").withHeader("Cache-Control", "no-cache");

	}



	@Action
	@Route("/removeUser/{userName}")
	public Response removeUser(String userName) {

		try {

			Collection<Membership> membershipsToRemove = organizationService_.getMembershipHandler()
					.findMembershipsByUserAndGroup(userName,
							ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId).getGroupId()
					);
			for (Membership m : membershipsToRemove) {
				organizationService_.getMembershipHandler().removeMembership(m.getId(),true);
			}

			flash.setSuccess("User \""+userName+"\" removed from your organization.");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return UsersManagement_.index();

	}

	@Ajax
	@Resource
	@Route("/isValidUserName")
	public Response.Content isValidUserName(String inputUserName) {
		UserHandler userHandler=organizationService_.getUserHandler();

		try {
			if (userHandler.findUserByName(inputUserName) == null) {
				return Response.ok("true").withMimeType("text/html; charset=UTF-8").withHeader("Cache-Control", "no-cache");
			}
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return Response.ok("false").withMimeType("text/html; charset=UTF-8").withHeader("Cache-Control", "no-cache");

	}

	@Ajax
	@Resource
	@Route("/isValidEmail")
	public Response.Content isValidEmail(String inputEmail) {
		UserHandler userHandler=organizationService_.getUserHandler();

		try {
			Query query = new Query();
			query.setEmail(inputEmail);
			if (userHandler.findUsersByQuery(query).getSize() == 0) {
				return Response.ok("true").withMimeType("text/html; charset=UTF-8").withHeader("Cache-Control", "no-cache");

			}
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return Response.ok("false").withMimeType("text/html; charset=UTF-8").withHeader("Cache-Control", "no-cache");

	}

	@View
	public void displayAddForm() {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("user", null);
		parameters.put("isManager", false);
		editUser.render(parameters);
	}

	@View
	public void displayEditForm(String userName) {
		User user = null;
		boolean isManager = false;
		try {
			user = organizationService_.getUserHandler().findUserByName(userName);

			isManager = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId).getManagers().contains(userName);



		} catch (Exception e) {
			e.printStackTrace();

		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("user", user);
		parameters.put("isManager", isManager);
		editUser.render(parameters);
	}


	@Action
	@Route("/editUser")
	public Response.View editUser(String inputUserName,
								  String inputUserNameHidden,
								  String inputFirstName,
								  String inputLastName,
								  String inputEmail,
								  String inputPassword,
								  String inputConfirmPassword,
								  String inputIsManager) {



		try {
			UserHandler userHandler = organizationService_.getUserHandler();
			Organization organization =ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);
			User user;

			if (inputUserNameHidden == null) {
				user = userHandler.createUserInstance(inputUserName);
				user.setEmail(inputEmail);
				user.setFirstName(inputFirstName);
				user.setLastName(inputLastName);
				//todo encode password to increase security => modification in login
				// user.setPassword(Util.encodeMD5(pass1x)) ;
				user.setPassword(inputPassword);
				try {
					if (userHandler.findUserByName(user.getUserName()) != null) {
						flash.setError("User "+inputUserName+" already exists.");
						return UsersManagement_.index();
					}
					Query query = new Query();
					query.setEmail(inputEmail);
					if (userHandler.findUsersByQuery(query).getSize() > 0) {
						flash.setError("Email " + inputEmail + " already exists.");
						return UsersManagement_.index();
					}
					organizationService_.getUserHandler().createUser(user, true);
				} catch (Exception e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					flash.setError("Error when adding user.");
					return UsersManagement_.index();
				}


				Group organizationGroup = organizationService_.getGroupHandler().findGroupById(organization.getGroupId());
				MembershipType membershipTypeMember = organizationService_.getMembershipTypeHandler().findMembershipType("member");
				organizationService_.getMembershipHandler().linkMembership(user, organizationGroup, membershipTypeMember, true);

				flash.setSuccess("User "+inputUserName+" created.");
			} else {
				user = userHandler.findUserByName(inputUserNameHidden);
				user.setEmail(inputEmail);
				user.setLastName(inputLastName);
				user.setFirstName(inputFirstName);

				organizationService_.getUserHandler().saveUser(user,true);
				flash.setSuccess("User "+inputUserNameHidden+" modified.");

			}

			boolean isOrganizationManager = inputIsManager!= null && inputIsManager.equals("on");
			Set<String> managers = organization.getManagers();

			if (isOrganizationManager && !managers.contains(user.getUserName())) {
				managers.add(user.getUserName());
				organization.setManagers(managers);
				ocwDataService_.getOrganizationDAO().saveOrganization(organization);
			} else if (!isOrganizationManager && managers.contains(user.getUserName())) {
				managers.remove(user.getUserName());
				organization.setManagers(managers);
				ocwDataService_.getOrganizationDAO().saveOrganization(organization);
			}
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		return UsersManagement_.index();
	}

	@View
	public void displayEditRolesForm(String userName) {
		User user = null;
		try {
			user = organizationService_.getUserHandler().findUserByName(userName);
		} catch (Exception e) {
			e.printStackTrace();

		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("user", user);


		Collection<String> actualMembershipsType = new ArrayList<String>();
		Collection<String> possibleMembershipsType = new ArrayList<String>();

		try {

			Collection<MembershipType> allMembershipsType=organizationService_.getMembershipTypeHandler().findMembershipTypes();

			for (MembershipType mt : allMembershipsType) {
				if(organizationService_.getMembershipHandler().findMembershipByUserGroupAndType(user.getUserName(),
						ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId).getGroupId(),
						mt.getName()) == null) {
					possibleMembershipsType.add(mt.getName());
				} else {
					actualMembershipsType.add(mt.getName());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


		parameters.put("possibleMembershipsType", possibleMembershipsType);
		parameters.put("actualMembershipsType", actualMembershipsType);

		editRoles.render(parameters);
	}





}
