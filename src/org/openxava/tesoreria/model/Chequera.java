package org.openxava.tesoreria.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.EntityValidator;
import org.openxava.annotations.EntityValidators;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.PropertyValue;
import org.openxava.annotations.Required;
import org.openxava.annotations.Tab;
import org.openxava.annotations.Tabs;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.ConfiguracionEntidad;
import org.openxava.base.model.ObjetoEstatico;
import org.openxava.calculators.LongCalculator;
import org.openxava.tesoreria.validators.ChequeraValidator;

@Entity

@Views({
	@View(name="Simple", members="codigo, nombre"),
	@View(members="codigo, nombre; activo;" + 
			"primerNumero, ultimoNumero;" + 
			"proximoNumero;" + 
			"cuenta;"),
	@View(name="Egreso", 
		members="codigo, nombre;" + 
		"proximoNumero, primerNumero, ultimoNumero;"),
})

@Tabs({
	@Tab(properties="codigo, nombre, proximoNumero, primerNumero, ultimoNumero, fechaCreacion, cuenta.codigo, cuenta.nombre",
		baseCondition=ObjetoEstatico.CONDITION_ACTIVOS), 
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@EntityValidators({
	@EntityValidator(value=ChequeraValidator.class, 
			properties= {
					@PropertyValue(name="primerNumero"),
					@PropertyValue(name="ultimoNumero"),
					@PropertyValue(name="proximoNumero"),
					@PropertyValue(name="cuenta"),
					@PropertyValue(name="id")
			})
})

public class Chequera extends ObjetoEstatico implements IChequera{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private CuentaBancaria cuenta;
	
	@Required
	private Long primerNumero;
	
	@Required
	private Long ultimoNumero;

	@Required
	@DefaultValueCalculator(value=LongCalculator.class, 
			properties={
					@PropertyValue(name="value", from="primerNumero")
			})
	private Long proximoNumero;
	
	public CuentaBancaria getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaBancaria cuenta) {
		this.cuenta = cuenta;
	}

	public Long getProximoNumero() {
		return proximoNumero;
	}

	public void setProximoNumero(Long proximoNumero) {
		this.proximoNumero = proximoNumero;
	}

	public Long getPrimerNumero() {
		return primerNumero;
	}

	public void setPrimerNumero(Long primerNumero) {
		this.primerNumero = primerNumero;
	}

	public Long getUltimoNumero() {
		return ultimoNumero;
	}

	public void setUltimoNumero(Long ultimoNumero) {
		this.ultimoNumero = ultimoNumero;
	}

	@Override
	@Hidden
	public Long getProximoNumeroChequera() {
		return this.getProximoNumero();
	}

	@Override
	public void setProximoNumeroChequera(Long proximoNumeroChequera) {
		this.setProximoNumero(proximoNumeroChequera);		
	}

	@Override
	@Hidden
	public Long getUltimoNumeroChequera() {
		return this.getUltimoNumero();
	}

	@Override
	@Hidden
	public Banco getBanco() {
		if (this.getCuenta() != null){
			return this.getCuenta().getBanco();
		}
		else{
			return null;
		}
	}
	
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesSoloLectura.add("cuenta");
	}
	
	@Override
	public void propiedadesSoloLecturaAlCrear(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion){
		super.propiedadesSoloLecturaAlCrear(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesEditables.add("cuenta");
	}
}
