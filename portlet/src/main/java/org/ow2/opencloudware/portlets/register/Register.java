package org.ow2.opencloudware.portlets.register;

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
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.organization.*;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.dao.OrganizationDAO;
import org.opencloudware.hibernate.model.Organization;
import org.ow2.opencloudware.portlets.common.Flash;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@SessionScoped
public class Register {

	@Inject
	@Path("index.gtmpl")
	Template index;

	@Inject
	Flash flash;

	OrganizationService organizationService_;
	OcwDataService ocwDataService_;

	@Inject
	public void Register(OrganizationService organizationService, OcwDataService ocwDataService) {
		organizationService_=organizationService;
		ocwDataService_=ocwDataService;
	}


	@View
	public void index() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("flash", flash);
		index.render(parameters);
		flash.setError("");
		flash.setSuccess("");

	}

	@Ajax
	@Resource
	@Route("/isValidUsername")
	public Response.Content isValidUsername(String inputUserName) {
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
	@Route("/createUser")
	public Response.View createUser(String inputUserName,
									String inputFirstName,
									String inputLastName,
									String inputEmail,
									String inputPassword,
									String inputConfirmPassword,
									String inputRadio,
									String inputOrganizationName,
									String inputOrganizationAddress,
									String inputCreditCardNumber) {


		/*System.out.println("Add user :"+
				"inputUserName : "+inputUserName+
				" inputFirstName : "+inputFirstName+
				" inputLastName : "+inputLastName+
				" inputEmail : "+inputEmail+
				" inputPassword : "+inputPassword+
				" inputConfirmPassword : "+inputConfirmPassword+
				" inputRadio : "+inputRadio+
				" inputOrganizationName : "+inputOrganizationName+
				" inputOrganizationAddress : "+inputOrganizationAddress+
				" inputCreditCardNumber : "+inputCreditCardNumber);
          */
		//to test
		//user name not exists
		//mail not exists
		//password ==
		//fields filled


		if (!inputPassword.equals(inputConfirmPassword)) {
			flash.setError("Password and Confirm Password don't match.");
			return Register_.index();
		}

		UserHandler userHandler=organizationService_.getUserHandler();
		User user = userHandler.createUserInstance(inputUserName);
		user.setEmail(inputEmail);
		user.setFirstName(inputFirstName);
		user.setLastName(inputLastName);
		//todo encode password to increase security => modification in login
		// user.setPassword(Util.encodeMD5(pass1x)) ;
		user.setPassword(inputPassword);

		try {
			if (userHandler.findUserByName(user.getUserName()) != null) {
				flash.setError("User "+inputUserName+" already exists.");
				return Register_.index();
			}
			Query query = new Query();
			query.setEmail(inputEmail);
			if (userHandler.findUsersByQuery(query).getSize() > 0) {
				flash.setError("Email " + inputEmail + " already exists.");
				return Register_.index();
			}
			organizationService_.getUserHandler().createUser(user, true);
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			flash.setError("Error when adding user.");
			return Register_.index();
		}

		flash.setSuccess("User "+inputUserName+" added.");

		if (inputRadio.equals("Yes")) {
			GroupHandler groupHandler = organizationService_.getGroupHandler();

			String sanitizedOrganizationName = "/opencloudware/"+Utils.cleanString(inputOrganizationName);
			try {
				Group newGroup = groupHandler.findGroupById(sanitizedOrganizationName);
				if (newGroup != null) {
					flash.setError("This organization already exist. The user account is correctly created");
					flash.setSuccess("");
					return Register_.index();
				}


				newGroup = groupHandler.createGroupInstance();
				newGroup.setGroupName(Utils.cleanString(inputOrganizationName));
				newGroup.setLabel(inputOrganizationName);

				Group opencloudareGroup = groupHandler.findGroupById("/opencloudware");
				groupHandler.addChild(opencloudareGroup,newGroup,true);

				//todo this is no more necessary, because manager are managed by PDP. remove it ?
				//MembershipType membershipTypeManager = organizationService_.getMembershipTypeHandler().findMembershipType("manager");
				//organizationService_.getMembershipHandler().linkMembership(user, newGroup, membershipTypeManager, true);

				MembershipType membershipTypeMember = organizationService_.getMembershipTypeHandler().findMembershipType("member");
				organizationService_.getMembershipHandler().linkMembership(user, newGroup, membershipTypeMember, true);

				OrganizationDAO organizationDAO = ocwDataService_.getOrganizationDAO();
				Organization organization = organizationDAO.createOrganizationInstance(inputOrganizationName);
				organization.setCreditCardNumber(inputCreditCardNumber);
				organization.setGroupId(newGroup.getId());
				organization.setAddress(inputOrganizationAddress);
				Set<String> managers = new HashSet<String>();
				managers.add(user.getUserName());
				organization.setManagers(managers);
				organizationDAO.createOrganization(organization);


				flash.setSuccess("User "+inputUserName+" and organization "+inputOrganizationName+" added");
			} catch (Exception e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}

		}

		return Register_.index();
	}


}

