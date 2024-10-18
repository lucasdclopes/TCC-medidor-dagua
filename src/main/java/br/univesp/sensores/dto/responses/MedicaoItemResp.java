package br.univesp.sensores.dto.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MedicaoItemResp(
		Long idMedicao,
		BigDecimal vlDistancia,
		LocalDateTime dtMedicao
		) {
	
	public MedicaoItemResp(Long idMedicao, BigDecimal vlDistancia, LocalDateTime dtMedicao, Integer profundidade) {
		this (idMedicao, normalizarComProfundidade(vlDistancia,profundidade), dtMedicao);
	}
	
	private static BigDecimal normalizarComProfundidade(BigDecimal vlDistancia, Integer profundidade) {
		vlDistancia = new BigDecimal(profundidade).subtract(vlDistancia);
		return (vlDistancia.compareTo(BigDecimal.ZERO) < 0)? BigDecimal.ZERO : vlDistancia; //se for menor do que zero, usa zero.
	}

}
