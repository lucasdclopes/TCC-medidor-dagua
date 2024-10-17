package br.univesp.sensores.dto.responses;

import java.util.List;

import br.univesp.sensores.helpers.DaoHelper.Page;

public record LogErroListaResp(
		Page page,
		List<LogErroItemResp> logs
		) {

}
