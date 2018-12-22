package com.ebxps.cadif;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.onwbp.base.repository.ModulesRegister;

/**
 * Register this webapp module and perform some startup tasks.
 * 
 * @author Orchestra Networks - Feb 2013
 *
 */
@SuppressWarnings("serial")
public class RegisterServlet extends HttpServlet
{

	/**
	 * Register the webapp module and perform some startup tasks.
	 * @param config A servlet configuration supplied by the application server
	 */
	public void init(ServletConfig config) throws ServletException
	{		
		// Run the superclass constructor
		super.init(config);

		// Register the module with EBX
		ModulesRegister.registerWebApp(this, config);

	}

	/**
	 * Unregister the webapp module.
	 */
	public void destroy() {
		ModulesRegister.unregisterWebApp(this, this.getServletConfig());
	}


}
