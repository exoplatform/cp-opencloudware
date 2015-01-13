package org.ow2.opencloudware.portlets.organization.manager;

import juzu.*;
import juzu.io.Stream;
import juzu.plugin.ajax.Ajax;
import juzu.request.HttpContext;
import juzu.template.Template;
import org.apache.commons.fileupload.FileItem;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.security.ConversationState;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.dao.OrganizationDAO;
import org.opencloudware.hibernate.model.Organization;
import org.ow2.opencloudware.portlets.common.Flash;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Romain Denarie
 * Date: 23/08/13
 * Time: 10:33
 * To change this template use File | Settings | File Templates.
 */
@SessionScoped
public class MyOrganization {


	public static final String OPENCLOUDWARE = "opencloudware";
	@Inject
	@Path("global.gtmpl")
	Template global;

	@Inject
	@Path("organization.gtmpl")
	Template organization;

	@Inject
	@Path("organizationInformation.gtmpl")
	Template organizationInformation;

	@Inject
	@Path("organizationEditInformation.gtmpl")
	Template organizationEditInformation;


	@Inject
	@Path("logo.gtmpl")
	Template logoFragment;

	@Inject
	@Path("uploadLogo.gtmpl")
	Template uploadLogo;

	@Inject
	@Path("usersRequests.gtmpl")
	Template usersRequests;

	@Inject
	@Path("userLine.gtmpl")
	Template userLine;


	private OrganizationService organizationService_;
	private OcwDataService ocwDataService_;
	private RepositoryService repositoryService_;
	private MailService mailService_;

	@Inject
	Flash flash;


	@Inject
	MyOrganizationLogoService logoService;

	String currentOrganizationId;


	@Inject
	public void MyOrganization(OrganizationService organizationService, OcwDataService ocwDataService, RepositoryService repositoryService, MailService mailService) {
		organizationService_=organizationService;
		ocwDataService_=ocwDataService;
		repositoryService_=repositoryService;
		mailService_=mailService;
		currentOrganizationId="";
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
	@Route("/getOrganizationFragment/{organizationId}")
	public void getOrganizationFragment(String organizationId) {
		try {
			Organization organizationObject = ocwDataService_.getOrganizationDAO().findOrganizationById(organizationId);
			currentOrganizationId=organizationId;

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("organization",organizationObject);
			parameters.put("flash",flash);
			parameters.put("currentOrganizationId",currentOrganizationId);
			organization.render(parameters);
		} catch	(Exception e) {
			e.printStackTrace();
			MyOrganization_.index();
		}


	}

	@Ajax
	@Resource
	@Route("/getOrganizationFragmentByGroupId/{groupId}")
	public void getOrganizationFragmentByGroupId(String groupId) {
		try {

			Group group = organizationService_.getGroupHandler().findGroupById(groupId);
			if (group == null) {
				MyOrganization_.index();

			} else {
			    String organizationName = group.getLabel();

				Organization organizationObject = ocwDataService_.getOrganizationDAO().findOrganizationByName(organizationName);
				currentOrganizationId=organizationObject.getId();

				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("organization",organizationObject);
				parameters.put("flash",flash);

				parameters.put("currentOrganizationId",currentOrganizationId);
				organization.render(parameters);
			}
		} catch	(Exception e) {
			e.printStackTrace();
			MyOrganization_.index();
		}


	}


	@Ajax
	@Resource
	@Route("/getOrganizationInformationFragment")
	public void getOrganizationInformationFragment() {
		try {
            ConversationState conversationState = ConversationState.getCurrent();
            org.exoplatform.services.security.Identity identity = conversationState.getIdentity();
            String userId = identity.getUserId();
            boolean isManager=false;

			Organization organizationObject = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);

			String managers = "";
			for (String manager : organizationObject.getManagers()) {
				User user = organizationService_.getUserHandler().findUserByName(manager);
                if (user.getUserName().equals(userId)) isManager=true;
				if (user.getFirstName() == null || user.getLastName() == null) {
					managers += manager;
				} else {
					managers += user.getFirstName()+" "+ user.getLastName() + ", ";
				}

			}

			if (!managers.equals("")) managers=managers.substring(0,managers.length()-2);
			else managers = "No managers configured";

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("organization",organizationObject);
			parameters.put("managersString",managers);
            parameters.put("isManager",isManager);

			organizationInformation.render(parameters);
		} catch	(Exception e) {
			MyOrganization_.index();
		}
	}


	@Ajax
	@Resource
	@Route("/getOrganizationEditFragment/")
	public void getOrganizationEditFragment() {
		try {
			Organization organizationObject = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);


			Set<String> managers = organizationObject.getManagers();
			ListAccess<User> users = organizationService_.getUserHandler().findUsersByGroupId(organizationObject.getGroupId());

			Map<String, String> managersNames = new HashMap<String,String>();
			Map<String, String> usersNames = new HashMap<String,String>();
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
				if (managers.contains(user.getUserName())) {
					managersNames.put(user.getUserName(), fullName);
				} else {
					usersNames.put(user.getUserName(), fullName);
				}

			}


			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("organization",organizationObject);
			parameters.put("usersNames",usersNames);
			parameters.put("managersNames",managersNames);

