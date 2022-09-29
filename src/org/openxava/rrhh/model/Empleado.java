package org.openxava.rrhh.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.reclamos.model.*;

import com.openxava.naviox.model.User;

@Entity

@View(members=
	"codigo, activo;" +
	"nombre;" + 
	"dni, fechaNacimiento, fechaVencimientoRegistro;" + 
	"funcion;" +
	"usuarioSistema;" + 
	"usuarioReclamo;"
)

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

public class Empleado extends ObjetoEstatico{

	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@DescriptionsList(descriptionProperties="codigo")
	@NoCreate @NoModify
	private UsuarioReclamo usuarioReclamo;
	
	@Column(length=10)
	private String dni;
	
	private Date fechaNacimiento;

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@DescriptionsList(descriptionProperties="nombre", forTabs="Combo")
	@NoCreate @NoModify
	private FuncionEmpleado funcion;
	
	@OneToOne(fetch=FetchType.LAZY, optional=true, orphanRemoval=false)
	@DescriptionsList(descriptionProperties="name")
	@NoCreate @NoModify	
	private User usuarioSistema;
	
	private Date fechaVencimientoRegistro;

	public UsuarioReclamo getUsuarioReclamo() {
		return usuarioReclamo;
	}

	public void setUsuarioReclamo(UsuarioReclamo usuarioReclamo) {
		this.usuarioReclamo = usuarioReclamo;
	}

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}

	public Date getFechaNacimiento() {
		return fechaNacimiento;
	}

	public void setFechaNacimiento(Date fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	public FuncionEmpleado getFuncion() {
		return funcion;
	}

	public void setFuncion(FuncionEmpleado funcion) {
		this.funcion = funcion;
	}

	public Date getFechaVencimientoRegistro() {
		return fechaVencimientoRegistro;
	}

	public void setFechaVencimientoRegistro(Date fechaVencimientoRegistro) {
		this.fechaVencimientoRegistro = fechaVencimientoRegistro;
	}

	public User getUsuarioSistema() {
		return usuarioSistema;
	}

	public void setUsuarioSistema(User usuarioSistema) {
		this.usuarioSistema = usuarioSistema;
	}
}
