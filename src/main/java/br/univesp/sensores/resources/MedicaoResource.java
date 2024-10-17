package br.univesp.sensores.resources;

import java.math.BigDecimal;
import java.util.Comparator;

import br.univesp.sensores.dao.AlertaDao;
import br.univesp.sensores.dao.MedicaoDao;
import br.univesp.sensores.dao.MedicaoDao.TipoAgrupamento;
import br.univesp.sensores.dao.UserConfigDao;
import br.univesp.sensores.dto.queryparams.DtParams;
import br.univesp.sensores.dto.queryparams.PaginacaoQueryParams;
import br.univesp.sensores.dto.requests.NovaMedicao;
import br.univesp.sensores.dto.responses.MedicaoListaResp;
import br.univesp.sensores.dto.responses.NovaMedicaoResponse;
import br.univesp.sensores.entidades.Alerta;
import br.univesp.sensores.entidades.Alerta.TipoAlerta;
import br.univesp.sensores.entidades.MedicaoSensor;
import br.univesp.sensores.helpers.ConfigHelper;
import br.univesp.sensores.helpers.ConfigHelper.Chave_User;
import br.univesp.sensores.helpers.ResourceHelper;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

@Path("/medicao")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MedicaoResource {
	
	@Inject MedicaoDao medicaoDao;
	@Inject AlertaDao alertaDao;
	@Inject UserConfigDao userConfDao;
	
	
	
	@GET
	public Response getSensores(@Valid @BeanParam final PaginacaoQueryParams paginacao, @Valid @BeanParam final DtParams dtParams, 
			@Valid @QueryParam("tempoReal") final boolean tempoReal, @Valid @QueryParam("tipoAgrupamento") final Integer tipoAgrupamento) {
		
		if (!tempoReal) 
			paginacao.overrideMaxItens(1000);
		
		MedicaoListaResp medicoes = null;
		if (tipoAgrupamento != null) 
			medicoes = medicaoDao.listarAgrupado(paginacao, dtParams, TipoAgrupamento.toAgrupamento(tipoAgrupamento));
		else 
			medicoes = medicaoDao.listar(paginacao,dtParams,tempoReal);
		
		if (medicoes.medicoes().isEmpty())
			return Response.status(Status.NO_CONTENT).build();
		
		return Response.ok().entity(medicoes.medicoes())
				.header("page-quantidade", medicoes.page().pageQuantidade())
				.header("page-has-proxima", medicoes.page().hasProxima())
				.build();
	}
	
	@POST
	public Response salvarMedicao(final NovaMedicao novaMedicao, @Context UriInfo uriInfo) {
		
		MedicaoSensor med = new MedicaoSensor(novaMedicao.vlDistancia());
		Long id = medicaoDao.salvarMedicao(med);
				
		/*mostra o intervalo de tempo que o dispositivo vai esperar até a próxima execução
		e carrega o alerta que define com que temperatura o dispositivo será acionado
		*/
		NovaMedicaoResponse response = new NovaMedicaoResponse(
				ConfigHelper.getInstance().getConfigInteger(Chave_User.MONITORAMENTO_INTERVALO_MS,userConfDao), 
				alertaDao.buscarAlertasValidos().stream()
				.filter(a -> a.deveHabilitarDispositivo() && a.getTipoAlerta() == TipoAlerta.DISTANCIA)
				.sorted(Comparator.comparing(Alerta::getVlMax))
				.map(Alerta::getVlMax)
				.findFirst()
				.orElse(BigDecimal.ZERO)
				);
		
		return Response.created(ResourceHelper.montarLocation(uriInfo,id)).entity(response).build();
		
	}


}
