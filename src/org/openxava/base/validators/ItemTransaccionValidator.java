package org.openxava.base.validators;

import org.openxava.base.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public abstract class ItemTransaccionValidator implements IValidator{

	private Transaccion transaccion;
	
	public Transaccion getTransaccion() {
		return transaccion;
	}

	public void setTransaccion(Transaccion transaccion) {
		this.transaccion = transaccion;
	}

	@Override
	public void validate(Messages errors) throws Exception {
		boolean validar = true;
		
		if (this.getTransaccion() != null){
			validar = !this.getTransaccion().finalizada();
		}
		
		if (validar){
			validarItemTransaccion(errors);
		}		
	}
	
	protected abstract void validarItemTransaccion(Messages errores);  
}
