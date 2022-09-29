package org.openxava.tesoreria.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.FalseCalculator;
import org.openxava.contabilidad.model.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(name="Simple", members="nombre")
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

public class TipoValorConfiguracion extends ObjetoEstatico{
	
	@Required
	private TipoValor comportamiento;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoModify
	@NoCreate
	@DescriptionsList(descriptionProperties="nombre")
	private Moneda moneda;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean permitirNegativo = Boolean.FALSE;
	
	@ReadOnly
	@Hidden
	private boolean consolidaAutomaticamente = false;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private CuentaContable cuentaContableFinanzas;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre",
				condition="${tipo} = 1")
	@NoCreate @NoModify
	private Producto conceptoChequeRechazado;
	
	@Column(length=15)
	private String proveedorTarjeta;
	
	public TipoValor getComportamiento() {
		return comportamiento;
	}

	public void setComportamiento(TipoValor comportamiento) {
		this.comportamiento = comportamiento;
		if (comportamiento != null){
			this.consolidaAutomaticamente = comportamiento.consolidaAutomaticamente();
		}
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}
	
	public CuentaContable getCuentaContableFinanzas() {
		return cuentaContableFinanzas;
	}

	public void setCuentaContableFinanzas(CuentaContable cuentaContableFinanzas) {
		this.cuentaContableFinanzas = cuentaContableFinanzas;
	}

	@Override 
	public Boolean soloLectura(){
		// Después de crearse, no puede ser modificado
		return Boolean.TRUE;
	}

	public TipoValorConfiguracion consolidaCon(Tesoreria tesoreria) {
		if (this.getComportamiento().equals(TipoValor.Efectivo) || 
			this.getComportamiento().equals(TipoValor.TarjetaCreditoCobranza) ){
			return this;
		}
		else{
			return tesoreria.consolidaCon(this);			
		}
	}

	public boolean getConsolidaAutomaticamente() {
		if (this.getComportamiento() != null){
			return this.getComportamiento().consolidaAutomaticamente();
		}
		else{
			return consolidaAutomaticamente;
		}
	}

	public void setConsolidaAutomaticamente(boolean consolidaAutomaticamente) {
		if (this.getComportamiento() != null){
			this.consolidaAutomaticamente = this.getComportamiento().consolidaAutomaticamente();
		}
		else{
			this.consolidaAutomaticamente = consolidaAutomaticamente;
		}
	}

	public Producto getConceptoChequeRechazado() {
		return conceptoChequeRechazado;
	}

	public void setConceptoChequeRechazado(Producto conceptoChequeRechazado) {
		this.conceptoChequeRechazado = conceptoChequeRechazado;
	}
	
	public Boolean getPermitirNegativo() {
		return permitirNegativo == null ? Boolean.TRUE : this.permitirNegativo;
	}

	public void setPermitirNegativo(Boolean permitirNegativo) {
		if (permitirNegativo != null){
			this.permitirNegativo = permitirNegativo;
		}
	}
	
	@Override
	protected void onPrePersist() {
		super.onPrePersist();
		
		if (this.getComportamiento().equals(TipoValor.ChequePropioAutomatico)){
			throw new ValidationException("Falta implementar el comportamiento" + this.getComportamiento().toString());
		}
	}

	public String getProveedorTarjeta() {
		return proveedorTarjeta;
	}

	public void setProveedorTarjeta(String proveedorTarjeta) {
		this.proveedorTarjeta = proveedorTarjeta;
	}

}
