package br.univesp.sensores.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(1)
public class ServerFilter implements ContainerRequestFilter {
	
	private static final Logger LOGGER = Logger.getLogger( ServerFilter.class.getName());

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		StreamCopiado objetoParseado = copiarStream(requestContext.getEntityStream());	
		
		requestContext.setEntityStream(objetoParseado.streamCopia()); //devolve uma cópia do stream pro server
		String payload = objetoParseado.streamOriginal().toString(StandardCharsets.UTF_8); 
		
		LOGGER.log(Level.INFO,String.format(
				"Requisição recebida. %s %s PayloadRequest: %s. ",
				requestContext.getMethod(),
				requestContext.getUriInfo().getRequestUri().toString(),
				payload
				));
		
	}
	
	
	public static record StreamCopiado(
			ByteArrayOutputStream streamOriginal,ByteArrayInputStream streamCopia
			)
	{}
	private StreamCopiado copiarStream(InputStream inputStream) throws IOException {

		try (ByteArrayOutputStream captura = new ByteArrayOutputStream();
				ByteArrayOutputStream copia = new ByteArrayOutputStream();
				InputStream streamOriginal = inputStream;) {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = streamOriginal.read(buffer)) != -1) {
				captura.write(buffer, 0, length);
				copia.write(buffer, 0, length);
			}
			/*return new StreamCopiado(
					captura.toString(encoding.name()),new ByteArrayInputStream(copia.toByteArray())
					);*/
			
			return new StreamCopiado(
					captura,new ByteArrayInputStream(copia.toByteArray())
					);
		}	
	}

}
