package br.univesp.sensores.resources;

import br.univesp.sensores.scheduler.SchedulerAlerta;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/scheduler")
@Produces(MediaType.APPLICATION_JSON)
public class SchedulerResource {

	@Inject
	private SchedulerAlerta scheduler;

	@POST
	@Path("/iniciar")
	public Response iniciar() {
		return Response.ok().entity(scheduler.inicializarIntervaloFixo()).build();
	}
	
	@POST
	@Path("/parar")
	public Response parar() {
		scheduler.parar();
		return Response.ok().entity(scheduler.getStatusMonitor()).build();
	}
	
	@GET
	public Response status() {
		return Response.ok().entity(scheduler.getStatusMonitor()).build();
	}
	
}
