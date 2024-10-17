package br.univesp.sensores.server;

import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public class OptionCors {
	@OPTIONS
	@Path("{path:.*}")
	public Response handleCORSRequest(@PathParam("path") final String path) {
		Response.ResponseBuilder builder = Response.ok();
		return builder.build();
	}
}