package br.univesp.sensores.erros;

public class ErroNegocioException extends RuntimeException {

	private static final long serialVersionUID = -3847212462239944744L;

	public ErroNegocioException() {
		super();
	}

	public ErroNegocioException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ErroNegocioException(String message, Throwable cause) {
		super(message, cause);
	}

	public ErroNegocioException(String message) {
		super(message);
	}

	public ErroNegocioException(Throwable cause) {
		super(cause);
	}

	
}
