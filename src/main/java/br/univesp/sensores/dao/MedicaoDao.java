package br.univesp.sensores.dao;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.univesp.sensores.dto.queryparams.DtParams;
import br.univesp.sensores.dto.queryparams.PaginacaoQueryParams;
import br.univesp.sensores.dto.responses.MedicaoItemResp;
import br.univesp.sensores.dto.responses.MedicaoListaResp;
import br.univesp.sensores.entidades.MedicaoSensor;
import br.univesp.sensores.helpers.DaoHelper;
import br.univesp.sensores.helpers.EnumHelper;
import br.univesp.sensores.helpers.EnumHelper.IEnumDescritivel;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@Stateless
public class MedicaoDao {
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * Salva a entidade e retorna o ID auto gerado
	 * @param sensor
	 * @return id gerado no banco de dados
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public Long salvarMedicao(MedicaoSensor sensor) {
		em.persist(sensor);
		em.flush();
		return sensor.getIdMedicao();
	}
	
	public MedicaoListaResp listar(final PaginacaoQueryParams paginacao, final DtParams dtParams, boolean tempoReal) {
		
		Long total = 0L;
		String where = "WHERE 1 = 1 ";
		String jpql = """
				select new br.univesp.sensores.dto.responses.MedicaoItemResp (
					m.idMedicao,m.vlDistancia,m.dtMedicao
				) from MedicaoSensor m 
				""";
		final String orderBy = " order by m.dtMedicao desc ";
		Map<String,Object> params = new HashMap<>();
		
		where += DaoHelper.addWhereRangeData(params, dtParams, "dtMedicao");
		
		jpql += where + orderBy;
		TypedQuery<MedicaoItemResp> query = em.createQuery(jpql, MedicaoItemResp.class);
		params.forEach(query::setParameter);
		
		if (!tempoReal) { //monitoramento de tempo real não deve utilizar esta informação, além de custar muito desempenho	
			String jpqlCount = """
				select count(m.idMedicao) from MedicaoSensor m 
				""" + where;
			
			TypedQuery<Long> queryCount = em.createQuery(jpqlCount, Long.class);
			params.forEach(queryCount::setParameter);
			total = queryCount.getSingleResult();
		}
		List<MedicaoItemResp> resultList = paginacao.configurarPaginacao(query).getResultList();
		return new MedicaoListaResp(
				DaoHelper.infoPaginas(paginacao, total, resultList.size()),
				resultList
				);
				
	}
	
	public enum TipoAgrupamento implements IEnumDescritivel {
		MINUTO(1),
		HORA(2),
		DIA(3),
		SEMANA(4);
		private Integer codigo;
		TipoAgrupamento(Integer codigo){
			this.codigo = codigo;
		}
		@Override
		public Integer getCodigo() {
			return this.codigo;		
		}
		
		@Override
		public String getDescricao() {
			return "Tipo de agrupamento";
		}
		
		public static TipoAgrupamento toAgrupamento(Integer codigo) {
			return EnumHelper.getEnumFromCodigo(codigo,TipoAgrupamento.class);
		}
	}
	
