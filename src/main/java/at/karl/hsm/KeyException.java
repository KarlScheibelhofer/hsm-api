package at.karl.hsm;

import javax.ws.rs.core.Response;

public class KeyException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private Response.Status status;

  public KeyException(Response.Status status, String msg, Throwable cause) {
    super(msg, cause);
    this.status = status;
  }

  public Response.Status getStatus() {
    return status;
  }

}
