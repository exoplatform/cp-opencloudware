package org.ow2.opencloudware.portlets.join;

import juzu.*;
import juzu.request.RenderContext;
import juzu.template.Template;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.opencloudware.hibernate.OcwDataService;
import org.opencloudware.hibernate.model.Organization;
import org.ow2.opencloudware.portlets.common.Flash;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Romain Denarie
 * Date: 05/12/13
 * Time: 10:10
 * To change this template use File | Settings | File Templates.
 */
@SessionScoped
public class JoinOrganizationPortlet {
	@Inject
	@Path("index.gtmpl")
	Template index;


	@Inject
	Flash flash;


	OrganizationService organizationService_;
	OcwDataService ocwDataService_;
	MailService mailService_;

	String currentUser;

	@Inject
	public JoinOrganizationPortlet(OrganizationService organizationService, OcwDataService ocwDataService, MailService mailService) {
		organizationService_=organizationService;
		ocwDataService_=ocwDataService;
		mailService_=mailService;
	}

	@View
	public void index(RenderContext renderContext) {

		currentUser = renderContext.getSecurityContext().getRemoteUser();

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("flash", flash);
		index.render(parameters);
		flash.setError("");
		flash.setSuccess("");

	}

	@Action
	@Route("/joinOrganization")
	public Response.View joinOrganization(String inputOrganizationName) {
		try {


			User user = organizationService_.getUserHandler().findUserByName(currentUser);

			String mailFrom = "noreply@opencloudware.ow2.org";
			String mailTo = user.getEmail();

			String subject = "Request to join organization "+inputOrganizationName;
			String message = "";

			Organization organization = ocwDataService_.getOrganizationDAO().findOrganizationByName(inputOrganizationName);

			if (organization == null) {
				message = "Hello "+user.getFirstName()+" "+user.getLastName()+"\n";
				message += "Your request to join organization "+inputOrganizationName+" has been received by our services.\n";
				message += "Unfortunatelly, the organization name you provided was not correct. We have no organization with this name.\n";
				message += "Thanks to retry with another organization name.\n";
				message += "----------------------\n";
				message += "This message has been generated automatically as notification about request to join organization. Please do not reply.";
			} else {
				message = "Hello "+user.getFirstName()+" "+user.getLastName()+"\n";
				message += "Your request to join organization "+inputOrganizationName+" has been received by our services.\n";
				message += "We transmit it to organization managers. You'll received notification by email when they evaluate it.\n";
				message += "----------------------\n";
				message += "This message has been generated automatically as notification about request to join organization. Please do not reply.";

				Set<String> requests = organization.getUsersRequest();
				requests.add(user.getUserName());
				organization.setUsersRequest(requests);
				ocwDataService_.getOrganizationDAO().saveOrganization(organization);
			}



			mailService_.sendMessage(mailFrom, mailTo, subject, message);
			flash.setSuccess("Your request to organization " + inputOrganizationName + " has been correctly sent. You will receive a confirmation mail.");

		} catch (Exception e) {
			e.printStackTrace();
			flash.setError("Error when getting current user");
			flash.setSuccess("");
		}

		return JoinOrganizationPortlet_.index();
	}

}
