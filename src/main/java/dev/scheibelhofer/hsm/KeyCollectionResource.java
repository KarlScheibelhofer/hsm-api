package dev.scheibelhofer.hsm;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.logging.Log;

@Path("/keys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KeyCollectionResource {

	@Inject
	KeyService service;

    @Inject
    KeyResource keySubResource;

    @Path("/{id}")
    public KeyResource get(@PathParam("id") long id) {
        Log.debug("address sub resource for key " + id);
        return keySubResource;
    }
    
    @GET
    public Collection<Key> list(@QueryParam("name") String name) {
        if (name == null) {
            Log.info("list all keys");
            return service.getAll();
        }
        Log.info("list keys with name " + name);
        return service.getByName(name);
    }

    @POST
    public Response create(Key key) {
        Log.info("create new key " + key.name);
    	Key k = service.create(key);
    	return Response.status(Response.Status.CREATED).entity(k).build();
     }

     @GET
     @Path("/algorithms")
     @Produces(MediaType.TEXT_PLAIN)
     public Response getKeyAlgorithms() {
         Log.info("get key algorithms");
         String algorithmListStr = Stream.of(KeyAlgorithm.values()).map(Enum::name).sorted().collect(Collectors.joining(" "));
         return Response.ok(algorithmListStr).build();
     }
     
}