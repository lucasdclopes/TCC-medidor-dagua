package br.univesp.sensores.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.jboss.logging.Logger;

import br.univesp.sensores.dao.AlertaDao;
import br.univesp.sensores.dao.MedicaoDao;
import br.univesp.sensores.dao.UserConfigDao;
import br.univesp.sensores.dto.queryparams.DtParams;
import br.univesp.sensores.dto.queryparams.PaginacaoQueryParams;
import br.univesp.sensores.dto.responses.MedicaoItemResp;
import br.univesp.sensores.entidades.Alerta;
import br.univesp.sensores.helpers.ConfigHelper;
import br.univesp.sensores.helpers.ConfigHelper.Chave;
import br.univesp.sensores.services.EmailService;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@Singleton //cuidado para utilizar o jakarta.ejb.Singleton. E não o jakarta.inject.Singleton
@Startup
//TODO: Melhoria, se couber no prazo: Criar um cache dos alertas nesta classe, para não precisar buscar no banco todas as vezes
public class SchedulerAlerta extends SchedulerTemplate {

	private static final Logger LOGGER = Logger.getLogger( SchedulerTemplate.class.getName());
	
	private ConfigHelper config = ConfigHelper.getInstance();
	private LocalDateTime medicaoMaisRecente = LocalDateTime.now().minus(60,ChronoUnit.SECONDS); //inicia obtendo os últimos 30 segundos
	
	@Inject private AlertaDao alertaDao;
	@Inject private MedicaoDao medicaoDao;
	@Inject private EmailService email;
	@Inject private UserConfigDao userConfDao;
	
	@Override
	protected void executarTarefaAgendada() {
		if (!config.getConfigBoolean(Chave.SCHEDULER_ALERTA_LIGADO))
			return;
		
		executarAlertas();
	}
	
	@Asynchronous
	@Transactional(value = TxType.REQUIRES_NEW)
	private void executarAlertas() {
		LocalDateTime agora = LocalDateTime.now();
		try {
			List<Alerta> alertas = alertaDao.buscarAlertasValidos();
			if (alertas.isEmpty())
				return;
			
			List<MedicaoItemResp> medicoes = medicaoDao.listar(
					new PaginacaoQueryParams(50, 1), new DtParams(medicaoMaisRecente, null),true
					).medicoes();
			if (medicoes.isEmpty())
				return;
			
			LOGGER.info("executando verificação de alertas");
			medicaoMaisRecente = medicoes.get(0).dtMedicao();
			for (Alerta alerta : alertas) {
				alerta.enviarAlerta(medicoes,email,userConfDao);
				alertaDao.atualizar(alerta);
			}
	
		} catch (Exception e) {
			super.gravarLogScheduler(agora, e);
		}
		
	}

	@Override
	protected long getIntervaloExecucaoSegundos() throws Exception {
		return config.getConfigInteger(Chave.SCHEDULER_ALERTA_INTERVALO);
	}

}
