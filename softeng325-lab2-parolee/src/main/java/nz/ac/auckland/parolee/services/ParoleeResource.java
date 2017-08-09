package nz.ac.auckland.parolee.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import nz.ac.auckland.parolee.domain.Gender;
import nz.ac.auckland.parolee.domain.Parolee;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class to implement a simple REST Web service for managing parolees.
 * 
 * ParoleeResource implements a WEB service with the following interface:
 * 
 * - GET    <base-uri>/parolees/{id}
 *          Retrieves a parolee based on their unique id. The format of the 
 *          returned data is XML.
 *          
 * - POST   <base-uri>/parolees
 *          Creates a new Parolee. The HTTP post message contains an XML 
 *          representation of the parolee to be created.
 *          
 * - PUT    <base-uri>/parolees/{id}
 *          Updates a parolee, identified by their id.The HTTP PUT message
 *          contains an XML document describing the new state of the parolee.
 *          
 * - DELETE <base-uri>/parolees/{id}
 *          Deletes a parolee, identified by their unique id.
 *          
 * - DELETE <base-uri>/parolees
 *          Deletes all parolees.         
 *
 */
@Path("/parolees")
public class ParoleeResource {

	private static Logger _logger = LoggerFactory
			.getLogger(ParoleeResource.class);

	private Map<Long, Parolee> _paroleeDB = new ConcurrentHashMap<Long, Parolee>();
	private AtomicLong _idCounter = new AtomicLong();