			organizationEditInformation.render(parameters);
		} catch	(Exception e) {
			MyOrganization_.index();
		}
	}

	@Ajax
	@Resource
	@Route("/getUploadLogoFragment/")
	public void getUploadLogoFragment() {
		try {
			Organization organizationObject = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);


			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("organization",organizationObject);
			parameters.put("logoPath",logoService.getLogoURL(organizationObject,true));
			uploadLogo.render(parameters);
		} catch	(Exception e) {
			e.printStackTrace();
			MyOrganization_.index();
		}
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
	@Route("/editOrganization")
	public Response.View editOrganization(String inputOrganizationId,
										  String oldOrganizationName,
										  String inputOrganizationName,
										  String inputOrganizationAddress,
										  String inputUsersNames,
										  String inputManagersNames) {


		//TODO : add a check to prevent to remove all managers
		//should be done in js in gtml AND here

		GroupHandler groupHandler = organizationService_.getGroupHandler();

		try {

			OrganizationDAO organizationDAO = ocwDataService_.getOrganizationDAO();
			Organization oldOrganization = organizationDAO.findOrganizationById(inputOrganizationId);
			if (oldOrganization == null) {
				flash.setError("This organization with id \""+inputOrganizationId+"\" doesn't exist.");
				flash.setSuccess("");
				return MyOrganization_.index();
			}



			oldOrganization.setOrganizationName(inputOrganizationName);
			oldOrganization.setAddress(inputOrganizationAddress);


			String[] managers = inputManagersNames.split(",");

			Set<String> oldManagers = oldOrganization.getManagers();
			oldManagers.clear();
			for (String manager : managers) {
				oldManagers.add(manager);
			}
			oldOrganization.setManagers(oldManagers);



			Group group = groupHandler.findGroupById(oldOrganization.getGroupId());
			//on ne peut pas changer le nom d'un group => on change que le label
			group.setLabel(inputOrganizationName);
			groupHandler.saveGroup(group,true);

			organizationDAO.saveOrganization(oldOrganization);

			flash.setSuccess("Organization \""+inputOrganizationName+"\" modified.");
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		return MyOrganization_.index();
	}


	/**
	 * create a object JSON from the map.
	 *

	 * @return Response.Content
	 */
	private Response.Content<Stream.Char> createJSON(final Map<String, String> data) {
		Response.Content<Stream.Char> json = new Response.Content<Stream.Char>(200, Stream.Char.class) {
			@Override
			public String getMimeType() {
				return "application/json";
			}

			@Override
			public void send(Stream.Char stream) throws IOException {
				stream.append("{");
				Iterator<Map.Entry<String, String>> i = data.entrySet().iterator();
				while (i.hasNext()) {
					Map.Entry<String, String> entry = i.next();
					stream.append("\"" + entry.getKey() + "\"");
					stream.append(":");
					stream.append("\"" + entry.getValue() + "\"");
					if (i.hasNext()) {
						stream.append(",");
					}
				}
				stream.append("}");
			}
		};
		return json;
	}

	/**
	 * create a object text/html
	 *
	 * @param text
	 * @return
	 */
	private Response.Content<Stream.Char> createText(final String text) {
		Response.Content<Stream.Char> textObject = new Response.Content<Stream.Char>(200,
				Stream.Char.class) {
			@Override
			public String getMimeType() {
				return "text/html";
			}

			@Override
			public void send(Stream.Char stream) throws IOException {
				stream.append(text);
			}
		};
		return textObject;
	}

	/**
	 * method save() records an image in BrandingDataStorageService
	 *
	 * @param httpContext
	 * @param file
	 * @return Response.Content
	 * @throws IOException
	 */
	@Resource
	public Response.Content uploadFile(HttpContext httpContext, FileItem file, String browser) throws IOException {

		try {
			Organization organization = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);

			if (browser != null && browser.equals("html5")) {
				if (file != null && file.getContentType().contains("png")) {
					logoService.saveLogoPreview(file,organization);
				}
				Map<String, String> result = new HashMap<String, String>();
				result.put("logoUrl", logoService.getLogoURL(organization, false));
				return createJSON(result);
			} else {
				if (file != null && file.getContentType().contains("png")) {
					logoService.saveLogoPreview(file,organization);
					return createText(logoService.getLogoURL(organization, false));
				} else {
					return createText("false");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return createText("false");
		}
	}



	/**
	 * this method will be invoked when the user click on save
	 *
	 * @param isChangeLogo to know if the logo will be update from logo preview
	 * @return
	 */
	@Ajax
	@Resource
	public void saveLogo(String isChangeLogo) {
		if (isChangeLogo != null && Boolean.valueOf(isChangeLogo)) {
			try {
				Organization organization = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);
				logoService.saveLogo(organization);
			} catch (Exception e) {
				e.printStackTrace();
			}


		}
		getUploadLogoFragment();
	}


	@Ajax
	@Resource
	@Route("/getUsersRequestsFragment")
	public void getUsersRequestsFragment() {
		try {
			Organization organizationObject = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("usersRequests",organizationObject.getUsersRequest());
			usersRequests.render(parameters);
		} catch	(Exception e) {
			e.printStackTrace();
			MyOrganization_.index();
		}
	}

	@Ajax
	@Resource
	@Route("/getUserLine")
	public void getUserLine(String userName) {
		try {
			User user=organizationService_.getUserHandler().findUserByName(userName);

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("user", user);
			userLine.render(parameters);

		} catch (Exception e) {

			e.printStackTrace();
			index();
		}
	}

	@Action
	@Route("/applyUsersModification")
	public Response.View applyUsersModification(String inputAcceptedUsers, String inputRejectedUsers) {

		try {
			Organization organization = ocwDataService_.getOrganizationDAO().findOrganizationById(currentOrganizationId);

			if (!inputAcceptedUsers.equals("")) {
				String[] acceptedUsers = inputAcceptedUsers.split(";");
				int treatedUsers = 0;
				for (String userName : acceptedUsers) {

					//1 remove user from requests
					Set<String> usersRequest = organization.getUsersRequest();
					usersRequest.remove(userName);
					organization.setUsersRequest(usersRequest);
					ocwDataService_.getOrganizationDAO().saveOrganization(organization);


					//2 add user as member of the organization
					User user = organizationService_.getUserHandler().findUserByName(userName);
					Group organizationGroup = organizationService_.getGroupHandler().findGroupById(organization.getGroupId());
					MembershipType membershipTypeMember = organizationService_.getMembershipTypeHandler().findMembershipType("member");
					organizationService_.getMembershipHandler().linkMembership(user, organizationGroup, membershipTypeMember, true);

					//3 send acceptation mail
					sendAcceptationMail(organization, user);

					treatedUsers++;
				}
				if (treatedUsers > 0) {
					flash.setSuccess(treatedUsers+" users accepted in your organization.");
				}
			}
			if (!inputRejectedUsers.equals("")) {
				String[] rejectedUsers = inputRejectedUsers.split(";");
				int treatedUsers = 0;
				for (String userName : rejectedUsers) {

					//1 remove user from requests
					Set<String> usersRequest = organization.getUsersRequest();
					usersRequest.remove(userName);
					organization.setUsersRequest(usersRequest);
					ocwDataService_.getOrganizationDAO().saveOrganization(organization);



					//2 send refuse mail
					User user = organizationService_.getUserHandler().findUserByName(userName);
					sendRefuseMail(organization, user);


					treatedUsers++;
				}
				flash.setSuccess(flash.getSuccess()+" "+treatedUsers+" users requests rejected.");
			}



		} catch (Exception e) {
			e.printStackTrace();
			flash.setSuccess("");
			flash.setError("Error when accepting and rejecting users.");
		}


		return MyOrganization_.index();


	}

	private void sendRefuseMail(Organization organization, User user) {
		String mailFrom = "noreply@opencloudware.ow2.org";
		String mailTo = user.getEmail();

		String subject = "Request to join organization "+organization.getOrganizationName();
		String message = "Hello "+user.getFirstName()+" "+user.getLastName()+"\n";
		message += "Your request to join organization "+organization.getOrganizationName()+" has been evaluated.\n";
		message += "Unfortunatelly, the organization refuses your request.\n";
		message += "Thanks for using OpenCloudware.\n";
		message += "----------------------\n";
		message += "This message has been generated automatically as notification about request to join organization. Please do not reply.";

		sendMail(mailFrom,mailTo,subject,message);



	}

	private void sendMail(String mailFrom, String mailTo, String subject, String message) {
		try {
			mailService_.sendMessage(mailFrom, mailTo, subject, message);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendAcceptationMail(Organization organization, User user) {
		String mailFrom = "noreply@opencloudware.ow2.org";
		String mailTo = user.getEmail();

		String subject = "Request to join organization "+organization.getOrganizationName();
		String message = "Hello "+user.getFirstName()+" "+user.getLastName()+"\n";
		message += "Your request to join organization "+organization.getOrganizationName()+" has been evaluated.\n";
		message += "The organization accepts your request. You will have access to organization working space at your next login\n";
		message += "Thanks for using OpenCloudware.\n";
		message += "----------------------\n";
		message += "This message has been generated automatically as notification about request to join organization. Please do not reply.";


		sendMail(mailFrom,mailTo,subject,message);

	}

}
