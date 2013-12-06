package org.opencloudware.filter;

import org.exoplatform.web.filter.Filter;
import org.gatein.wci.ServletContainerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogoutFilter implements Filter {

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

        ServletContainerFactory.getServletContainer().logout((HttpServletRequest)request, (HttpServletResponse)response);

        HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.sendRedirect(httpResponse.encodeRedirectURL("/logout"));

	}

}