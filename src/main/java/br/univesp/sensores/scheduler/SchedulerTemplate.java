package br.univesp.sensores.scheduler;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import br.univesp.sensores.dao.LogErrosDao;
import br.univesp.sensores.dto.responses.MonitorStatusResp;
import br.univesp.sensores.entidades.LogErrosSistema;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.inject.Inject;

public abstract class SchedulerTemplate {
	private static final Logger LOGGER = Logger.getLogger( SchedulerTemplate.class.getName());
	
	@Inject private LogErrosDao dbLog;
	
	public static enum CodigosStatusMonitor {
		INICIADO("Monitor iniciado"),
		INICIANDO("Monitor inicializando"),
		PARANDO("Monitor parando"),
		PARADO("Monitor parado");
		
		private String status;

		CodigosStatusMonitor(String status) {
			this.status = status;
		}
		
		public String getDescricaoStatus() {
			return this.status;
		}
	}

	
	@Resource
	private ManagedScheduledExecutorService execucaoAgendada;
	private ScheduledFuture<?> proximaTarefa;
	
	private CodigosStatusMonitor status;
	private LocalDateTime dtIniciado;
	private Long intervalo;
		
	protected abstract void executarTarefaAgendada();//NÃO PODE JOGAR EXCEÇÃO
	protected abstract long getIntervaloExecucaoSegundos() throws Exception;
	
	protected void gravarLogScheduler(LocalDateTime inicioExecucao, Exception erro) {
		try { 
			dbLog.salvar(
					new LogErrosSistema(inicioExecucao, erro)
					);
		} catch (Exception e) {
			LOGGER.fatal("Não foi possível gravar um erro na tarefa agendada no banco de dados",e);
		}
	}

	@Lock(LockType.READ)
	public MonitorStatusResp getStatusMonitor() {
		return new MonitorStatusResp(status, dtIniciado, intervalo, null, 
				proximaTarefa != null ? proximaTarefa.getDelay(TimeUnit.SECONDS):null
				);
	}
	
	@Lock(LockType.WRITE)
	public MonitorStatusResp inicializarIntervaloFixo() {
		
		LocalDateTime inicioExecucao = LocalDateTime.now();
		LOGGER.log(Level.INFO, "Iniciando monitor");
		if (status == CodigosStatusMonitor.PARADO) {
			status = CodigosStatusMonitor.INICIANDO;
			try {
				this.intervalo = getIntervaloExecucaoSegundos();
				this.proximaTarefa = execucaoAgendada.scheduleAtFixedRate(
						() -> {this.executarTarefaAgendada();}, 
						intervalo, 
						intervalo, 
						TimeUnit.SECONDS);
				this.dtIniciado = LocalDateTime.now();
				this.status = CodigosStatusMonitor.INICIADO;
				return this.getStatusMonitor();
			} catch (Exception e) {
				gravarLogScheduler(inicioExecucao,e);
				LOGGER.log(Level.FATAL, "Erro iniciando monitor", e);
				parar();
				return new MonitorStatusResp(status, dtIniciado, intervalo, e.getMessage(),null);
			}
		}
		return this.getStatusMonitor();
	}
	
	@Lock(LockType.WRITE)
	public void parar() {
		if (status != CodigosStatusMonitor.PARADO) {
			status = CodigosStatusMonitor.PARANDO;
			if (proximaTarefa != null)
				proximaTarefa.cancel(false);
			dtIniciado = null;
			status = CodigosStatusMonitor.PARADO;
		}
	}
	
	
	@PostConstruct
	protected void init() {
		status = CodigosStatusMonitor.PARADO;
		inicializarIntervaloFixo();
	}
	
	@PreDestroy
	protected void preDestroy() {
		parar();
	}
}
