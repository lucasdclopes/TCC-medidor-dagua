package br.univesp.sensores.helpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

/**
 * Necessário para os endpoints trabalharem com LocalDate e LocalDateTime
 */
public class LocalDateRestHelper {
	
	private final static DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE_TIME; 
	
	
	@Provider
	public static class LocalDateParamConverterProvider implements ParamConverterProvider {
	    @SuppressWarnings("unchecked") //não há necessidade de checagem pelo type, neste caso é garantido pelo framework
		@Override
	    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType,//java.lang.reflect.Type;
	            Annotation[] annotations) {
	        if (rawType.equals(LocalDateTime.class))
	            return (ParamConverter<T>) new LocalDateTimeConversor();
	        if (rawType.equals(LocalDate.class))
	            return (ParamConverter<T>) new LocalDateConversor();
	        return null;
	    }
	}
	
	public static class LocalDateTimeConversor implements ParamConverter<LocalDateTime> {
		 @Override
	    public LocalDateTime fromString(String value) {
	        if (StringHelper.isNullOuVazio(value))
	            return null;
	        return LocalDateTime.parse(value, ISO_DATE);
	    }

	    @Override
	    public String toString(LocalDateTime value) {
	        if (value == null)
	            return null;
	        return value.format(ISO_DATE);
	    }
	}
	
	public static class LocalDateConversor implements ParamConverter<LocalDate> {
		@Override
	    public LocalDate fromString(String value) {
	        if (StringHelper.isNullOuVazio(value))
	            return null;
	        return LocalDate.parse(value, ISO_DATE);
	    }
	
	    @Override
	    public String toString(LocalDate value) {
	        if (value == null)
	            return null;
	        return value.format(ISO_DATE);
	    }
	}
}
