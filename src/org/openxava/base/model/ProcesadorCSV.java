package org.openxava.base.model;

import java.math.*;
import java.text.*;
import java.util.Date;

import org.openxava.util.Is;
import org.openxava.validators.*;

public class ProcesadorCSV {
	
	public static BigDecimal convertirTextoANumero(String decimal){
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		symbols.setGroupingSeparator('.');
    	DecimalFormat format = new DecimalFormat("#,###.##", symbols);
    	format.setParseBigDecimal(true);
    	try {
			BigDecimal numero = (BigDecimal)format.parse(decimal);
			return numero;
		} catch (ParseException e) {
			throw new ValidationException("Formato númerico incorrecto: " + decimal + ". Espera un número con separador decimal ','");
		}
	}
	
	public static Date convertirTextoAFecha(String fecha, String mascaraFecha){
		String mascara = mascaraFecha;
		if (Is.empty(mascara)){
			mascara = "dd/MM/yyyy";
		}
		DateFormat format = new SimpleDateFormat(mascara);
		try {
			return format.parse(fecha);		
		}
		catch(Exception e){
			throw new ValidationException("Error al procesar la fecha " + fecha);
		}
	}
}
