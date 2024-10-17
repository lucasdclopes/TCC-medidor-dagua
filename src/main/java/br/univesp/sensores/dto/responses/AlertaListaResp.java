package br.univesp.sensores.dto.responses;

import java.util.List;

import br.univesp.sensores.helpers.DaoHelper.Page;

public record AlertaListaResp(
		Page page,
		List<AlertaItemResp> alerta
		) {

}
