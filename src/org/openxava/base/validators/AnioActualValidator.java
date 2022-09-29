package org.openxava.base.validators;

import java.util.*;

import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class AnioActualValidator implements IPropertyValidator {
	
	@Override
	public void validate(Messages errors, Object value, String propertyName, String modelName) throws Exception {
		Integer añoUsuario= (Integer)value;
		if (!Is.empty(añoUsuario)){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			int añoActual = calendar.get(Calendar.YEAR);
			if (añoUsuario > añoActual){
				errors.add("el año del auto no puede ser mayor a la fecha actual");
			}
		}
}

}