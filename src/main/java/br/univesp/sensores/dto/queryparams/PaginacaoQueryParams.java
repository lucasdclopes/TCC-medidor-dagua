package br.univesp.sensores.dto.queryparams;

import br.univesp.sensores.helpers.ConfigHelper;
import br.univesp.sensores.helpers.ConfigHelper.Chave;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.QueryParam;

public class PaginacaoQueryParams {
	
	private final Integer MAX_ITENS = ConfigHelper.getInstance()
			.getConfigInteger(Chave.PAGINACAO_MAX_ITENS);
	private Integer maxItensAtual = MAX_ITENS;
	
	@QueryParam("size") 
	protected Integer nroLinhas;
	@QueryParam("page") 
	protected Integer nroPagina;
	
	public PaginacaoQueryParams() {}
	
	public PaginacaoQueryParams(Integer nroLinhas, Integer nroPagina) {
		this.nroLinhas = nroLinhas;
		this.nroPagina = nroPagina;
	}
	public void overrideMaxItens (Integer maxItens) {
		this.maxItensAtual = maxItens;
	}
	public Integer getNroLinhas(){
		if (nroLinhas == null || nroLinhas < 1)
			nroLinhas = 20;
		if (nroLinhas > maxItensAtual)
			nroLinhas = maxItensAtual;
		return nroLinhas;
	}
	/**
	 * Valida o número da página Se for null ou 0, retorna o padrão definido na propriedade padraoNroPagina
	 * @return valor do número da página
	 */
	public Integer getNroPagina(){
		if (nroPagina == null || nroPagina < 1)
			this.nroPagina = 1;
		return this.nroPagina;
	}
	
	/**
	 * Auxiliar a paginação na JPA.
	 */
	public <T> TypedQuery<T> configurarPaginacao(TypedQuery<T> typedQuery) {
		return typedQuery
		.setFirstResult(this.getNroLinhas() * (this.getNroPagina() - 1))
		.setMaxResults(nroLinhas);
	}
}
