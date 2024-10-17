package br.univesp.sensores.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.jboss.logging.Logger;

import br.univesp.sensores.dao.UserConfigDao;

public class ConfigHelper {
	
	private static final Logger LOGGER = Logger.getLogger(ConfigHelper.class.getName());
	
	private final static ClassLoader loader = Thread.currentThread().getContextClassLoader();
	private final static InputStream inputConfigs = loader.getResourceAsStream("config.properties");
	private final static String EMAIL_ALERTA; 
	static {
		StringBuilder builder = new StringBuilder(2200);
		try (InputStream is = loader.getResourceAsStream("template_alerta.html");//src/main/resources
				InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(streamReader)) {

			String linha;
			while ((linha = reader.readLine()) != null) {
				builder.append(linha);
			}
		} catch (IOException e) {
			LOGGER.fatal("Não é possível inicializar o sistema por um problema no carregamento do html do email de alerta",e);
		}
		EMAIL_ALERTA = builder.toString();
	}
	
	private final static Properties properties = new Properties();
	private static ConfigHelper singleton = null;
	
	public enum Chave {		
		ALERTA_INTERVALO_MIN,
		ALERTA_LIMITE_TOTAL,
		CORS_URLS_PERMITIDAS,
		PAGINACAO_MAX_ITENS, 
		SCHEDULER_ALERTA_INTERVALO,
		SCHEDULER_ALERTA_LIGADO,
		SIMULADOR_INTERVALO;
	}
	
	public enum Chave_User {		
		EMAIL_NOME_REMETENTE,
		EMAIL_ENDERECO_REMETENTE,
		EMAIL_SMTP_HOSTNAME,
		EMAIL_SMTP_PORTA,
		EMAIL_SMTP_USER,
		EMAIL_SMTP_SENHA,
		
		MONITORAMENTO_INTERVALO_MS,
		SENSOR_ALTURA_RESERVATORIO_CM
	}
	//singleton
	public static ConfigHelper getInstance() {
		ConfigHelper instancia = singleton;
		if (instancia == null) {
			synchronized (ConfigHelper.class) {
				instancia = singleton;
				if (instancia == null)
					singleton = instancia = new ConfigHelper();
				
			}
		}
		try {
			properties.load(inputConfigs);
		} catch (IOException e) {
			throw new RuntimeException("Não foi possível carregar as configurações do sistema, " + e.getMessage(),e);
		}
		return instancia;

	}
	
	public String getConfig(Chave chave) { 
		String valor = properties.getProperty(chave.name());
		if (valor == null) 
			throw new RuntimeException("A configuração " + chave  + " não existe no sistema");
		return valor;
	}
	
	public String getConfig(Chave_User chave, UserConfigDao dao) { 
		return dao.buscarPorId(chave)
				.orElseThrow(() -> new RuntimeException("A configuração " + chave  + " não existe no sistema"))
				.getConfigValor();
	}
	
	public Integer getConfigInteger(Chave chave) { 
		return toInteger(getConfig(chave),chave.name());
	}
	
	public Integer getConfigInteger(Chave_User chave, UserConfigDao dao) { 
		return toInteger(getConfig(chave,dao),chave.name());
	}
	
	public Boolean getConfigBoolean(Chave chave) { 
		return toBoolean(getConfig(chave),chave.name());	
	}
	
	public Boolean getConfigBoolean(Chave_User chave, UserConfigDao dao) { 
		return toBoolean(getConfig(chave,dao),chave.name());	
	}
	
	private Boolean toBoolean(String vlConfig, String vlChave) {
		if (vlConfig.equalsIgnoreCase("true"))
			return true;
		else if (vlConfig.equalsIgnoreCase("false"))
			return false;
		else 
			throw new RuntimeException("O valor da chave " + vlChave  + " deveria ser true ou false");	
	}
	
	private Integer toInteger(String vlConfig, String vlChave) {
		try {
			return Integer.parseInt(vlConfig);
		} catch (NumberFormatException e) {
			throw new RuntimeException("O valor da chave " + vlChave  + " deveria ser numérico");
		}
	}
	
	public File getResourceFile(String fileName) {
		return new File(loader.getResource(fileName).getFile());
	}
	
	public String getEmailTemplateEmailAlerta() {
		return EMAIL_ALERTA;
	}
}
