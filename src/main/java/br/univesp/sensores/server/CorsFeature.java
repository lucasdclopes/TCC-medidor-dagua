package br.univesp.sensores.server;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.resteasy.plugins.interceptors.CorsFilter;

import br.univesp.sensores.helpers.ConfigHelper;
import br.univesp.sensores.helpers.ConfigHelper.Chave;
import br.univesp.sensores.helpers.StringHelper;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;


@Provider
public class CorsFeature implements Feature {
	
	private static final Logger LOGGER = Logger.getLogger(CorsFeature.class.getName());
	
	@Override
	public boolean configure(FeatureContext context) {		
		
		try {
			
			CorsFilter corsFilter; 
			String urlIterado;
		
			// Monta a lista dando split na vírgula e tirando possíveis espaços em branco.			
			List<String> listaUrlsPermitidasCors = new ArrayList<String>(Arrays.asList(
					ConfigHelper.getInstance().getConfig(Chave.CORS_URLS_PERMITIDAS).split("\\s*,\\s*")
					));
			ListIterator<String> iteratorListaUrlsPermitidasCors = listaUrlsPermitidasCors.listIterator();						
			
			// Percorro a lista procurando item vazio no meio da string. Se tiver, tira da lista.
			while (iteratorListaUrlsPermitidasCors.hasNext()){
				urlIterado = iteratorListaUrlsPermitidasCors.next();	
				if (StringHelper.isNullOuVazio(urlIterado) ) {
					iteratorListaUrlsPermitidasCors.remove();
				}
			}

			corsFilter = new CorsFilter();					
			corsFilter.getAllowedOrigins().addAll(listaUrlsPermitidasCors);
			corsFilter.setExposedHeaders("*");
			corsFilter.setAllowCredentials(true);		
			context.register(corsFilter);
			return true;
		}
		catch (Exception e) {
			LOGGER.log(Level.FATAL, "Erro ao inicializar o sistema.", e);
			throw new IllegalArgumentException("Não foi possível carregar o CORS.",e);			
		}
	}  
}