package br.univesp.sensores.dto.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.univesp.sensores.entidades.MedicaoSensor;

public record MedicaoItemResp(
		Long idMedicao,
		BigDecimal vlDistancia,
		LocalDateTime dtMedicao
		) {
	
	public MedicaoItemResp(Long idMedicao, BigDecimal vlDistancia, LocalDateTime dtMedicao, Integer profundidade) {
		this (idMedicao, MedicaoSensor.normalizarComProfundidade(vlDistancia,profundidade), dtMedicao);
	}

}
