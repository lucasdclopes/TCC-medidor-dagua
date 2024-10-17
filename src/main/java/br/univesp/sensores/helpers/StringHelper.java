package br.univesp.sensores.helpers;

public class StringHelper {

	public static boolean isNullOuVazio(String value) {
		return value == null || value.isBlank();
	}
}
