package br.univesp.sensores.dto.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertaItemResp(
		Long idAlerta,
		Boolean isHabilitado,
		Integer intervaloEsperaSegundos,
		BigDecimal vlMax,
		BigDecimal vlMin,
		LocalDateTime dtCriado,
		LocalDateTime dtUltimoEnvio,
		String destinatarios,
		Boolean habilitarDispositivo
		) {

}
