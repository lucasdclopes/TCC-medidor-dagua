package br.univesp.sensores.entidades;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.univesp.sensores.dao.UserConfigDao;
import br.univesp.sensores.dto.responses.MedicaoItemResp;
import br.univesp.sensores.erros.ErroNegocioException;
import br.univesp.sensores.helpers.ConfigHelper;
import br.univesp.sensores.helpers.ConfigHelper.Chave;
import br.univesp.sensores.helpers.ConfigHelper.ChaveUser;
import br.univesp.sensores.services.EmailService;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "alerta")
public class Alerta implements Serializable {
	
	//private static final Logger LOGGER = Logger.getLogger( Alerta.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idAlerta;
	private Boolean isHabilitado;
	private Integer intervaloEsperaSegundos;
	private BigDecimal vlMax;
	private BigDecimal vlMin;
	private LocalDateTime dtCriado;
	private String destinatarios;
	private LocalDateTime dtUltimoEnvio; 
	private Boolean habilitarDispositivo; 
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "alerta", orphanRemoval = true, cascade = {CascadeType.PERSIST,CascadeType.MERGE})
	private Set<AlertaEnviado> alertasEnviados = new HashSet<>();
	
	private final static Integer INTERVALO_MIN = ConfigHelper.getInstance()
			.getConfigInteger(Chave.ALERTA_INTERVALO_MIN);
	
	/**
	 * Construtor exclusivo para o framework Jakarta.
	 */
	@Deprecated
	public Alerta() {}

	public Alerta(Integer intervaloEsperaSegundos, BigDecimal vlMax, BigDecimal vlMin, String destinatarios, Boolean habilitarDispositivo) {
		if (vlMax == null && vlMin == null)
			throw new ErroNegocioException("Pelo menos o valor mínimo ou valor máximo precisa estar preenchido. Ambos estão vazios");
		
		validarIntervalo(intervaloEsperaSegundos);
		validarEmails(destinatarios);
		
		if (vlMax != null && vlMin != null && vlMax.compareTo(vlMin) <= 0 )
			throw new ErroNegocioException("O valor do limite mínimo não pode ser igual ou maior que o valor do limite máximo");

		this.intervaloEsperaSegundos = intervaloEsperaSegundos;
		this.vlMax = vlMax;
		this.vlMin = vlMin;
		this.isHabilitado = true;
		this.dtCriado = LocalDateTime.now();
		this.destinatarios = destinatarios;
		this.habilitarDispositivo = habilitarDispositivo;
	}
	
	public void habilitar() {
		this.isHabilitado = true;
	}
	
	public void desabilitar() {
		this.isHabilitado = false;
	}
	
	public void habilitarDispositivo() {
		this.habilitarDispositivo = true;
	}
	
	public void desabilitarDispositivo() {
		this.habilitarDispositivo = false;
	}
	
	public void alterarDestinatarios(String destinatarios) {
		validarEmails(destinatarios);
		this.destinatarios = destinatarios;
	}
	
	public void alterarIntervalo(Integer intervalo) {
		validarIntervalo(intervalo);
		this.intervaloEsperaSegundos = intervalo;
		
	}	
	
	public void enviarAlerta(List<MedicaoItemResp> medicoes, EmailService email, UserConfigDao userConfDao) {
		
		//verifica se já se passaram X segundos desde o último envio
		if (this.dtUltimoEnvio != null && 
				this.dtUltimoEnvio.until(LocalDateTime.now(), ChronoUnit.SECONDS) < this.intervaloEsperaSegundos)
			return;
		
		//Se algum alerta entrar nas condições, envia o alerta
		medicoes.stream().filter(m -> 
			checkRange(m.vlDistancia()) //verifica se algum alerta é maior que o limite máximo, ou menor que o limite mínimo
		).findAny().ifPresent(medicao -> {
			LocalDateTime agora = LocalDateTime.now();
			
			ConfigHelper config = ConfigHelper.getInstance();
			Map<String,File> anexos = new HashMap<>();
			anexos.put("level", config.getResourceFile("level.png"));
			
			String titulo = ConfigHelper.getInstance().getConfig(ChaveUser.EMAIL_ALERTA_TITULO, userConfDao);
			email.enviarEmail(titulo, this.destinatarios,montarEmailAlerta(medicao),anexos);
			this.alertasEnviados.add(new AlertaEnviado(this,agora));
			this.dtUltimoEnvio = agora;
		});
	
	}
	
	private final static DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private String montarEmailAlerta(MedicaoItemResp medicao) {
		
		String template = ConfigHelper.getInstance().getEmailTemplateEmailAlerta();
		return template
				.replace("${idAlerta}", this.getIdAlerta().toString())
				.replace("${vlDistancia}", medicao.vlDistancia().toPlainString())
				.replace("${dtCriacao}", this.getDtCriado().format(datePattern))
				.replace("${vlMin}", this.getVlMin()!=null?this.getVlMin().toPlainString():"N/A")
				.replace("${vlMax}", this.getVlMax()!=null?this.getVlMax().toPlainString():"N/A")
				.replace("${dtUltimoEnvio}",this.dtUltimoEnvio == null? "N/A" : this.dtUltimoEnvio.format(datePattern));
	}
	
	private void validarIntervalo(Integer intervalo) {
		if (intervalo < INTERVALO_MIN)
			throw new ErroNegocioException(
					String.format("O tempo de espera entre alertas não pode ser menor do que %s segundos",INTERVALO_MIN));
		
	}
	
	private Boolean checkRange(BigDecimal vlMedicao) {
		return (vlMax != null && vlMedicao.compareTo(vlMax) > 0)
				|| (vlMin != null && vlMedicao.compareTo(vlMin) < 0);
	}
	
	private Set<String> validarEmails(String destinatarios) {
		Set<String> emails = new HashSet<String>(Arrays.asList(destinatarios.split(","))); //jogar em um SET elimina os duplicados
		emails.forEach(mail -> {
			try {
				new InternetAddress(mail).validate();
			} catch (AddressException e) {
				throw new ErroNegocioException("O endereço de email ("+ mail +") informado é inválido");
			}
		});
		return emails;
	}
	
	public Long getIdAlerta() {
		return idAlerta;
	}

	public Boolean getIsHabilitado() {
		return isHabilitado;
	}

	public Integer getIntervaloEsperaSegundos() {
		return intervaloEsperaSegundos;
	}
	
	public void setVlMax(BigDecimal vlMax) {
		this.vlMax = vlMax;
	}

	public BigDecimal getVlMax() {
		return vlMax;
	}

	public BigDecimal getVlMin() {
		return vlMin;
	}
	
	public LocalDateTime getDtCriado() {
		return dtCriado;
	}
	
	public String getDestinatarios() {
		return destinatarios;
	}
	
	public Boolean deveHabilitarDispositivo() {
		return habilitarDispositivo;
	}

	public Set<AlertaEnviado> getAlertasEnviados() {
		return alertasEnviados;
	}

}
