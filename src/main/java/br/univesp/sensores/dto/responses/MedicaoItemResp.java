package br.univesp.sensores.dto.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MedicaoItemResp(
		Long idMedicao,
		BigDecimal vlDistancia,
		LocalDateTime dtMedicao
		) {

}
