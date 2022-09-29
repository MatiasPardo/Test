package org.openxava.ventas.model;

import java.util.*;

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
			@PropertyValue(name="modelo", value="TipoEntrega"),
			@PropertyValue(name="principal")
		}
	)	
})

public class TipoEntrega extends ObjetoEstatico{
	
	@DefaultValueCalculator(FalseCalculator.class)
	private Boolean principal = Boolean.FALSE;
	
	@Required
	private ComportamientoTipoEntrega comportamiento;

	@DefaultValueCalculator(FalseCalculator.class)
	private Boolean domicilioObligatorio = Boolean.FALSE;
	
	public ComportamientoTipoEntrega getComportamiento() {
		return comportamiento;
	}

	public void setComportamiento(ComportamientoTipoEntrega comportamiento) {
		this.comportamiento = comportamiento;
	}
	
	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	public Boolean getDomicilioObligatorio() {
		return domicilioObligatorio;
	}

	public void setDomicilioObligatorio(Boolean domicilioObligatorio) {
		if (domicilioObligatorio == null){
			this.domicilioObligatorio = Boolean.FALSE;
		}
		else{
			this.domicilioObligatorio = domicilioObligatorio;
		}
	}

	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		propiedadesSoloLectura.add("comportamiento");
	}	
	
	public void propiedadesSoloLecturaAlCrear(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion){
		super.propiedadesSoloLecturaAlCrear(propiedadesSoloLectura, propiedadesEditables, configuracion);
		propiedadesEditables.add("comportamiento");
	}
}
