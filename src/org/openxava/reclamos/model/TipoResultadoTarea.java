package org.openxava.reclamos.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.validators.*;

@Entity
@EntityValidators({
	@EntityValidator(
		value=PrincipalValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="modelo", value="TipoResultadoTarea"),
			@PropertyValue(name="principal")
		})
})
//Deposito Entity Valitator

public class TipoResultadoTarea extends ObjetoEstatico{
	
	
	private boolean principal;
										//Validar solo un principal (Preguntar)
	public boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(boolean principal) {
		this.principal = principal;
	}

}
