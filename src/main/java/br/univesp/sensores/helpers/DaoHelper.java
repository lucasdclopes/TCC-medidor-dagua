package br.univesp.sensores.helpers;

import java.util.Map;

import br.univesp.sensores.dto.queryparams.DtParams;
import br.univesp.sensores.dto.queryparams.PaginacaoQueryParams;

public class DaoHelper {

	/**
	 * Monta os parâmetros de WHERE para querys que tem range de busca por data
	 * @param params objeto com os parâmetros
	 * @param dtParams parâmetros de data
	 * @param nomeCampo nome do campo de data da entidade
	 * @return
	 */
	public static String addWhereRangeData(
			Map<String,Object> params, final DtParams dtParams, final String nomeCampo) {

		String paramsWhere = "";
		if (dtParams != null) {
			
			dtParams.validar(); //verifica se são datas válidas
			
			if (dtParams.getDtInicial() != null) {
				paramsWhere += " AND " + nomeCampo + " >= :dtInicial ";
				params.put("dtInicial", dtParams.getDtInicial());
			}
			
			if (dtParams.getDtFinal() != null) {
				paramsWhere += " AND " + nomeCampo + " <= :dtFinal ";
				params.put("dtFinal", dtParams.getDtFinal());
			}	
		}
		return paramsWhere;
	}
	
	public record Page (Long totalRegistros, Long pageQuantidade,Boolean hasProxima) {}
	public static Page infoPaginas(PaginacaoQueryParams paginacao,Long totalRegistros,Integer registrosNaPagina) {
		
		Long totalPaginas = totalRegistros / paginacao.getNroLinhas() + (totalRegistros % paginacao.getNroLinhas() == 0 ? 0 : 1);
		
		if (registrosNaPagina < paginacao.getNroLinhas())
			return new Page(totalRegistros,totalPaginas, false);

		return new Page(
				totalRegistros,
				totalPaginas,
				(paginacao.getNroLinhas() * paginacao.getNroPagina() < totalRegistros)
				);
	}
}
