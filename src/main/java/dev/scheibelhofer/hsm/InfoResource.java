package dev.scheibelhofer.hsm;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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