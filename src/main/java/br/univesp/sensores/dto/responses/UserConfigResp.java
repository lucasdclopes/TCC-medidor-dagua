package br.univesp.sensores.dto.responses;

import br.univesp.sensores.helpers.ConfigHelper.ChaveUser;

public record UserConfigResp(ChaveUser chave, String valor) {

}
