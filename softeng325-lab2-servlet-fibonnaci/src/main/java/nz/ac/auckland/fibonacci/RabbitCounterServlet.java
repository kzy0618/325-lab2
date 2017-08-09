package nz.ac.auckland.fibonacci;

import java.beans.XMLEncoder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.*;

/**
 * Implementation of a REST Web service using HttpServlet. 
 *
 * Adapted from Java Web Services by Martin Kalin.
 *
 */
@SuppressWarnings("serial")
public class RabbitCounterServlet extends HttpServlet {
	// A RabitCounterServlet instance stores state in _cache, a Map where the 
	// key is the index position within a Fibonacci series, and value is the 
	// corresponding Fibonacci value.
	private Map<Integer, Integer> _cache;
	
	/**
	 * Servlet lifecycle method called by a servlet container before it is used 
	 * for request processing.
	 */
	@Override
	public void init() {
		// Initialise the Map.
		_cache = Collections.synchronizedMap(new HashMap<Integer,Integer>());
	}
	
	/**
	 * Hook method called by lifecycle method service(). doGet() is called to
	 * process HTTP GET messages. For RabbitCounterServlet, this method 
	 * attempts to return either i) a Fibonacci value at a specified position 
	 * with the Fibonacci sequence, or ii) all computed values. For i) the 
	 * index value is expected to be represented by a query parameter of the 
	 * HTTP GET message. If the requested value hasn't been generated this 
	 * method returns -1.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		String num = request.getParameter("num");
		
		// If no query string, assume client wants the full list.
		if(num == null) {
			Collection<Integer> fibs = _cache.values();
			sendTypedResponse(request, response, fibs);
		} else {
			try {
				Integer key = Integer.parseInt(num.trim());
				Integer fib = _cache.get(key);
				if(fib == null) {
					fib = -1;
				}
				sendTypedResponse(request, response, fib);
			} catch(NumberFormatException e) {
				sendTypedResponse(request, response, -1);
			}
		}
	}
	
	/**
	 * Hook method called by lifecycle method service(). doPost() is called to
	 * process HTTP POST messages. For RabbitCounterServlet, this method 
	 * expects a sequence of values to be stored in the body of the HTTP POST
	 * message. Each value represents the index value within the Fibonacci 
	 * sequence for which the corresponding Fibonacci number should be 
	 * generated. This method stores computed values.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String nums = request.getParameter("nums");
		if(nums == null) {
			// POST request doesn't contain any numbers for which to compute
			// Fibonacci values.
			throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		// Extract the integers from a string such as: "[1, 2, 3]".
		nums = nums.replace('[', '\0');
		nums = nums.replace(']', '\0');
		String[] parts = nums.split(", ");
		List<Integer> list = new ArrayList<Integer>();
		for(String next : parts) {
			int n = Integer.parseInt(next.trim());
			_cache.put(n, countRabbits(n));
			list.add(_cache.get(n));
		}
		sendTypedResponse(request, response, list + " added.");
	}
	
	/**
	 * Hook method called by lifecycle method service(). doDelete() is called to
	 * process HTTP DELETE messages. For RabbitCounterServlet, this method 
	 * expects a query parameter, num, which identifies the position within the
	 * Fibonacci sequence of the Fibonacci value to be deleted.
	 */
	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) {
		String key = request.getParameter("num");
		
		// DELETE request doesn't contain a sequence number for a Fibonacci 
		// value to delete.
		if(key == null) {
			throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		try {
			int n = Integer.parseInt(key.trim());
			_cache.remove(n);
			sendTypedResponse(request, response, n + " deleted.");
		} catch(NumberFormatException e) {
			throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) {
		throw new HTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
	
	@Override
	public void doHead(HttpServletRequest request, HttpServletResponse response) {
		throw new HTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
	
	@Override
	public void doOptions(HttpServletRequest request, HttpServletResponse response) {
		throw new HTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
	
	/**
	 * Helper method to examine the type of content required from the HTTP 
	 * request (the value of the Accept header) and generate a response in that
	 * format.
	 * 
	 * @param request an object representing the HTTP request.
	 * @param response an object representing the HTTP response.
	 * @param data the data to return as the result of processing to the 
	 *        consumer.
	 */
	private void sendTypedResponse(HttpServletRequest request,
			HttpServletResponse response,
			Object data) {
		String desiredType = request.getHeader("accept");
		
		// If client requests plain text or HTML, send it. Otherwise send XML.
		if(desiredType.contains("text/plain")) {
			sendPlain(response, data);
		} else if(desiredType.contains("text/html")) {
			sendHtml(response, data);
		} else {
			sendXml(response, data);
		}
	}
	
	/**
	 * Helper method to generate an XML response and return this to the
	 * consumer.
	 */
	private void sendXml(HttpServletResponse response, Object data) {
		try {
			XMLEncoder enc = new XMLEncoder(response.getOutputStream());
			enc.writeObject(data.toString());
			enc.close();
		} catch(IOException e) {
			throw new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Helper method to generate a HTML response and return this to the 
	 * consumer.
	 */
	private void sendHtml(HttpServletResponse response, Object data) {
		String htmlStart = 
				"<html><head><title>send_html response</title></head><body><div>";
		String htmlEnd = "</div></body></html>";
		String htmlDoc = htmlStart + data.toString() + htmlEnd;
		sendPlain(response, htmlDoc);
	}
	
	/**
	 * Helper method to generate a plain-text response and return this to the 
	 * consumer.
	 */
	private void sendPlain(HttpServletResponse response, Object data) {
		try {
			ServletOutputStream out = response.getOutputStream();
			out.write(data.toString().getBytes());
			out.flush();
		} catch(IOException e) {
			throw new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Helper method to generate a Fibonacci value at a given position within 
	 * Fibonacci sequence.
	 */
	private int countRabbits(int n) {
		if(n < 0) {
			throw new HTTPException(403);
		}
		
		if(n < 2) {
			return n;
		}
		
		if(_cache.containsKey(n)) {
			return _cache.get(n);
		}
		
		if(_cache.containsKey(n - 1) && _cache.containsKey(n - 2)) {
			_cache.put(n, _cache.get(n - 1 ) + _cache.get(n - 2));
			return _cache.get(n);
		}
		
		int fib = 1;
		int prev = 0;
		for(int i = 2; i <= n; i++) {
			int temp = fib;
			fib += prev;
			prev = temp;
		}
		_cache.put(n, fib);
		return fib;
	}
}
