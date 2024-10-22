package br.univesp.sensores.dto.requests;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NovoAlerta(
		@NotNull(message = "Frequência máxima do alerta não pode ser vazia")
		@Min(value = 240, message = "A frequência máxima do alerta não pode ser menor do que 4 minutos (240 segundos)")
		Integer intervaloEsperaSegundos,
		BigDecimal vlMax,
		BigDecimal vlMin,
		@NotBlank(message = "É preciso informar pelo menos um e-mail para o envio de alertas")
		String destinatarios,
		@NotNull(message = "É preciso informar se o dispositivo de alerta será habilitado")
		Boolean habilitarDispositivo
		) {

}
