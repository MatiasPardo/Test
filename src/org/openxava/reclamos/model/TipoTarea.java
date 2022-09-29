package org.openxava.reclamos.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.validators.*;

@Entity

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})
@EntityValidators({
	@EntityValidator(
		value=PrincipalValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="modelo", value="TipoTarea"),
			@PropertyValue(name="principal")
		})
})

public class TipoTarea extends ObjetoEstatico {
	
	private boolean principal;
										//Validar solo un principal (Preguntar)
	public boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(boolean principal) {
		this.principal = principal;
	}

}

