package br.univesp.sensores.dto.requests;

import br.univesp.sensores.helpers.ConfigHelper.ChaveUser;

public record AtualizaUserConfig(ChaveUser chave, String valor ) {
}
