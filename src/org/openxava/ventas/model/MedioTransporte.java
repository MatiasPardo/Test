package org.openxava.ventas.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;

@Entity

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@View(members="codigo, activo;" + 
		"nombre;" +
		"cuit, contacto;" +
		"telefono, celular, email;" +
		"domicilio;" +
		"horario;" 
		)

public class MedioTransporte extends ObjetoEstatico{
	
	@Column(length=20) @Required
	private String cuit;
		
	@ManyToOne(optional=true, fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
    @NoSearch
    @ReferenceView("Observaciones")
    @AsEmbedded
    private Domicilio domicilio;
	
	@Column(length=20) 
	private String telefono;
	
	@Column(length=20) 
	private String celular;
	
	@Column(length=40)
	@Stereotype("EMAIL")
	private String email;
	
	@Column(length=50)
	@DisplaySize(value=20)
	private String contacto;
	
	@Column(length=50)
	private String horario;
	
	public String getCuit() {
		return cuit;
	}

	public void setCuit(String cuit) {
		this.cuit = cuit;
	}

	public Domicilio getDomicilio() {
		return domicilio;
	}

	public void setDomicilio(Domicilio domicilio) {
		this.domicilio = domicilio;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getCelular() {
		return celular;
	}

	public void setCelular(String celular) {
		this.celular = celular;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContacto() {
		return contacto;
	}

	public void setContacto(String contacto) {
		this.contacto = contacto;
	}

	public String getHorario() {
		return horario;
	}

	public void setHorario(String horario) {
		this.horario = horario;
	}
}
