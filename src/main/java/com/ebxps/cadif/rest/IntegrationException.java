/**
 * 
 */
package com.ebxps.cadif.rest;

/**
 * Thrown to indicate that an operation cannot complete normally.
 * 
 * @author Steve Higgins - Orchestra Networks _ June 2017
 *
 */
public class IntegrationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -155736251638562226L;

	/**
	 * 
	 */
	public IntegrationException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public IntegrationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public IntegrationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IntegrationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public IntegrationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
