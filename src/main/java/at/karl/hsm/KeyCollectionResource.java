package at.karl.hsm;

import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/keys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KeyCollectionResource {

	@Inject
	KeyService service;

    @Inject
    KeyResource keys;

    @Path("/{id}")
    public KeyResource get(@PathParam("id") long id) {
        return keys;
    }

    @GET
    public Collection<Key> list(@QueryParam("name") String name) {
    	if (name == null) {
            return service.getAll();
        }
        return service.getByName(name);
    }

    @POST
    public Response create(Key key) {
    	Key k = service.create(key);
    	return Response.status(Response.Status.CREATED).entity(k).build();
     }

}