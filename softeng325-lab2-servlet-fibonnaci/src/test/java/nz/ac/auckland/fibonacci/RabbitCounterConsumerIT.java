package nz.ac.auckland.fibonacci;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class to exercise the RabbitCounterServlet.
 * 
 * This is an integration test, hence its IT suffix. Maven's FailSafe plugin 
 * for running integration tests expects, by default, that integration tests
 * conform to this naming convention.
 * 
 * This uses the standard JDK classes for writing HTTP clients. When using a 
 * framework, e.g. JAX-RS, writing clients is easier because the framework 
 * provides more abstraction. For now, we'll use only the JDK classes to make
 * HTTP requests to a servlet.
 *
 */
public class RabbitCounterConsumerIT {
	// Create a Logger for output.
	private static Logger _logger = LoggerFactory.getLogger(RabbitCounterConsumerIT.class);
	
	// Communication endpoint for the RabbitCounter Web service.
	private static final String url = "http://localhost:10000/rabbit";
	
	@Test
	public void sendRequests() {
		try {
			HttpURLConnection conn = null;
			
			// POST request to create some Fibonacci numbers.
			List<Integer> nums = new ArrayList<Integer>();
			for(int i = 1; i < 15; i++) {
				nums.add(i);
			}
			String payload = URLEncoder.encode("nums", "UTF-8") + "=" +
					URLEncoder.encode(nums.toString(), "UTF-8");
			
			// Send the request.
			conn = getConnection(url, "POST");
			conn.setRequestProperty("accept", "text/xml");
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.writeBytes(payload);
			out.flush();
			getResponse(conn);
			
			// GET to test whether POST worked.
			conn = getConnection(url, "GET");
			conn.setRequestProperty("accept", "text/xml");
			conn.connect();
			getResponse(conn);
			
			conn = getConnection(url + "?num=12", "GET");
			conn.addRequestProperty("accept", "text/plain");
			conn.connect();
			getResponse(conn);
			
			// DELETE request.
			conn = getConnection(url + "?num=12", "DELETE");
			conn.setRequestProperty("accept", "text/xml");
			conn.connect();
			getResponse(conn);
			
			// GET request to test whether DELETE worked.
			conn = getConnection(url + "?num=12", "GET");
			conn.addRequestProperty("accept", "text/plain");
			conn.connect();
			getResponse(conn);
		} catch(IOException e) {
			_logger.error(e.toString());
		} catch(NullPointerException e) {
			_logger.error(e.toString());
		}
	}
	
	private HttpURLConnection getConnection(String urlString, String verb) {
		HttpURLConnection conn = null;
		
		try {
			URL url = new URL(urlString);
			conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod(verb);
			conn.setDoInput(true);
			conn.setDoOutput(true);
		} catch(MalformedURLException e) {
			_logger.error(e.toString());
		} catch(IOException e) {
			_logger.error(e.toString());
		}
		return conn;
	}
	
	private void getResponse(HttpURLConnection conn) {
		try {
			String response = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String next = null;
			while((next = reader.readLine()) != null) {
				response += next;
			}
			_logger.info("The response: " + response);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
