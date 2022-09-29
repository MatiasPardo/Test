package org.openxava.ventas.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;

@Entity

@Views({
	@View(
		members="codigo, principal;" +
				"nombre;" + 
				"domicilio;" +
				"horario, zona, medioTransporte, frecuencia"), 
	@View(name="Reparto",
		members="domicilio;" +
				"horario, zona, medioTransporte, frecuencia"),
})

@Tabs({
	@Tab(properties="codigo, nombre, principal, activo, domicilio.direccion, domicilio.ciudad.ciudad, horario, zona.codigo, zona.nombre, medioTransporte.nombre"
		)
})

public class LugarEntregaMercaderia extends ObjetoNegocio{
	
	@Column(length=50) @Required
	@SearchKey
    private String codigo;
	
	@Column(length=100) @Required
    private String nombre;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean activo = true;
	
	@ReadOnly
	@ReferenceView("Simple")
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private Cliente cliente;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean principal = Boolean.FALSE;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
    @NoSearch
    @ReferenceView("Observaciones")
    @AsEmbedded
    private Domicilio domicilio;
	
	@Column(length=50)
	private String horario;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	private Zona zona;
	
	@NoModify
	@NoCreate
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	private MedioTransporte medioTransporte;

	@NoModify
	@NoCreate
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private FrecuenciaEntrega frecuencia;
	
	public Domicilio getDomicilio() {
		return domicilio;
	}

	public void setDomicilio(Domicilio domicilio) {
		this.domicilio = domicilio;
	}

	public String getHorario() {
		return horario;
	}

	public void setHorario(String horario) {
		this.horario = horario;
	}

	public Zona getZona() {
		return zona;
	}

	public void setZona(Zona zona) {
		this.zona = zona;
	}

	public MedioTransporte getMedioTransporte() {
		return medioTransporte;
	}

	public void setMedioTransporte(MedioTransporte medioTransporte) {
		this.medioTransporte = medioTransporte;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}
	
	@Override
	public void onPreDelete(){
		super.onPreDelete();
		if (this.getCliente() != null){
			if (this.getCliente().getDomicilioLegal() != null){
				if (this.getCliente().getDomicilioLegal().equals(this)){
					throw new ValidationException("No se puede borrar el domicilio legal");
				}
			}
		}
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public FrecuenciaEntrega getFrecuencia() {
		return frecuencia;
	}

	public void setFrecuencia(FrecuenciaEntrega frecuencia) {
		this.frecuencia = frecuencia;
	}
}
