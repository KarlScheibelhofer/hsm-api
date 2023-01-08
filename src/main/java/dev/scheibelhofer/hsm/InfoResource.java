package dev.scheibelhofer.hsm;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.logging.Log;

@Path("/info")
public class InfoResource {

     @GET
     @Path("/key-algorithms")
     @Produces(MediaType.TEXT_PLAIN)
     public Response getKeyAlgorithms() {
         Log.info("get key algorithms");
         String algorithmListStr = Stream.of(KeyAlgorithm.values()).map(Enum::name).sorted().collect(Collectors.joining(" "));
         return Response.ok(algorithmListStr).build();
     }
}