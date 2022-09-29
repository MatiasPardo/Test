package org.openxava.negocio.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.validators.*;
import org.openxava.util.*;

@Entity

@EntityValidators({
	@EntityValidator(
		value=PrincipalValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="modelo", value="Moneda"),
			@PropertyValue(name="principal")
		}
	),
})
	
public class Moneda extends ObjetoNegocio{
	
	@Column(length=100) @Required
	@SearchKey
	private String nombre;
	
	@Column(length=10) @Required
    private String simbolo;
	
	private Boolean principal = Boolean.FALSE;
	
	@Column(length=3) 
	@ReadOnly
	private String codigoAfip;
	
	private Integer orden = 0;
	
	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	@Override
	public Boolean soloLectura(){
		Boolean soloLectura = super.soloLectura();
		if (!soloLectura){
			if (!Is.emptyString(this.getId())){
				soloLectura = Boolean.TRUE;
			}
		}
		return soloLectura;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getSimbolo() {
		return simbolo;
	}

	public void setSimbolo(String simbolo) {
		this.simbolo = simbolo;
	}

	public String getCodigoAfip() {
		return codigoAfip;
	}

	public void setCodigoAfip(String codigoAfip) {
		this.codigoAfip = codigoAfip;
	}
			
	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		if (orden != null){
			this.orden = orden;
		}
	}

	@Override
	public String toString(){
		if (!Is.emptyString(this.getNombre())){
			return this.getNombre();
		}
		else{
			return super.toString();
		}
	}
}
