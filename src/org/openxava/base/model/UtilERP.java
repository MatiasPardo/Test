package org.openxava.base.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openxava.model.impl.IPersistenceProvider;
import org.openxava.model.meta.MetaModel;
import org.openxava.util.IPropertiesContainer;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.view.View;


public class UtilERP {

	private static final int ANIOSVALIDOS = 10;
		
	public static long diferenciaDias(Date desde, Date hasta){
		Calendar inicio = Calendar.getInstance();
		inicio.setTime(desde);
		Calendar fin = Calendar.getInstance();
		fin.setTime(hasta);
		
		long dif = fin.getTimeInMillis() - inicio.getTimeInMillis();
		return dif / (24 * 60 * 60 * 1000);		
	}
	
	public static Date trucarDateTime(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static void validarRangoFecha(Date fecha){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.YEAR, ANIOSVALIDOS * -1);
		if (fecha.compareTo(cal.getTime()) < 0){
			throw new ValidationException(UtilERP.convertirString(fecha) + " es inválida: no se aceptan fechas menores a 10 años");
		}
	}
	
	public static String convertirString(Date date){
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		return format.format(date);
	}
	
	public static String convertirString(BigDecimal decimal){
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		symbols.setGroupingSeparator('.');
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##", symbols);
		return decimalFormat.format(decimal);
	}
	
	public static String obtenerErrorPersistException(PersistenceException exception){
		String error = "";
		Throwable root = ExceptionUtils.getRootCause(exception);
		if (root instanceof ConstraintViolationException){
			ConstraintViolationException constraintEx = (ConstraintViolationException)root;
			
			StringBuffer errores = new StringBuffer();
			for(ConstraintViolation<?> constraint: constraintEx.getConstraintViolations()){
				if (errores.length() > 0) errores.append(", ");
				errores.append(constraint.getMessage());
			}
			error = errores.toString();
		}
		else{
			error = ExceptionUtils.getRootCauseMessage(exception);
			if (Is.emptyString(error)){
				error = exception.getMessage();
			}
			
		}
		return error;
	}
	
	public static void copyValuesViewToObject(View view, Object object){
		MetaModel metaModel = view.getMetaModel();
		IPersistenceProvider provider = (IPersistenceProvider) metaModel.getMetaComponent().getPersistenceProvider();
		IPropertiesContainer r = provider.toPropertiesContainer(metaModel, object);		
		Map<?, ?> values = view.getValues();
		Iterator<?> toRemove = metaModel.getOnlyReadPropertiesNames().iterator();
		while (toRemove.hasNext()) {
			values.remove(toRemove.next());
		}
		try{
			r.executeSets(values);
		}
		catch(Exception e){
			throw new ValidationException("Error al copiar valores desde la vista al modelo: " + e.toString());
		}
	}
	
	public static Class<?> tipoEntidad(Object objetoPersistente){
		if (objetoPersistente instanceof org.hibernate.proxy.HibernateProxy){
			return ((org.hibernate.proxy.HibernateProxy)objetoPersistente).getHibernateLazyInitializer().getPersistentClass(); 
		}
		else{
			return objetoPersistente.getClass();
		}
	}
}
