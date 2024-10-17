package br.univesp.sensores.dto.responses;

import java.time.LocalDateTime;
import java.util.List;

import br.univesp.sensores.helpers.DaoHelper.Page;

public record AlertasEnviadosListaResp(
		Page page,
		List<LocalDateTime> envios
		) {

}
