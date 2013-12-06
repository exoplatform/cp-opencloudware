/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@Application
@Bindings(
		{
				@Binding(value = org.exoplatform.services.organization.OrganizationService.class),
				@Binding(value = org.opencloudware.hibernate.OcwDataService.class)
		}
)

@Portlet
@Servlet("/register/*")

@Assets(
		scripts = {
				@Script(id = "jquery", src = "js/common/jquery-1.8.3.min.js", location = AssetLocation.SERVER),
				@Script(id = "jqueryValidate", src = "js/common/jquery.validate.min.js", depends = "jquery", location = AssetLocation.SERVER),
				@Script(id = "transition", src = "js/register/bootstrap-transition.js", depends = "jquery", location = AssetLocation.SERVER),
				@Script(id = "collapse", src = "js/register/bootstrap-collapse.js", depends = {"jquery", "transition"}, location = AssetLocation.SERVER),
				@Script(src = "js/register/register.js", depends = {"jquery", "collapse"}, location = AssetLocation.SERVER)

		},
		stylesheets = {
				@Stylesheet(id = "common", src = "skin/common/common.css", location = AssetLocation.SERVER),
				@Stylesheet(src = "skin/register/register.css", depends = "common", location = AssetLocation.SERVER)

		}


)
package org.ow2.opencloudware.portlets.register;

import juzu.Application;
import juzu.asset.AssetLocation;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;
import juzu.plugin.servlet.Servlet;

