package br.univesp.sensores.services;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import br.univesp.sensores.dao.LogErrosDao;
import br.univesp.sensores.dao.UserConfigDao;
import br.univesp.sensores.entidades.LogErrosSistema;
import br.univesp.sensores.helpers.ConfigHelper;
import br.univesp.sensores.helpers.ConfigHelper.ChaveUser;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.inject.Inject;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

@Stateless
public class EmailService {
	
	@Inject private LogErrosDao errosDao;
	@Inject private UserConfigDao userConf;

	public void enviarEmail(String titulo, String emails, String mensagem, Map<String,File> anexos) {
		
		ConfigHelper config = ConfigHelper.getInstance();
		
		
		Properties prop = new Properties();
		prop.put("mail.smtp.auth", true);
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.host", config.getConfig(ChaveUser.EMAIL_SMTP_HOSTNAME,userConf));
		prop.put("mail.smtp.port", config.getConfigInteger(ChaveUser.EMAIL_SMTP_PORTA,userConf));
		prop.put("mail.smtp.ssl.trust", config.getConfig(ChaveUser.EMAIL_SMTP_HOSTNAME,userConf));
		

		Session session = Session.getInstance(prop, new Authenticator() {
		    @Override
		    protected PasswordAuthentication getPasswordAuthentication() {
		        return new PasswordAuthentication(config.getConfig(ChaveUser.EMAIL_SMTP_USER,userConf),config.getConfig(ChaveUser.EMAIL_SMTP_SENHA,userConf));
		    }
		});
		
		
		Message message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(config.getConfig(ChaveUser.EMAIL_ENDERECO_REMETENTE,userConf)));
			message.setRecipients(	 
					Message.RecipientType.TO,InternetAddress.parse(emails)		  
					);
			message.setSubject(titulo);
			
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(mensagem, "text/html; charset=utf-8");
			
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);
			
			for (var anexo : anexos.entrySet()) {
				MimeBodyPart imagePart = new MimeBodyPart();
				imagePart.setHeader("Content-ID", "<"+anexo.getKey()+">");
				imagePart.setDisposition(MimeBodyPart.INLINE);
				// attach the image file
				imagePart.attachFile(anexo.getValue());
				multipart.addBodyPart(imagePart);
			}
			
	
			message.setContent(multipart);
		} catch (MessagingException | IOException e) {
			throw new RuntimeException("Erro durante a configuração do email",e);
		}
		
		try {
			ManagedThreadFactory managedThreadFactory = InitialContext.doLookup("java:comp/DefaultManagedThreadFactory");
			
			managedThreadFactory.newThread(() -> { 
				try {
					Transport.send(message);
				} catch (Exception e) {
					errosDao.salvar(new LogErrosSistema(LocalDateTime.now(), e));
				}
			}).start();
			
		} catch (NamingException e) {
			throw new RuntimeException("Não foi possível inicializar a thread de email",e);
		}
	}
}
