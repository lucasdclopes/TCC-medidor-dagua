package br.univesp.sensores.dto.responses;

import java.util.List;

import br.univesp.sensores.helpers.DaoHelper.Page;

public record MedicaoListaResp(
		Page page,
		List<MedicaoItemResp> medicoes
		) {

}
