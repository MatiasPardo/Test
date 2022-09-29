package org.openxava.reclamos.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.validators.PrincipalValidator;
import org.openxava.rrhh.model.*;


@Entity

@Views({
	@View(name="Cumplimiento",
		members="codigo, nombre; empleados"),
	@View(members="usuario, fechaCreacion;" +
			"codigo, activo, principal;" + 
			"nombre;" +
			"empleados;" + 
			"vehiculo;")
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
			@PropertyValue(name="modelo", value="UsuarioReclamo"),
			@PropertyValue(name="principal")
		}
	)
})

public class UsuarioReclamo extends ObjetoEstatico {
		
	@OneToMany(mappedBy="usuarioReclamo",cascade=CascadeType.ALL)
	@ListProperties(value="nombre, codigo")
	@ReadOnly
	private Collection<Empleado> empleados;
  
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@NoCreate @NoModify
	@ReferenceView("Reclamo")
	private Vehiculo vehiculo;
  
	private Boolean principal = Boolean.FALSE;
	  
	public Collection<Empleado> getEmpleados() {
		return empleados;
	}

	public void setEmpleados(Collection<Empleado> empleados) {
		this.empleados = empleados;
	}

	public Vehiculo getVehiculo() {
		return vehiculo;
	}

	public void setVehiculo(Vehiculo vehiculo) {
		this.vehiculo = vehiculo;
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	} 
}
