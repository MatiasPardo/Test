package org.openxava.base.validators;

import java.util.*;

import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class AnioActualValidator implements IPropertyValidator {
	
	@Override
	public void validate(Messages errors, Object value, String propertyName, String modelName) throws Exception {
		Integer a�oUsuario= (Integer)value;
		if (!Is.empty(a�oUsuario)){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			int a�oActual = calendar.get(Calendar.YEAR);
			if (a�oUsuario > a�oActual){
				errors.add("el a�o del auto no puede ser mayor a la fecha actual");
			}
		}
}

}