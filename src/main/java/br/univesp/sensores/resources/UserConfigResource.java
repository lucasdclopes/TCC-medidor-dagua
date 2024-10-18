package br.univesp.sensores.resources;

import java.util.HashMap;
import java.util.Map;

import br.univesp.sensores.dao.UserConfigDao;
import br.univesp.sensores.erros.ErroNegocioException;
import br.univesp.sensores.helpers.ConfigHelper.ChaveUser;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/userconfig")
@Produces(MediaType.APPLICATION_JSON)
public class UserConfigResource {
	
	@Inject private UserConfigDao userConfDao;
	
	@PUT
	public Response atualizarConfigs(Map<ChaveUser,String> configsAtualizadas) {
		//atualiza todas as configs. Não é muito eficiente desta forma, mas é fácil de codar, e como são poucos dados não há problema
		//refazer se forem criadas muitas configurações
		configsAtualizadas.forEach((k,v) -> 
			userConfDao.atualizar(	
					userConfDao.buscarPorId(k).orElseThrow(() -> new ErroNegocioException("A configuração " + k.name() + "não existe"))
					.alterarValor(v)
					)
			);
		return Response.noContent().build();
	}
	@GET
	public Response carregarConfigs() {
		Map<ChaveUser,String> resposta = new HashMap<ChaveUser, String>(); 
		userConfDao.todasConfigs().forEach(el -> resposta.put(
				el.getConfigNome(), el.getConfigNome().isSegredo()? "************" : el.getConfigValor()
						));
		return Response.ok(resposta).build();
	}

	
}
