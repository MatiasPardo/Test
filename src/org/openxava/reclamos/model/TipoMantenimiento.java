package org.openxava.reclamos.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.negocio.validators.*;

@Entity

@Views({
	@View(name="Simple", members="codigo, nombre")
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@EntityValidators({
	@EntityValidator(
		value=PrincipalValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="modelo", value="TipoMantenimiento"),
			@PropertyValue(name="principal")
		})
})

public class TipoMantenimiento extends ObjetoEstatico{
		
	@Required
	private TipoNativoMantenimiento tipo;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean principal;
	
	public TipoNativoMantenimiento getTipo() {
		return tipo;
	}

	public void setTipo(TipoNativoMantenimiento tipo) {
		this.tipo = tipo;
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}
}
