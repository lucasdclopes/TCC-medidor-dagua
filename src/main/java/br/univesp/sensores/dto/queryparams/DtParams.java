package br.univesp.sensores.dto.queryparams;

import java.time.LocalDateTime;

import br.univesp.sensores.erros.ErroNegocioException;
import jakarta.ws.rs.QueryParam;

public class DtParams {
	
	private @QueryParam("dtInicial") LocalDateTime dtInicial;
	private @QueryParam("dtFinal") LocalDateTime dtFinal;
	
	public DtParams() {}
	
	public DtParams(LocalDateTime dtInicial, LocalDateTime dtFinal) {
		this.dtInicial = dtInicial;
		this.dtFinal = dtFinal;
	}


	public LocalDateTime getDtInicial() {
		return dtInicial;
	}
	public LocalDateTime getDtFinal() {
		return dtFinal;
	}
	
	public void validar() {
		if (dtInicial != null && dtFinal != null && dtInicial.isAfter(dtFinal))
			throw new ErroNegocioException("A data final de busca não pode ser anterior à data inicial");
			
	}
}
