package br.univesp.sensores.entidades;

import br.univesp.sensores.helpers.ConfigHelper.ChaveUser;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="user_config")
public class UserConfig {

	@Id
	@Enumerated(EnumType.STRING)
	private ChaveUser configNome;
	private String configValor;
	
	/**
	 * Construtor exclusivo para o framework Jakarta.
	 */
	@Deprecated
	public UserConfig() {}

	public UserConfig(ChaveUser configNome, String configValor) {
		this.configNome = configNome;
		this.configValor = configValor;
	}

	public ChaveUser getConfigNome() {
		return configNome;
	}

	public String getConfigValor() {
		return configValor;
	}
	
	public UserConfig alterarValor(String novoValor) {
		this.configValor = novoValor;
		return this;
	}
}
