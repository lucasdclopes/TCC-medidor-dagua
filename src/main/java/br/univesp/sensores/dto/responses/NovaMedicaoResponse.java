package br.univesp.sensores.dto.responses;

import java.math.BigDecimal;

public record NovaMedicaoResponse(
		Integer intervalo,
		BigDecimal tempAcionamento) {}
