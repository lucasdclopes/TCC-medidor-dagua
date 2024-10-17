package br.univesp.sensores.helpers;

import br.univesp.sensores.erros.ErroNegocioException;

public class EnumHelper {
	
	public interface IEnumDescritivel {
		public String getDescricao();
		public Integer getCodigo();
	}
	
	public static <E extends Enum<E> & IEnumDescritivel> E getEnumFromCodigo(Integer codigo, Class<E> clazz) {
		
		E[] enums = clazz.getEnumConstants();  
		for (E e : enums) {          
			if (e.getCodigo().equals(codigo)) {        
				return e; 
			}
		}      
		throw new ErroNegocioException(
				String.format("Valor (%s) inválido para %s",codigo,enums[0].getDescricao())
		);
	}
}
