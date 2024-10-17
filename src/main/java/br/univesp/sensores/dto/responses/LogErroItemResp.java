package br.univesp.sensores.dto.responses;

import java.time.LocalDateTime;

public record LogErroItemResp(
		Long idLogErros,
		String msgErro,
		LocalDateTime dtLog,
		String stacktrace
		) {

}
