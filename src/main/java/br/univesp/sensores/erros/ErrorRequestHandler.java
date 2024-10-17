package br.univesp.sensores.erros;

import java.time.LocalDateTime;

import org.jboss.logging.Logger;

import br.univesp.sensores.dao.LogErrosDao;
import br.univesp.sensores.dto.responses.ResponseSimples;
import br.univesp.sensores.entidades.LogErrosSistema;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBTransactionRolledbackException;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ErrorRequestHandler implements ExceptionMapper<Exception> {
		
	private static final Logger LOGGER = Logger.getLogger( ErrorRequestHandler.class.getName());
	
	private final static String MSG_GENERICA = "Ocorreu um erro inesperado. Contate o administrador para consultar os logs";
	@Inject private LogErrosDao errosDao;
	
	@Override
	public Response toResponse(Exception e) {
				
		//checa por exceções que "envolvem" outras exceções, pegando a exceção orignal
		if (e instanceof EJBTransactionRolledbackException ejbe) 
			e = checkCause(ejbe);

		if (e instanceof EJBException ejbe) 
			e = checkCause(ejbe);

		
		
		//Checa por exceções geradas pelo servidor HTTP.
		if (e instanceof NotAllowedException)//verbo HTTP incorreto, não gera log, se não bots de pesquisa podem lotar os nossos logs
			return Response.status(Status.METHOD_NOT_ALLOWED).entity(new ResponseSimples("verbo HTTP inválido")).build();
		if (e instanceof NotFoundException) {//endereço sem nenhum match nos resources, não gera log, se não bots de pesquisa podem lotar os nossos logs{
			if (e.getCause() instanceof IllegalArgumentException) {//provavelmente parâmetros incorretos na URL
				return Response.status(Status.NOT_FOUND).entity(new ResponseSimples("url construída de forma inválida")).build();
			}
				
			return Response.status(Status.NOT_FOUND).entity(new ResponseSimples("endereço inválido")).build();
		}
		
		try {
			errosDao.salvar(new LogErrosSistema(LocalDateTime.now(), e));
		} catch (Exception ex) {
			LOGGER.fatal("Não foi possível logar um erro geral",e);
			return Response.status(500).entity(new ResponseSimples(MSG_GENERICA)).build();
		}
		
		//erro tratado
		if (e instanceof ErroNegocioException) {
			return Response.status(422).entity(new ResponseSimples(e.getMessage())).build();
		} 
		
		Integer status = 500;
		String msgErroUsuario = MSG_GENERICA;
		if (e instanceof NotSupportedException err) {
			status = 415;
			msgErroUsuario = err.getMessage();
		}
		
		//Por ser um erro não tratado, esconde a mensagem de erro, que ficará disponível somente para quem tiver acesso aos logs
		return Response.status(status).entity(msgErroUsuario).build();
	}
	
	public Exception checkCause(Exception e) {		
		return e.getCause() != null? (Exception) e.getCause() : e;	
	}
}
