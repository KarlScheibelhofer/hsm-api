package at.karl.hsm;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class KeyExceptionMapper implements ExceptionMapper<KeyException> {

    @Override
    public Response toResponse(KeyException ex) {
        return Response.status(ex.getStatus()).entity(toResponseBody(ex)).build();
    }

    Object toResponseBody(KeyException ex) {
        Map<String,String> m = new HashMap<>();
        m.put("message", ex.getMessage());
        return m;
    }

}
