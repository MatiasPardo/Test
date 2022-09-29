package org.openxava.fisco.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.Required;
import org.openxava.annotations.View;

@Entity

@View(members="id, principal; tipo; regionalidad; regimenFacturacion")

public class TipoFacturacion {
	
	@ReadOnly
	@Hidden
	@Id
	private Integer id;
	
	@Required
	@ReadOnly
	@Column(length=15)
	private String tipo;
	
	@Required
	@Hidden
	@ReadOnly
	private Regionalidad regionalidad;
	
	@Required
	@Hidden
	@ReadOnly
	private RegimenFacturacionFiscal regimenFacturacion;

	@ReadOnly
	@Hidden
	private Boolean principal = false;  
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Regionalidad getRegionalidad() {
		return regionalidad;
	}

	public void setRegionalidad(Regionalidad regionalidad) {
		this.regionalidad = regionalidad;
	}

	public RegimenFacturacionFiscal getRegimenFacturacion() {
		return regimenFacturacion;
	}

	public void setRegimenFacturacion(RegimenFacturacionFiscal regimenFacturacion) {
		this.regimenFacturacion = regimenFacturacion;
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}
	
	public boolean calculaImpuestos(){
		return this.getRegimenFacturacion().getCalculaImpuestos();
	}
}
