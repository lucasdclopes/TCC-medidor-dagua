package br.univesp.sensores.dao;

import java.util.List;
import java.util.Optional;

import br.univesp.sensores.entidades.UserConfig;
import br.univesp.sensores.helpers.ConfigHelper.ChaveUser;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class UserConfigDao {
	
	@PersistenceContext
	private EntityManager em;
	
	public UserConfig atualizar(UserConfig config) {
		return em.merge(config);
	}
	
	public Optional<UserConfig> buscarPorId(final ChaveUser chave) {
		return Optional.ofNullable(
				em.find(UserConfig.class, chave)
				);
	}
	public List<UserConfig> todasConfigs(){
		String jpql = "SELECT c FROM UserConfig c";
		return em.createQuery(jpql,UserConfig.class).getResultList();
	}

}
