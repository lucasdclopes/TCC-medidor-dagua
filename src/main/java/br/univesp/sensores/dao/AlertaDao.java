package br.univesp.sensores.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.univesp.sensores.dto.queryparams.DtParams;
import br.univesp.sensores.dto.queryparams.PaginacaoQueryParams;
import br.univesp.sensores.dto.responses.AlertaItemResp;
import br.univesp.sensores.dto.responses.AlertaListaResp;
import br.univesp.sensores.dto.responses.AlertasEnviadosListaResp;
import br.univesp.sensores.entidades.Alerta;
import br.univesp.sensores.helpers.DaoHelper;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@Stateless
public class AlertaDao {
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * Salva a entidade e retorna o ID auto gerado
	 * @param sensor
	 * @return id gerado no banco de dados
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public Long salvar(Alerta alerta) {
		em.persist(alerta);
		em.flush();
		return alerta.getIdAlerta();
	}
	
	public Alerta atualizar(Alerta alerta) {
		return em.merge(alerta);
	}
	
	public Optional<Alerta> buscarPorId(final Long idAlerta) {
		return Optional.ofNullable(
				em.find(Alerta.class, idAlerta)
				);
	}
	
	public static record FiltrosAlerta (BigDecimal vlMax, BigDecimal vlMin) {}
	public AlertaListaResp listar(final PaginacaoQueryParams paginacao, final DtParams dtParams, Optional<FiltrosAlerta> filtros) {
		
		String where = "WHERE 1 = 1 ";
		String jpql = """
				select new br.univesp.sensores.dto.responses.AlertaItemResp (
					a.idAlerta,a.isHabilitado,a.intervaloEsperaSegundos,
					a.vlMax,a.vlMin,a.dtCriado,a.dtUltimoEnvio,a.destinatarios,a.habilitarDispositivo
				) from Alerta a 
				""";
		final String orderBy = " order by a.dtCriado desc ";
		Map<String,Object> params = new HashMap<>();
		
		where += DaoHelper.addWhereRangeData(params, dtParams, "a.dtCriado");
		
		if (filtros.isPresent()) {
			FiltrosAlerta filtro = filtros.get();
			
			if (filtro.vlMin != null) {
				where += " AND vlMin = :vlMin ";
				params.put("vlMin", filtro.vlMin);
			}
			
			if (filtro.vlMax != null) {
				where += " AND vlMax = :vlMax ";
				params.put("vlMax", filtro.vlMax);
			}
		}
		
		jpql += where + orderBy;
		
		TypedQuery<AlertaItemResp> query = em.createQuery(jpql, AlertaItemResp.class);
		params.forEach(query::setParameter);
		
		String jpqlCount = """
				select count(a.idAlerta) from Alerta a 
				""" + where;
			
		TypedQuery<Long> queryCount = em.createQuery(jpqlCount, Long.class);
		params.forEach(queryCount::setParameter);
		Long total = queryCount.getSingleResult();
		
		List<AlertaItemResp> resultList = paginacao.configurarPaginacao(query).getResultList();
		return new AlertaListaResp(
				DaoHelper.infoPaginas(paginacao, total, resultList.size()),
				resultList
				);
				
	}
	
	public List<Alerta> buscarAlertasValidos(){
		String jpql = """
				select a from Alerta a
				left join fetch a.alertasEnviados 
				WHERE a.isHabilitado = true
				order by a.dtCriado desc
				""";
		
		return em.createQuery(jpql, Alerta.class).getResultList();
	}
	/*
	public Optional<Alerta> buscarAlertaComDispositivo(){
		String jpql = """
				select top 1 a from Alerta a
				WHERE 
				a.isHabilitado = true
				and
				a.tipoAlerta = """
				+ TipoAlerta.TEMPERATURA.getCodigo() + " " +  
				"""
				order by a.vlMax asc
				""";
		try {
			return Optional.of(
					em.createQuery(jpql, Alerta.class).getSingleResult()
					);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}*/
	
	public AlertasEnviadosListaResp listarEnviados(final Long idAlerta, final PaginacaoQueryParams paginacao){
		
		String jpql = """
				select e.dtEnvio from AlertaEnviado e 
				where e.alerta.idAlerta = :idAlerta
				order by e.dtEnvio desc
				""";
		
		String jpqlCount = """
				select count(e.idEnviado) from AlertaEnviado e 
				where e.alerta.idAlerta = :idAlerta 
				""";
			
		Long total = em.createQuery(jpqlCount, Long.class).setParameter("idAlerta", idAlerta).getSingleResult();
		
		List<LocalDateTime> resultList = paginacao.configurarPaginacao(
				em.createQuery(jpql, LocalDateTime.class))
				.setParameter("idAlerta", idAlerta)
				.getResultList();
		
		return new AlertasEnviadosListaResp(
				DaoHelper.infoPaginas(paginacao, total, resultList.size()),
				resultList
				);
	}
	
	@Transactional(value = TxType.REQUIRED)
	public void deletarEnviados(final Long idAlerta,final DtParams dtParams) {
		String jpql = """
				delete from AlertaEnviado ae 
				where ae.alerta.idAlerta = :idAlerta
				""";
		
		Map<String,Object> params = new HashMap<>();
		jpql += DaoHelper.addWhereRangeData(params, dtParams, "dtEnvio");
		params.put("idAlerta", idAlerta);
		
		Query query = em.createQuery(jpql);
		params.forEach(query::setParameter);
		query.executeUpdate();
	
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public void deletarPorId(final Long idAlerta) {
		
		deletarEnviados(idAlerta,null);		//deleta os relacionamentos
		em.flush();
		
		String jpql = """
				delete from Alerta a 
				where a.idAlerta = :idAlerta
				""";
		
		em.createQuery(jpql)
		.setParameter("idAlerta", idAlerta)
		.executeUpdate();
		
	}
	
}
