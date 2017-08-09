package nz.ac.auckland.parolee.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple JUnit test to test the behaviour of the Parolee Web service.
 * 
 * The test is implemented using the JAX-RS client API.
 *
 */
public class ParoleeResourceIT {
	
	private static Logger _logger = LoggerFactory
			.getLogger(ParoleeResourceIT.class);
	
	private static String WEB_SERVICE_URI = "http://localhost:10000/services/parolees";
	
	private static Client _client;
	
	private static String[] _xmlPayloads = {
		"<parolee>" + "<first-name>Al</first-name>"
				+ "<last-name>Capone</last-name>"
				+ "<gender>Male</gender>"
				+ "<date-of-birth>17/01/1899</date-of-birth>"
				+ "</parolee>",

		"<parolee>" + "<first-name>John</first-name>"
				+ "<last-name>Gotti</last-name>"
				+ "<gender>Male</gender>"
				+ "<date-of-birth>27/10/1940</date-of-birth>"
				+ "</parolee>",

		"<parolee>" + "<first-name>Pablo</first-name>"
				+ "<last-name>Escobar</last-name>"
				+ "<gender>Male</gender>"
				+ "<date-of-birth>01/12/1949</date-of-birth>"
				+ "</parolee>",

		"<parolee>" + "<first-name>Carlos</first-name>"
				+ "<last-name>Marcello</last-name>"
				+ "<gender>Male</gender>"
				+ "<date-of-birth>6/2/1910</date-of-birth>"
				+ "</parolee>" };
	
	private static List<String> _paroleeUris = new ArrayList<String>();
	
	
	@BeforeClass
	public static void createClient() {
		// Use ClientBuilder to create a new client that can be used to create
		// connections to the Web service.
		_client = ClientBuilder.newClient();
	}
	
	@AfterClass
	public static void closeConnection() {
		_client.close();
	}
	
	@Before
	public void clearAndPopulate() {
		// Delete all Parolees in the Web service.
		Response response = _client.target(WEB_SERVICE_URI).request().delete();
		response.close();
		
		// Clear Parolee Uris
		_paroleeUris.clear();
		
		// Populate the service with Parolees.
		for (String payload : _xmlPayloads) {
			response = _client.target(WEB_SERVICE_URI).request()
					.post(Entity.xml(payload));
			String paroleeUri = response.getLocation().toString();
			_paroleeUris.add(paroleeUri);
			response.close();
		}
	}
	
	@Test
	public void testCreate() {
		// XML representation of the new Parolee.
		String xmlPayload = "<parolee>" + "<first-name>Jesse</first-name>"
				+ "<last-name>James</last-name>"
				+ "<gender>Male</gender>"
				+ "<date-of-birth>05/09/1847</date-of-birth>"
				+ "</parolee>";
		
		// Make a HTTP POST request to create a new Parolee.
		Response response = _client.target(WEB_SERVICE_URI).request().post(Entity.xml(xmlPayload));
		
		// Check that the HTTP response code is 201 Created.
		int responseCode = response.getStatus();
		assertEquals(201, responseCode);
		
		String paroleeUri = response.getLocation().toString();
		_logger.info("Uri of newly created Parolee: " + paroleeUri);
		
		// Close the Response object.
		response.close();
	}
	
	@Test
	public void testRetrieve() {
		String paroleeUri = _paroleeUris.get(_paroleeUris.size()-1);

		// Make a HTTP GET request to retrieve the last created Parolee.
		Response response = _client.target(paroleeUri).request().get();
		
		// Check that the HTTP response code is 200 OK.
		int responseCode = response.getStatus();
		assertEquals(200, responseCode);
		
		String xmlResponse = response.readEntity(String.class);
		_logger.info("Retrieved Parolee: " + xmlResponse);	
		
		// Close the Response object.
		response.close();
			
	}
	
	@Test
	public void testUpdate() {
		// Create a XML representation of the first parolee, changing Al
		// Capone's gender.
		String updateParolee = "<parolee>" + "<first-name>Al</first-name>"
				+ "<last-name>Capone</last-name>"
				+ "<gender>Female</gender>"
				+ "<date-of-birth>17/01/1899</date-of-birth>"
				+ "</parolee>";

		// Make a HTTP PUT request to update the Parolee.
		Response response = _client.target(WEB_SERVICE_URI + "/1").request()
				.put(Entity.xml(updateParolee));

		// Check that the HTTP response code is 204 No content.
		int status = response.getStatus();
		assertEquals(204, status);
		
		// Close the Response object.
		response.close();
	}
	
	@Test
	public void testDelete() {
		// Make a HTTP DELETE request to delete the first Parolee.
		Response response = _client.target(WEB_SERVICE_URI + "/1").request().delete();

		// Check that the HTTP response code is 204 No content.
		int status = response.getStatus();
		assertEquals(204, status);
		
		// Close the Response object.
		response.close();
	}
}