package br.univesp.sensores.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="user_config")
public class UserConfig {

	@Id
	private String configNome;
	private String configValor;
	
	/**
	 * Construtor exclusivo para o framework Jakarta.
	 */
	@Deprecated
	public UserConfig() {}

	public UserConfig(String configNome, String configValor) {
		this.configNome = configNome;
		this.configValor = configValor;
	}

	public String getConfigNome() {
		return configNome;
	}

	public String getConfigValor() {
		return configValor;
	}
	
	public void alterarValor(String novoValor) {
		this.configValor = novoValor;
	}
}
