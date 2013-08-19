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


@Bindings({@Binding(Flash.class)})
@Servlet("/")
@Assets(
		/*scripts = {
				@Script(id = "jquery", src = "javascripts/jquery-1.8.3.min.js"),
				/*@Script(id = "transition", src = "javascripts/bootstrap-transition.js", depends = "jquery"),
				@Script(id = "collapse", src = "javascripts/bootstrap-collapse.js", depends = {"jquery", "transition"}),
				@Script(src = "javascripts/register.js", depends = {"jquery", "collapse"})
				*/

		//}//,
		//stylesheets = {
		//		@Stylesheet(src = "stylesheets/bootstrap-3.0.0.RC1.min.css")
		//}


)
package org.ow2.opencloudware.portlets;

import juzu.plugin.asset.Assets;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.servlet.Servlet;
import org.ow2.opencloudware.portlets.register.Flash;