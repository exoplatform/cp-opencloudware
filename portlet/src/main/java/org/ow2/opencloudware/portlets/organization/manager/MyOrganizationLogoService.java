/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.opencloudware.portlets.organization.manager;

import org.apache.commons.fileupload.FileItem;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.impl.ImageUtils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.opencloudware.hibernate.model.Organization;

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.Session;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by The eXo Platform SAS Author : Nguyen Viet Bang
 * bangnv@exoplatform.com Jan 22, 2013
 */

public class MyOrganizationLogoService {
  private static final Log LOG               = ExoLogger.getExoLogger(MyOrganizationLogoService.class);

  public static int        logoHeight        = 34;

  RepositoryService        repositoryService;

  public static String     logo_preview_name = "logo_preview.png";

  public static String     logo_name         = "logo.png";

  public MyOrganizationLogoService() {
    repositoryService = (RepositoryService) ExoContainerContext.getCurrentContainer()
                                                               .getComponentInstanceOfType(RepositoryService.class);
  }



	public String getLogoURL(Organization organizationObject, boolean isRealLogo) {
		SessionProvider sessionProvider = SessionProvider.createSystemProvider();
		try {
			Session session = sessionProvider.getSession("collaboration",
					repositoryService.getCurrentRepository());
			Node rootNode = session.getRootNode();

			Node appData = rootNode.getNode("Groups"+organizationObject.getGroupId()+"/ApplicationData");
			if (!appData.hasNode(MyOrganization.OPENCLOUDWARE)) {
				appData.addNode(MyOrganization.OPENCLOUDWARE);
				appData.save();
			}
			Node ocw = appData.getNode(MyOrganization.OPENCLOUDWARE);


			if (isRealLogo) {
				if (ocw.hasNode(logo_name)) return "/portal/rest/jcr/repository/collaboration"+ocw.getPath() + "/"+logo_name+ "?"	+ System.currentTimeMillis();
			} else {
				if (ocw.hasNode(logo_preview_name)) return "/portal/rest/jcr/repository/collaboration"+ocw.getPath() + "/"+logo_preview_name+ "?"	+ System.currentTimeMillis();
			}

		} catch (Exception e) {
			LOG.error("Error while saving the logo: ", e.getMessage());
		} finally {
			sessionProvider.close();
		}

		return "";

	}

  /**
   * Save logo file in the jcr
   * 
   * @param item, item file
   */

  public void saveLogoPreview(FileItem item, Organization organizationObject) {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try {
    	Session session = sessionProvider.getSession("collaboration",
                                                   repositoryService.getCurrentRepository());
    	Node rootNode = session.getRootNode();

		Node appData = rootNode.getNode("Groups"+organizationObject.getGroupId()+"/ApplicationData");
		if (!appData.hasNode(MyOrganization.OPENCLOUDWARE)) {
			appData.addNode(MyOrganization.OPENCLOUDWARE);
			appData.save();
		}
		Node ocw = appData.getNode(MyOrganization.OPENCLOUDWARE);
      	Node fileNode = null;
      	if (ocw.hasNode(logo_preview_name)) {
        	fileNode = ocw.getNode(logo_preview_name);
        	fileNode.remove();
        	session.save();
      	}
      	fileNode = ocw.addNode(logo_preview_name, "nt:file");
      	Node jcrContent = fileNode.addNode("jcr:content", "nt:resource");
      	jcrContent.setProperty("jcr:data", resizeImage(item.getInputStream()));
      	jcrContent.setProperty("jcr:lastModified", Calendar.getInstance());
      	jcrContent.setProperty("jcr:encoding", "UTF-8");
      	jcrContent.setProperty("jcr:mimeType", item.getContentType());
      	ocw.save();
      	session.save();
    } catch (Exception e) {
      	LOG.error("Error while saving the logo: ", e.getMessage());
    } finally {
      	sessionProvider.close();
    }
  }

  /**
   * Move logo preview file to logo file
   */

  public void saveLogo(Organization organizationObject) {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try {
		Session session = sessionProvider.getSession("collaboration",
				repositoryService.getCurrentRepository());
		Node rootNode = session.getRootNode();

		Node appData = rootNode.getNode("Groups"+organizationObject.getGroupId()+"/ApplicationData");
		if (!appData.hasNode(MyOrganization.OPENCLOUDWARE)) {
			appData.addNode(MyOrganization.OPENCLOUDWARE);
			appData.save();
		}
		Node ocw = appData.getNode(MyOrganization.OPENCLOUDWARE);

      Node fileNode = null;

      if (ocw.hasNode(logo_name)) {
        fileNode = ocw.getNode(logo_name);
        fileNode.remove();
        session.save();
      }
      if (ocw.hasNode(logo_preview_name)) {
        fileNode = ocw.getNode(logo_preview_name);
        String newPath = fileNode.getPath().replace(logo_preview_name, logo_name);
        session.move(fileNode.getPath(), newPath);
        session.save();
      }
    } catch (Exception e) {
      LOG.error("Error while saving the logo: ", e.getMessage());
    } finally {
      sessionProvider.close();

    }
  }
  
  /**
   * resize image to scale, just used in custom size case. When 0 as specified as width then it will be calculated automatically to fit with expected height.
   * @param input
   * @return
   * @throws Exception
   */
  private static InputStream resizeImage(InputStream input) throws Exception{
    BufferedImage buffer = ImageIO.read(input);
    return ImageUtils.scaleImage(buffer, 0, logoHeight, false);
  }

}
