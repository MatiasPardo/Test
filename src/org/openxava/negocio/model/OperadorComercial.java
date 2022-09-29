package org.openxava.negocio.model;


import javax.persistence.*;

import org.openxava.afip.model.*;
import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.impuestos.model.SituacionIngresosBrutos;
import org.openxava.negocio.calculators.*;
import org.openxava.util.Is;

@MappedSuperclass
public abstract class OperadorComercial extends ObjetoEstatico{
	
	@Required
	@DefaultValueCalculator(value=TipoDocumentoDefaultCalculator.class)
	private TipoDocumento tipoDocumento;
	
	@Column(length=20) @Required
	@Action("PersonaAfip.validarAfip")
	private String numeroDocumento;
	
	@DefaultValueCalculator(value=TipoPersonaAfipCalculators.class)
	private TipoPersonaAfip tipo;
	
	@Column(length=50)
	@DisplaySize(value=20)
	private String contacto;
	
	@Column(length=20) 
	private String telefono;
	
	@Column(length=50)
	@Stereotype("WEBURL")
	private String web;
	
	@Column(length=40)
	@Stereotype("EMAIL")
	private String mail1;
	
	@Column(length=150)
	@Stereotype("EMAIL")
	private String mail2;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
    @NoSearch
    @ReferenceView("Observaciones")
    @AsEmbedded
    private Domicilio domicilio;

	@Stereotype("MEMO")
    private String observaciones;
	
	@Column(length=50)
	private String numeroIIBB;
	
	private SituacionIngresosBrutos condicionIIBB;
	
	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		if (Is.emptyString(numeroDocumento)){
			this.numeroDocumento = numeroDocumento;
		}
		else{
			this.numeroDocumento = numeroDocumento.trim();
		}
	}

	public Domicilio getDomicilio() {
		return domicilio;
	}

	public void setDomicilio(Domicilio domicilio) {
		this.domicilio = domicilio;
	}

	public String getContacto() {
		return contacto;
	}

	public void setContacto(String contacto) {
		this.contacto = contacto;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getMail1() {
		return mail1;
	}

	public void setMail1(String mail1) {
		this.mail1 = mail1;
	}

	public String getMail2() {
		return mail2;
	}

	public void setMail2(String mail2) {
		this.mail2 = mail2;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public String getWeb() {
		return web;
	}

	public void setWeb(String web) {
		this.web = web;
	}

	public TipoPersonaAfip getTipo() {
		return tipo;
	}

	public void setTipo(TipoPersonaAfip tipo) {
		this.tipo = tipo;
	}
	
	public String getNumeroIIBB() {
		return numeroIIBB;
	}

	public void setNumeroIIBB(String numeroIIBB) {
		this.numeroIIBB = numeroIIBB;
	}

	public SituacionIngresosBrutos getCondicionIIBB() {
		return condicionIIBB;
	}

	public void setCondicionIIBB(SituacionIngresosBrutos condicionIIBB) {
		this.condicionIIBB = condicionIIBB;
	}
}
