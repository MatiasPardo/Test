package org.openxava.base.validators;

import java.util.*;

import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class ValidadorFechaActual implements IPropertyValidator{

	@Override
	public void validate(Messages errors, Object value, String propertyName, String modelName) throws Exception {
		Date fechaTransaccion = (Date)value;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		calendar.set(Calendar.HOUR_OF_DAY, calendar.getMinimum(Calendar.HOUR_OF_DAY));
		calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
		calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
		calendar.set(Calendar.MILLISECOND, calendar.getMinimum(Calendar.MILLISECOND));
		if (calendar.getTime().compareTo(fechaTransaccion) <= 0){
			errors.add("La fecha no puede ser mayor a la fecha actual");
		}				
	}

}