	/**
	 * Attempts to retrieve a particular Parolee based on their unique id. If 
	 * the required Parolee is found, this method returns a 200 response along 
	 * with an XML representation of the Parolee. In other cases, this method 
	 * returns a 404 response.
	 *  
	 * @param id the unique id of the Parolee to be returned.
	 * 
	 * @return a StreamingOutput object that writes out the Parolee state in 
	 *         XML form.
	 */
	@GET
	@Path("{id}")
	@Produces("application/xml")
	public StreamingOutput retrieveParolee(@PathParam("id") long id) {
		_logger.info("Retrieving parolee with id: " + id);
		// Lookup the Parolee within the in-memory data structure.
		final Parolee parolee = _paroleeDB.get(id);
		if (parolee == null) {
			// Return a HTTP 404 response if the specified Parolee isn't found.
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		// Return a StreamingOuput instance that the JAX-RS implementation will
		// use to set the body of the HTTP response message.
		return new StreamingOutput() {
			public void write(OutputStream outputStream) throws IOException,
					WebApplicationException {
				outputParolee(outputStream, parolee);
			}
		};
	}

	/**
	 * Creates a new Parolee.
	 * 
	 * @param is the Inputstream that contains an XML representation of the
	 * Parolee to be created.
	 * 
	 * @return a Response object that includes the HTTP "Location" header,
	 *         whose value is the URI of the newly created resource. The HTTP 
	 *         response code is 201. The JAX-RS run-time processes the Response
	 *         object when preparing the HTTP response message.
	 */
	@POST
	@Produces("application/xml")
	public Response createParolee(InputStream is) {
		// Read an XML representation of a new Parolee. Note that with JAX-RS, 
		// any non-annotated parameter in a Resource method is assumed to hold 
		// the HTTP request's message body.
		Parolee parolee = readParolee(is);

		// Generate an ID for the new Parolee, and store it in memory.
		parolee.setId(_idCounter.incrementAndGet());
		_paroleeDB.put(parolee.getId(), parolee);

		_logger.debug("Created parolee with id: " + parolee.getId());

		return Response.created(URI.create("/parolees/" + parolee.getId()))
				.build();
	}

	/**
	 * Attempts to update an existing Parolee. If the specified Parolee is
	 * found it is updated, resulting in a HTTP 204 response being returned to 
	 * the consumer. In other cases, a 404 response is returned.
	 * 
	 * @param id the unique id of the Parolee to update.
	 * 
	 * @param is the InputStream used to store an XML representation of the
	 * new state for the Parolee.
	 */
	@PUT
	@Path("{id}")
	@Consumes("application/xml")
	public void updateParolee(@PathParam("id") long id, InputStream is) {
		Parolee update = readParolee(is);
		Parolee current = _paroleeDB.get(id);
		if (current == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		// Update the details of the Parolee to be updated.
		current.setFirstname(update.getFirstname());
		current.setLastname(update.getLastname());
		current.setGender(update.getGender());
		current.setDateOfBirth(update.getDateOfBirth());
	}

	/**
	 * Attempts to delete an existing Parolee. If the specified Parolee isn't 
	 * found, a 404 response is returned to the consumer. In other cases, a 204
	 * response is returned after deleting the Parolee.
	 * 
	 * @param id the unique id of the Parolee to delete.
	 */
	@DELETE
	@Path("{id}")
	public void deleteParolee(@PathParam("id") long id) {
		Parolee current = _paroleeDB.get(id);
		if (current == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		// Remove the Parolee.
		_paroleeDB.remove(id);
		_logger.info("Deleted parolee with ID: " + id);
	}
	
	/**
	 * Deletes all Parolees. A 204 response is returned to the consumer.
	 */
	@DELETE
	public void deleteAllParolees() {
		_paroleeDB.clear();
		_idCounter = new AtomicLong();
	}
	

	/**
	 * Helper method to generate an XML representation of a particular Parolee.
	 * 
	 * @param os the OutputStream used to write out the XML.
	 * 
	 * @param parolee the Parolee for which to generate an XML representation.
	 * 
	 * @throws IOException if an error is encountered in writing the XML to the 
	 * OutputStream.
	 */
	protected void outputParolee(OutputStream os, Parolee parolee)
			throws IOException {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
		String dateOfBirth = formatter.print(parolee.getDateOfBirth());

		PrintStream writer = new PrintStream(os);
		writer.println("<parolee id=\"" + parolee.getId() + "\">");
		writer.println("   <first-name>" + parolee.getFirstname()
				+ "</first-name>");
		writer.println("   <last-name>" + parolee.getLastname()
				+ "</last-name>");
		writer.println("   <gender>" + parolee.getGender() + "</gender>");
		writer.println("   <date-of-birth>" + dateOfBirth + "</date-of-birth>");
		writer.println("</parolee>");
	}

	/**
	 * Helper method to generate an XML representation for a collection of 
	 * Parolees.
	 */
	protected void outputParolees(OutputStream os, List<Parolee> parolees)
			throws IOException {
		for (Parolee parolee : parolees) {
			outputParolee(os, parolee);
		}
	}

	/**
	 * Helper method to read an XML representation of a Parolee, and return a
	 * corresponding Parolee object. 
	 * 
	 * @param is the InputStream containing an XML representation of the 
	 *        Parolee to create.
	 *        
	 * @return a new Parolee object.
	 */
	protected Parolee readParolee(InputStream is) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(is);
			Element root = doc.getDocumentElement();

			Parolee parolee = new Parolee();
			if (root.getAttribute("id") != null
					&& !root.getAttribute("id").trim().equals(""))
				parolee.setId(Long.valueOf(root.getAttribute("id")));
			NodeList nodes = root.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
				if (element.getTagName().equals("first-name")) {
					parolee.setFirstname(element.getTextContent());
				} else if (element.getTagName().equals("last-name")) {
					parolee.setLastname(element.getTextContent());
				} else if (element.getTagName().equals("gender")) {
					parolee.setGender(Gender.fromString(element
							.getTextContent()));
				} else if (element.getTagName().equals("date-of-birth")) {
					DateTimeFormatter formatter = DateTimeFormat
							.forPattern("dd/MM/yyyy");
					DateTime dateOfBirth = formatter.parseDateTime(element
							.getTextContent());
					parolee.setDateOfBirth(dateOfBirth);
				}
			}
			return parolee;
		} catch (Exception e) {
			throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
		}
	}

}
