package at.karl.hsm;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/keys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KeyResource {

    @Inject
    KeyService keyService;

    @GET
    public Response get(@PathParam("id") long id) {
        Key key = keyService.getById(id);
        if (key == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(key).build();
    }

    @POST
    @Path("/sign")
    public Response sign(@PathParam("id") long id, byte[] data) {
        Key key = keyService.getById(id);
        if (key == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Signature s = keyService.sign(id, data);
        return Response.ok(s).build();
    }

    @DELETE
    public Response delete(@PathParam("id") long id) {
        if (keyService.delete(id) == false) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

}