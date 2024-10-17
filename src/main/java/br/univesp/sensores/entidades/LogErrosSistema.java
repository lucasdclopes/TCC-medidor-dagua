package br.univesp.sensores.entidades;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "log_erros_sistema")
public class LogErrosSistema implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idLogErros;
	private String msgErro;
	private LocalDateTime dtLog;
	private String stacktrace;
	
	/**
	 * Construtor exclusivo para o framework Jakarta.
	 */
	@Deprecated
	public LogErrosSistema() {}

	public LogErrosSistema(LocalDateTime dtLog, Exception erro) {
		this.msgErro = erro.getMessage();
		this.dtLog = dtLog;
		StringWriter errors = new StringWriter();
		erro.printStackTrace(new PrintWriter(errors));
		this.stacktrace = errors.toString();
	}

	public Long getIdLogErros() {
		return idLogErros;
	}

	public String getMsgErro() {
		return msgErro;
	}

	public LocalDateTime getDtLog() {
		return dtLog;
	}

	public String getStacktrace() {
		return stacktrace;
	}

}
