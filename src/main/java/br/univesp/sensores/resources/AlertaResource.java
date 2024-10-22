package br.univesp.sensores.resources;

import java.util.List;
import java.util.Optional;

import br.univesp.sensores.dao.AlertaDao;
import br.univesp.sensores.dao.AlertaDao.FiltrosAlerta;
import br.univesp.sensores.dto.queryparams.DtParams;
import br.univesp.sensores.dto.queryparams.PaginacaoQueryParams;
import br.univesp.sensores.dto.requests.AtualizarAlerta;
import br.univesp.sensores.dto.requests.NovoAlerta;
import br.univesp.sensores.dto.responses.AlertaItemResp;
import br.univesp.sensores.dto.responses.AlertaListaResp;
import br.univesp.sensores.dto.responses.AlertasEnviadosListaResp;
import br.univesp.sensores.entidades.Alerta;
import br.univesp.sensores.erros.ErroNegocioException;
import br.univesp.sensores.helpers.ConfigHelper;
import br.univesp.sensores.helpers.ConfigHelper.Chave;
import br.univesp.sensores.helpers.ResourceHelper;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

@Path("/alerta")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertaResource {
	
	@Inject AlertaDao alertaDao;
	
	@GET
	public Response getAlertas(@Valid @BeanParam final PaginacaoQueryParams paginacao, @Valid @BeanParam final DtParams dtParams ) {
		
		AlertaListaResp db = alertaDao.listar(paginacao,dtParams,Optional.empty());
		if (db.alerta().isEmpty())
			return Response.status(Status.NO_CONTENT).build();
		
		return Response.ok().entity(db.alerta())
				.header("page-quantidade", db.page().pageQuantidade())
				.header("page-has-proxima", db.page().hasProxima())
				.build();
	}
	
	@GET
	@Path("/{idAlerta}")
	public Response getAlertasEnviados(@PathParam("idAlerta") final Long idAlerta, 
			@Valid @BeanParam final PaginacaoQueryParams paginacao, @Valid @BeanParam final DtParams dtParams ) {
		
		AlertasEnviadosListaResp db = alertaDao.listarEnviados(idAlerta,paginacao);
		if (db.envios().isEmpty())
			return Response.status(Status.NO_CONTENT).build();
		
		return Response.ok().entity(db.envios())
				.header("page-quantidade", db.page().pageQuantidade())
				.header("page-has-proxima", db.page().hasProxima())
				.build();
	}
	
	@POST
	public Response salvarNovoAlerta(@Valid final NovoAlerta novoAlerta, @Context UriInfo uriInfo) {
		
		Integer limite = ConfigHelper.getInstance().getConfigInteger(Chave.ALERTA_LIMITE_TOTAL);
		AlertaListaResp listar = alertaDao.listar(new PaginacaoQueryParams(100, 1), null,Optional.empty());
		if (listar.page().totalRegistros() > limite)
			throw new ErroNegocioException("Não é possível criar mais do que " + limite + " alertas. Exclua um alerta existente para poder criar um novo");

		List<AlertaItemResp> existente = alertaDao.listar(
				new PaginacaoQueryParams(100, 1), null,Optional.of(new FiltrosAlerta(novoAlerta.vlMax(), novoAlerta.vlMin()))
				).alerta();
		if (!existente.isEmpty())
			throw new ErroNegocioException("O alerta #" + existente.get(0).idAlerta() + " já possuí os mesmos parâmetros de monitoramento");
		
		Alerta alerta = new Alerta(novoAlerta.intervaloEsperaSegundos(), novoAlerta.vlMax(), novoAlerta.vlMin(),novoAlerta.destinatarios(), novoAlerta.habilitarDispositivo()
				);
		
		Long id = alertaDao.salvar(alerta);
		return Response
				.created(ResourceHelper.montarLocation(uriInfo,id))
				.build();	
	}
	
	/*
	 * Só permito alterar os destinatários, o intervalo limite e liga/desliga. Para não bagunçar o histórico.
	 * Se precisar mudar os parâmetros, crie outro alerta
	 */
	@PUT
	@Path("/{idAlerta}")
	public Response atualizarAlerta(@PathParam("idAlerta") final Long idAlerta, @Valid final AtualizarAlerta atualizar) {

		Alerta alerta = alertaDao.buscarPorId(idAlerta)
				.orElseThrow(() ->  new ErroNegocioException("O alerta especificado não foi encontrado"));
		
		if (atualizar.isHabilitado() != null) {
			if (atualizar.isHabilitado())
				alerta.habilitar();
			else
				alerta.desabilitar();
		}
		
		if (atualizar.habilitarDispositivo() != null) {
			if (atualizar.habilitarDispositivo())
				alerta.habilitarDispositivo();
			else
				alerta.desabilitarDispositivo();
		}
		
		if (atualizar.intervaloEsperaSegundos() != null) 
			alerta.alterarIntervalo(atualizar.intervaloEsperaSegundos());
		
		if (atualizar.destinatarios() != null)
			alerta.alterarDestinatarios(atualizar.destinatarios());
		
		if (atualizar.vlMax() != null)
			alerta.setVlMax(atualizar.vlMax());
		
		alertaDao.atualizar(alerta);
		
		return Response.status(Status.NO_CONTENT).build();
		
	}
	
	@DELETE
	@Path("/{idAlerta}")
	public Response removerAlerta(@PathParam("idAlerta") final Long idAlerta) {
		alertaDao.deletarPorId(idAlerta);
		return Response.status(Status.NO_CONTENT).build();
	}
}
