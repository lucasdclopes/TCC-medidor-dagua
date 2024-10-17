package br.univesp.sensores.entidades;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicao_sensor")
public class MedicaoSensor implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idMedicao;
	private BigDecimal vlDistancia;
	private LocalDateTime dtMedicao ;
	
	/**
	 * Construtor exclusivo para o framework Jakarta.
	 */
	@Deprecated
	public MedicaoSensor() {}
	

	public MedicaoSensor(BigDecimal vlDistancia) {
		this.vlDistancia = vlDistancia;
		this.dtMedicao = LocalDateTime.now();
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Long getIdMedicao() {
		return idMedicao;
	}

	public BigDecimal getVlDistancia() {
		return vlDistancia;
	}

	public LocalDateTime getDtMedicao() {
		return dtMedicao;
	}
	
	
}
