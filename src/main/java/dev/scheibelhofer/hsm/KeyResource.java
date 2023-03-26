package dev.scheibelhofer.hsm;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.logging.Log;

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KeyResource {

    @Inject
    KeyService keyService;

    @GET
    public Response get(@PathParam("id") long id) {
        Log.info("get key " + id);
        Key key = keyService.getById(id);
        if (key == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(key).build();
    }

    @POST
    @Path("/sign")
    public Response signJson(@PathParam("id") long id, byte[] data) {
        Log.info("sign JSON with key " + id);
        Key key = keyService.getById(id);
        if (key == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Signature s = keyService.signData(id, data);
        return Response.ok(s).build();
    }

    @POST
    @Path("/sign")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response signBinaryData(@PathParam("id") long id, byte[] data) {
        Log.info("sign binary with key " + id);
        Key key = keyService.getById(id);
        if (key == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Signature s = keyService.signData(id, data);
        return Response.ok(s).build();
    }

    @DELETE
    public Response delete(@PathParam("id") long id) {
        Log.info("delete key " + id);
        if (keyService.delete(id) == false) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

}