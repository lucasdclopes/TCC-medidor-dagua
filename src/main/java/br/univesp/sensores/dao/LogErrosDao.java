package br.univesp.sensores.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.univesp.sensores.dto.queryparams.DtParams;
import br.univesp.sensores.dto.queryparams.PaginacaoQueryParams;
import br.univesp.sensores.dto.responses.LogErroItemResp;
import br.univesp.sensores.dto.responses.LogErroListaResp;
import br.univesp.sensores.entidades.LogErrosSistema;
import br.univesp.sensores.helpers.DaoHelper;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@Stateless
public class LogErrosDao {
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * Salva a entidade e retorna o ID auto gerado
	 * @param sensor
	 * @return id gerado no banco de dados
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public Long salvar(LogErrosSistema log) {
		em.persist(log);
		em.flush();
		return log.getIdLogErros();
	}
	
	public LogErroListaResp listar(final PaginacaoQueryParams paginacao, final DtParams dtParams) {
		String where = "WHERE 1 = 1 ";
		String jpql = """
				select new br.univesp.sensores.dto.responses.LogErroItemResp (
					l.idLogErros,l.msgErro,l.dtLog,l.stacktrace
				) from LogErrosSistema l 
				""";
		final String orderBy = " order by l.dtLog desc ";
		Map<String,Object> params = new HashMap<>();
		
		where += DaoHelper.addWhereRangeData(params, dtParams, "dtCriado");
		jpql += where + orderBy;
		TypedQuery<LogErroItemResp> query = em.createQuery(jpql, LogErroItemResp.class);
		params.forEach(query::setParameter);
		
		String jpqlCount = """
				select count(l.idLogErros) from LogErrosSistema l 
				""" + where;
		
		TypedQuery<Long> queryCount = em.createQuery(jpqlCount, Long.class);
		params.forEach(queryCount::setParameter);
		Long total = queryCount.getSingleResult();
		
		List<LogErroItemResp> resultList = paginacao.configurarPaginacao(query).getResultList();
		return new LogErroListaResp(
				DaoHelper.infoPaginas(paginacao, total, resultList.size()),
				resultList
				);
	}
}
