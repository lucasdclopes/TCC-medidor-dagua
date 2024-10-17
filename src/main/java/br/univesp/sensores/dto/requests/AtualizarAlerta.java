package br.univesp.sensores.dto.requests;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;

public record AtualizarAlerta(
		@Min(value = 240, message = "A frequência máxima do alerta não pode ser menor do que 4 minutos (240 segundos)")
		Integer intervaloEsperaSegundos,
		String destinatarios,
		Boolean isHabilitado,
		Boolean habilitarDispositivo,
		BigDecimal vlMax 
		) {

}
