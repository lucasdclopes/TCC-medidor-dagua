package br.univesp.sensores.dto.responses;

import java.time.LocalDateTime;

import br.univesp.sensores.scheduler.SchedulerTemplate.CodigosStatusMonitor;

public record MonitorStatusResp(
		CodigosStatusMonitor cdStatusMonitor,
		LocalDateTime dtInicializado,
		Long frequenciaSegundos,
		String msgErro,
		Long tempoAteProximaExecucaoSegundos) {

}
