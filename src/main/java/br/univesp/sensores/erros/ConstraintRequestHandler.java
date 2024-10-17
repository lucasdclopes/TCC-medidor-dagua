package br.univesp.sensores.erros;

import java.time.LocalDateTime;

import org.jboss.logging.Logger;

import br.univesp.sensores.dao.LogErrosDao;
import br.univesp.sensores.dto.responses.ResponseSimples;
import br.univesp.sensores.entidades.LogErrosSistema;
import br.univesp.sensores.helpers.StringHelper;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConstraintRequestHandler implements ExceptionMapper<ConstraintViolationException> {

	private static final Logger LOGGER = Logger.getLogger( ConstraintRequestHandler.class.getName());
	@Inject private LogErrosDao errosDao;
	
	@Override
	public Response toResponse(ConstraintViolationException e) {
		
		String msgErro = "Por favor revise as informações: ";
		for (ConstraintViolation<?> cv : e.getConstraintViolations()) {
			String valorRecebido = null;
			if (cv.getInvalidValue() != null) {
				valorRecebido = cv.getInvalidValue().toString();
				if (StringHelper.isNullOuVazio(valorRecebido))
					valorRecebido = "vazio";
				if (valorRecebido.length() > 20)
					valorRecebido = valorRecebido.substring(0, 20) + "..." ;
			}
				
			msgErro += cv.getMessage() + ", recebido " + valorRecebido + ". ";
		}
		
		try {
			errosDao.salvar(new LogErrosSistema(LocalDateTime.now(), e));
		} catch (Exception ex) {
			LOGGER.fatal("Não foi possível logar um erro de validação",e);
			return Response.status(500).entity(new ResponseSimples("Ocorreu um erro inesperado. Contate o administrador para consultar os logs")).build();
		}

		return Response.status(422).entity(new ResponseSimples(msgErro)).build();
	}

}
