package org.openxava.ventas.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;

import org.openxava.annotations.Action;
import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.EntityValidator;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.PropertyValue;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.Required;
import org.openxava.annotations.Tab;
import org.openxava.annotations.Tabs;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.actions.Auditoria;
import org.openxava.base.model.ConfiguracionEntidad;
import org.openxava.base.model.ObjetoEstatico;
import org.openxava.calculators.FalseCalculator;
import org.openxava.calculators.TrueCalculator;
import org.openxava.calculators.ZeroBigDecimalCalculator;
import org.openxava.negocio.model.TipoPorcentaje;
import org.openxava.tesoreria.model.TipoValorConfiguracion;
import org.openxava.ventas.validators.CondicionVentaValidator;

@Entity

@Views({
	@View(members="Principal[codigo, activo; nombre; ventas, compras, principal; medioPago];" + 
				"Precios[tipo, porcentaje];" +
				"Financiacion[porcentajeInteres];" + 
				"Auditoria[usuario, fechaCreacion; auditoria];"),	
	@View(name="Simple", members="codigo, nombre"), 
	@View(name="CambioTipo", members="tipo"),
	@View(name="CambioPorcentaje", members="porcentaje")
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@EntityValidator(
		value=CondicionVentaValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="compras"),
			@PropertyValue(name="ventas"),
			@PropertyValue(name="principal")
		}
	)

public class CondicionVenta extends ObjetoEstatico{
	
	@DefaultValueCalculator(FalseCalculator.class)
	private Boolean principal = false;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean ventas = Boolean.TRUE;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean compras = Boolean.TRUE;
	
	@Required
	@Action(value="ModificarAtributoConAuditoria.Cambiar", alwaysEnabled=true, notForViews="CambioTipo")
	private TipoPorcentaje tipo;
	
	@Min(value=0, message="No puede menor a 0")
	@DefaultValueCalculator(value=ZeroBigDecimalCalculator.class)
	@Action(value="ModificarAtributoConAuditoria.Cambiar", alwaysEnabled=true, notForViews="CambioPorcentaje")
	private BigDecimal porcentaje = BigDecimal.ZERO;
	
	@Min(value=0, message="No puede menor a 0")
	@DefaultValueCalculator(value=ZeroBigDecimalCalculator.class)
	private BigDecimal porcentajeInteres = BigDecimal.ZERO;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private TipoValorConfiguracion medioPago;
	
	@ReadOnly
	@ListProperties("fechaCreacion, usuario, modificacion, valorAnterior, valorNuevo")
	public Collection<Auditoria> getAuditoria(){
		return Auditoria.registrosAuditoria(this);
	}
	
	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	public TipoPorcentaje getTipo() {
		return tipo;
	}

	public void setTipo(TipoPorcentaje tipo) {
		this.tipo = tipo;
	}

	public BigDecimal getPorcentaje() {
		return porcentaje == null ? BigDecimal.ZERO : this.porcentaje;
	}

	public void setPorcentaje(BigDecimal porcentaje) {
		if (porcentaje != null){
			this.porcentaje = porcentaje;
		}
	}
	
	public BigDecimal aplicarPorcentaje(BigDecimal importe){
		if (this.getPorcentaje().compareTo(BigDecimal.ZERO) != 0){
			if (this.getTipo().equals(TipoPorcentaje.Incremento)){
				return importe.add(importe.multiply(this.getPorcentaje()).divide(new BigDecimal(100)));
			}
			else if (this.getTipo().equals(TipoPorcentaje.Descuento)){
				return importe.subtract(importe.multiply(this.getPorcentaje()).divide(new BigDecimal(100)));
			}
			else{
				return importe;
			}
		}
		else{
			return importe;
		}
	}
	
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		propiedadesSoloLectura.add("tipo");
		propiedadesSoloLectura.add("porcentaje");
	}	
	
	public void propiedadesSoloLecturaAlCrear(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion){
		super.propiedadesSoloLecturaAlCrear(propiedadesSoloLectura, propiedadesEditables, configuracion);
		propiedadesEditables.add("tipo");
		propiedadesEditables.add("porcentaje");
	}
	
	public Boolean getVentas() {
		return ventas;
	}

	public void setVentas(Boolean ventas) {
		this.ventas = ventas;
	}

	public Boolean getCompras() {
		return compras;
	}

	public void setCompras(Boolean compras) {
		this.compras = compras;
	}

	public BigDecimal getPorcentajeInteres() {
		return porcentajeInteres;
	}

	public void setPorcentajeInteres(BigDecimal porcentajeInteres) {
		if (porcentajeInteres == null){
			this.porcentajeInteres = BigDecimal.ZERO;
		}
		else{
			this.porcentajeInteres = porcentajeInteres;
		}
	}

	public TipoValorConfiguracion getMedioPago() {
		return medioPago;
	}

	public void setMedioPago(TipoValorConfiguracion medioPago) {
		this.medioPago = medioPago;
	}
}
