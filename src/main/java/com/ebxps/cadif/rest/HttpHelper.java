package com.ebxps.cadif.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.ebxps.cadif.CrmpPaths;
import com.ebxps.cadif.Tools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.onwbp.adaptation.Adaptation;
import com.onwbp.org.apache.log4j.Category;


/**
 * Helper class that understands how to integrate with the integration platform using its REST services. 
 * 
 * @author Steve Higgins - Orchestra Networks - December 2017
 *
 */
public class HttpHelper {

	/** Logger. */
	private static final Category log = Tools.getCategory();
	
	/** the integration platform account credentials, encoded using Base64. */
	private String encodedCredentials = null;
	
	/** Name (label) of the endpoint. */
	private String endpointName = null;
	
	/** Endpoint URI. */
	private String endpointURI = null;
	
	/** HTTP Method. */
	private String httpMethod = null;
	
	/** HTTP request configuration. */
	private RequestConfig requestConfig = null;
	
	/** Java/JSON databinding is performed by the GSON library. */
	private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	
	/**
	 * Construct a new the integration platform helper with a set of credentials.
	 * @param nipUser Account user identifier
	 * @param nipPassword Account password
	 * @param socketTimeout Socket timeout (in milliseconds) for HTTP requests
	 */
	public HttpHelper(Adaptation endpointSpec, int socketTimeout) {
		
		// Extract the credentials
		String userid = endpointSpec.getString(CrmpPaths._Endpoints_Endpoint._Userid);
		String password = endpointSpec.getString(CrmpPaths._Endpoints_Endpoint._Password);
		setCredentials(userid, password);
		
		// Extract the URI and HTTP method
		this.endpointURI = endpointSpec.getString(CrmpPaths._Endpoints_Endpoint._Uri);
		this.endpointName = endpointSpec.getString(CrmpPaths._Endpoints_Endpoint._EndpointName);
		this.httpMethod = endpointSpec.getString(CrmpPaths._Endpoints_Endpoint._HttpMethod);
		
		// Build a request config that includes the socket timeout 
		this.requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).build();
		
	}

	/**
	 * Encode the user credentials for transmission to the integration platform
	 * @param user Account user identifier
	 * @param password Account password
	 */
	private void setCredentials(String user, String password) {
		String creds = String.format("%s:%s", user, password);
	    encodedCredentials = new String(Base64.getEncoder().encode(creds.getBytes()));
	}
	
	/**
	 * Send details of an object modification to the integration platform
	 * @param notification 
	 * @throws IntegrationException if the communication with the integration platform goes wrong
	 */
	public void sendNotification(NotificationMessage notification) throws IntegrationException {
		String parms = gson.toJson(notification);
		log.debug(String.format("\"%s\" (%s %s): %s", endpointName, httpMethod, endpointURI, parms));
		invokeService(parms, RestResponse.class);
	}
			
	/**
	 * Invoke a specific the integration platform API with the correct URL and HTTP method.
	 * @param targetApi The target API to invoke
	 * @param parms The parameters to pass. This will be passed as part of the service URL if the HTTP method
	 * is GET, otherwise it's assumed to be a JSON structure and is passed as the message body for POSTs.
	 * @param responseClass The expected response class from the the integration platform service
	 * @return A populated instance of the response class.
	 * @throws IntegrationException if anything goes wrong with the the integration platform service call.
	 */
	private <T> T invokeService(String parms, Class<T> responseClass) throws IntegrationException {

		// Send a GET or POST request to the integration platform and collect the raw json response
		String rawJson = null;
		if (httpMethod.equals("GET")) {
			
			// Get a response using HTTP GET
			HttpGet httpGet = new HttpGet(endpointURI + parms);
			rawJson = invokeService(httpGet);
			
		} else if (httpMethod.equals("POST")) {
			
			// Get a response using HTTP POST
			try {
				HttpPost httpPost = new HttpPost(endpointURI);
				StringEntity entity = new StringEntity(parms);
				httpPost.setEntity(entity);
				httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
				rawJson = invokeService(httpPost);
			} catch (UnsupportedEncodingException e) {
				throw new IntegrationException("Unsupported encoding in HTTP POST body", e);
			}
			
		} else {
			
			// Shouldn't happen but just in case
			throw new IntegrationException("Unsupported HTTP method " + httpMethod);
			
		}
		
		// Try to convert the raw response to an object of the required class
		try {
			return gson.fromJson(rawJson, responseClass);
		} catch (JsonSyntaxException e) {
			// Rethrow exceptions as the integration platform exceptions
			throw new IntegrationException(e.getMessage(),e);
		}
	
	}
	
	/**
	 * Invoke a service and return the raw JSON-format response. If the access token has expired then
	 * a new one will be requested and the service will be invoked again. If that retry fails then
	 * the whole request fails.
	 * @param request A fully configure GET or POST request
	 * @return The raw JSON response from the integration platform 
	 * @throws IntegrationException if the request fails 
	 */
	private String invokeService(HttpUriRequest request) throws IntegrationException {

		// Add an explicit authorisation header
        request.setHeader(HttpHeaders.AUTHORIZATION, String.format("Basic %s", encodedCredentials));

		// Create a builder for the HTTP client
		HttpClientBuilder builder = HttpClients.custom()
				.setDefaultRequestConfig(requestConfig);
		
		// Invoke the service
		try (CloseableHttpClient httpClient = builder.build()) {
		    
            StatusLine httpStatusLine = null; 

            // Execute the request
            CloseableHttpResponse httpResponse = httpClient.execute(request);		// Can throw IOException, ClientProtocolException
            
            // Check the HTTP status code
            httpStatusLine = httpResponse.getStatusLine();
            if (httpStatusLine.getStatusCode() >= 200 && httpStatusLine.getStatusCode() < 300) {	// was HttpURLConnection.HTTP_OK

            		// Extract and return the JSON-format response
                HttpEntity entity = httpResponse.getEntity();
                return EntityUtils.toString(entity);		// Can throw IOException 
                	            	
            } else {
            	
            		// Bad HTTP status code ... throw a the integration platform exception 
            		String nipResponseJson = "";
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                		nipResponseJson = String.format(" - %s", EntityUtils.toString(entity));		// Can throw IOException
                }
            	
                // Build a message for the exception
                String msg = String.format("HTTP %s failed on %s. Status %03d (%s)%s",
            			request.getMethod(), request.getURI(), 
            			httpStatusLine.getStatusCode(), httpStatusLine.getReasonPhrase(), 
            			nipResponseJson);
                throw new IntegrationException(msg);
                
            }
            
		} catch (ClientProtocolException e) {

			// Emit an error message and stack trace
            String msg = String.format("HTTP %s failed on %s. ClientProtocolException was thrown - %s", 
            		request.getMethod(), request.getURI(), e.getMessage());
			log.error(msg);
			e.printStackTrace();
			
			// Rethrow exceptions as the integration platform exceptions
			throw new IntegrationException(e.getMessage(),e);
						
		} catch (IOException e) {

			// Emit an error message and stack trace
            String msg = String.format("HTTP %s failed on %s. IOException was thrown - %s", 
            		request.getMethod(), request.getURI(), e.getMessage());
			log.error(msg);
			e.printStackTrace();
			
			// Rethrow exceptions as the integration platform exceptions
			throw new IntegrationException(e.getMessage(),e);
			
		}
		
	}		

	// Inner class for de-serialising a response body from the integration platform
	private class RestResponse {
		@SerializedName("message") private String message;
		@SerializedName("status") private String status;
	}
	
}
