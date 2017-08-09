package nz.ac.auckland.parolee.services;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS application subclass for the Parolee Web service. This class is
 * discovered by the JAX-RS run-time and is used to obtain a reference to the
 * ParoleeResource object that will process Web service requests.
 * 
 * The base URI for the Parolee Web service is:
 * 
 * http://<host-name>:<port>/services.
 *
 */
@ApplicationPath("/services")
public class ParoleeApplication extends Application {
   private Set<Object> _singletons = new HashSet<Object>();

   public ParoleeApplication()
   {
      _singletons.add(new ParoleeResource());
   }

   @Override
   public Set<Object> getSingletons()
   {
	  // Return a Set containing an instance of ParoleeResource that will be
	  // used to process all incoming requests on Parolee resources.
      return _singletons;
   }
}
