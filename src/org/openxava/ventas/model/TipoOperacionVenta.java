package org.openxava.ventas.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.EntityValidator;
import org.openxava.annotations.EntityValidators;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.PropertyValue;
import org.openxava.annotations.Tab;
import org.openxava.annotations.Tabs;
import org.openxava.annotations.View;
import org.openxava.base.model.ConfiguracionEntidad;
import org.openxava.base.model.ObjetoEstatico;
import org.openxava.calculators.FalseCalculator;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.validators.PrincipalValidator;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.validators.TipoOperacionVentaValidator;

@Entity

@View(members="codigo, nombre;" +
		"principal, activo;" +
		"consignacion, facturaAnticipo, transferenciaSucursales;")

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@EntityValidators({
	@EntityValidator(value=PrincipalValidator.class, 
		properties={						
			@PropertyValue(name="modelo", value="TipoOperacionVenta"),
			@PropertyValue(name="idEntidad", from="id"),
			@PropertyValue(name="principal", from="principal")
		}),
	@EntityValidator(value=TipoOperacionVentaValidator.class, 
		properties={
			@PropertyValue(name="transferenciaSucursales"),
			@PropertyValue(name="principal"),
			@PropertyValue(name="idEntidad", from="id")
		})
})

public class TipoOperacionVenta extends ObjetoEstatico{

	public static TipoOperacionVenta buscarTipoOperacionPorConsignacionPcipal() {
		Query query = XPersistence.getManager().createQuery("from TipoOperacionVenta where consignacion = :consignacion and activo = :activo order by fechaCreacion asc");
		query.setParameter("consignacion", true);
		query.setParameter("activo", true);
		query.setMaxResults(1);
		query.setFlushMode(FlushModeType.COMMIT);
		List<?> results = query.getResultList();
		TipoOperacionVenta tipoOperacion = null;
		if (!results.isEmpty()){
			tipoOperacion = (TipoOperacionVenta)results.get(0);
		}
		return tipoOperacion;		
	}
	
	public static TipoOperacionVenta buscarTipoTransferenciaSucursales(){
		Query query = XPersistence.getManager().createQuery("from TipoOperacionVenta where transferenciaSucursales = 't'");
		query.setFlushMode(FlushModeType.COMMIT);
		query.setMaxResults(2);
		List<?> results = query.getResultList();
		if (results.isEmpty()){
			throw new ValidationException("Falta definir un tipo de operación de venta para transferencia entre sucursales");
		}
		else if (results.size() > 1){
			throw new ValidationException("Hay dos o más tipos de operacion de venta definidas para transferencia entre sucursales");
		}
		else{
			return (TipoOperacionVenta)results.get(0);
		}
	}
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	@Hidden
	private Boolean principal = Boolean.FALSE;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	@Hidden
	private Boolean consignacion = Boolean.FALSE;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	@Hidden
	private Boolean facturaAnticipo = Boolean.FALSE;
	
	@Hidden
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean transferenciaSucursales = Boolean.FALSE;
	
	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}
	
	public Boolean getConsignacion() {
		return consignacion;
	}

	public void setConsignacion(Boolean consignacion) {
		if (consignacion != null){
			this.consignacion = consignacion;
		}
	}

	public Boolean getFacturaAnticipo() {
		return facturaAnticipo;
	}

	public void setFacturaAnticipo(Boolean facturaAnticipo) {
		if (facturaAnticipo != null){
			this.facturaAnticipo = facturaAnticipo;
		}
	}
	
	public Boolean getTransferenciaSucursales() {
		return transferenciaSucursales == null ? Boolean.FALSE : this.transferenciaSucursales;
	}

	public void setTransferenciaSucursales(Boolean transferenciaSucursales) {
		this.transferenciaSucursales = transferenciaSucursales;
	}
	
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		propiedadesSoloLectura.add("consignacion");
		propiedadesSoloLectura.add("facturaAnticipo");
	}
	
	@Override
	public void propiedadesSoloLecturaAlCrear(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion){
		super.propiedadesSoloLecturaAlCrear(propiedadesSoloLectura, propiedadesEditables, configuracion);
		propiedadesEditables.add("consignacion");
		propiedadesEditables.add("facturaAnticipo");
	}
	
	@Override
	protected void onPrePersist() {
		super.onPrePersist();
		
		this.validarConfiguracion();
	}
	
	@Override
	protected void onPreUpdate() {
		super.onPreUpdate();
		
		this.validarConfiguracion();
	}
	
	private void validarConfiguracion() {
		if (this.getConsignacion() && this.getFacturaAnticipo()){
			throw new ValidationException("El tipo de operación no puede ser consignación y anticipo");
		}
		
	}
}
