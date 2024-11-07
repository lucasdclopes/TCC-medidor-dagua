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

	public MedicaoSensor(BigDecimal vlDistancia) {
		this.vlDistancia = vlDistancia;
		this.dtMedicao = LocalDateTime.now();
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
	
	/**
	 * Construtor exclusivo para o framework Jakarta.
	 */
	@Deprecated
	public MedicaoSensor() {}
	
	/**
	 * Ajusta o valor medido considerando a profundidade do recipiente definida pelo usuário
	 * @param vlDistancia distancia medida
	 * @param profundidade profundidade configurada
	 */
	public static BigDecimal normalizarComProfundidade(BigDecimal vlDistancia, Integer profundidade) {
		vlDistancia = new BigDecimal(profundidade).subtract(vlDistancia);
		return (vlDistancia.compareTo(BigDecimal.ZERO) < 0)? BigDecimal.ZERO : vlDistancia; //se for menor do que zero, usa zero.
	}
	
	
}