	public MedicaoListaResp listarAgrupado(final PaginacaoQueryParams paginacao, final DtParams dtParams, TipoAgrupamento tipoAgrupamento) {
		

		String where = "WHERE 1 =1 ";
		String groupBy = """
			group by 
			  datepart(year, m.dtMedicao)
			  ,datepart(month, m.dtMedicao) 
				""";
		
		//acumula em cascata, não é pra ter o comando de BREAK mesmo!
		switch (tipoAgrupamento) {
			case MINUTO: groupBy+= " ,datepart(minute, m.dtMedicao) ";
			case HORA: groupBy+= " ,datepart(hour, m.dtMedicao) ";
			case DIA: groupBy+= " ,datepart(day, m.dtMedicao) ";
			case SEMANA: groupBy+= " ,datepart(week, m.dtMedicao) ";
				break;
			default:
				throw new RuntimeException("Tipo de agrupamento sem definição de execução");
		};
		
		String sql = """
			select 
				ROW_NUMBER() over (order by max(m.dtMedicao) desc) as id
				,cast(avg(m.vlDistancia) as decimal(7,2)),max(m.dtMedicao) as dtMed
			 from medicao_sensor m 
				%s
			order by dtMed desc
			OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
				""";
		
		List<Object> params = new ArrayList<>();
		
		if (dtParams != null) {
			dtParams.validar();
			
			if (dtParams.getDtInicial() != null) {
				where += " AND m.dtMedicao >= ? ";
				params.add(dtParams.getDtInicial());
			}
			
			if (dtParams.getDtFinal() != null) {
				where += " AND m.dtMedicao <= ? ";
				params.add(dtParams.getDtFinal());
			}

		}
		
		Query q = em.createNativeQuery(sql.formatted(where + groupBy));
		Integer iParams = 1;
		for (Object param : params) {
			q.setParameter(iParams++, param);
		}
		q.setParameter(iParams++, (paginacao.getNroPagina()-1)*paginacao.getNroLinhas());
		q.setParameter(iParams++, paginacao.getNroLinhas());
	
		@SuppressWarnings("unchecked") //é do próprio framework..
		List<Object[]> rs = q.getResultList();
		List<MedicaoItemResp> resultList = new ArrayList<>(); 

		for (Object[] el : rs) {
			int nrParam = 0;
			resultList.add(new MedicaoItemResp(
					(Long)el[nrParam++], (BigDecimal)el[nrParam++], ((Timestamp)el[nrParam]).toLocalDateTime())
					);
		}
		
		String sqlCount = """
			select 	top 1  
				COUNT(*) OVER () AS TotalRecords
			from medicao_sensor m 
			%s
				""";
		Query qCount = em.createNativeQuery(sqlCount.formatted(where + groupBy));

		iParams = 1;
		for (Object param : params) {
			qCount.setParameter(iParams++, param);
		}
		
		Integer rsCount = null;
		try {
			rsCount = (Integer)qCount.getSingleResult();
		} catch (NoResultException e) {
			rsCount = 0;
		}
		
		return new MedicaoListaResp(
				DaoHelper.infoPaginas(paginacao, rsCount.longValue(), resultList.size()),
				resultList
				);
				
	}
	/*
	public MedicaoListaResp listarAgrupado(final PaginacaoQueryParams paginacao, final DtParams dtParams, TipoAgrupamento tipoAgrupamento) {
		
		Integer total = 0;
		String where = "WHERE 1 = 1 ";
		String campoGroupBy = "cast(cast(m.dtMedicao as LocalDate) as LocalDateTime) ";
		String groupBy = " group by (" + campoGroupBy + ")";
		String jpql = """
				select new br.univesp.sensores.dto.responses.MedicaoItemResp (
					ROW_NUMBER() over (order by %s desc) as idMedicaoRow
					,cast(avg(m.vlTemperatura) as BigDecimal),cast(avg(m.vlUmidade) as BigDecimal),%s as dtAgrp
				) from MedicaoSensor m 
				""".formatted(campoGroupBy,campoGroupBy);
		//final String orderBy = " order by m.dtMedicao desc ";
		Map<String,Object> params = new HashMap<>();
		
		where += DaoHelper.addWhereRangeData(params, dtParams, "dtMedicao");
		
		jpql += where + groupBy;
				//orderBy;
		TypedQuery<MedicaoItemResp> query = em.createQuery(jpql, MedicaoItemResp.class);
		params.forEach(query::setParameter);
		

		String jpqlCount = """
			select count(1) from MedicaoSensor m 
			""" + where + groupBy;
		
		TypedQuery<Long> queryCount = em.createQuery(jpqlCount, Long.class);
		params.forEach(queryCount::setParameter);
		total = queryCount.getResultList().size();
		
		List<MedicaoItemResp> resultList = paginacao.configurarPaginacao(query).getResultList();
		return new MedicaoListaResp(
				DaoHelper.infoPaginas(paginacao, total.longValue(), resultList.size()),
				resultList
				);
				
	}*/
}
