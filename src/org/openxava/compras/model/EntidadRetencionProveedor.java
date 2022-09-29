package org.openxava.compras.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.impuestos.model.*;
import org.openxava.validators.ValidationException;

@Entity

public class EntidadRetencionProveedor extends ObjetoNegocio{
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	private Proveedor proveedor;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean calcula = Boolean.TRUE;

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(
					descriptionProperties="codigo, nombre", 
					condition="${tipo} IN (3, 11, 12)")
	private Impuesto impuesto;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(
			depends="this.impuesto",
			descriptionProperties="codigo, nombre", 
			condition="${impuesto.id} = ?")
	private AlicuotaImpuesto alicuota;

	public Boolean getCalcula() {
		return calcula == null ? Boolean.FALSE : calcula;
	}

	public void setCalcula(Boolean calcula) {
		this.calcula = calcula;
	}

	public Impuesto getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(Impuesto impuesto) {
		this.impuesto = impuesto;
	}

	public AlicuotaImpuesto getAlicuota() {
		return alicuota;
	}

	public void setAlicuota(AlicuotaImpuesto alicuota) {
		this.alicuota = alicuota;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
	
	@Override
	protected void onPrePersist() {
		super.onPrePersist();
		
		this.validarAlicuota();
	}
	
	@Override
	protected void onPreUpdate() {
		super.onPreUpdate();
		
		this.validarAlicuota();
	}
	
	private void validarAlicuota(){
		if (this.getImpuesto() != null){
			if (this.getImpuesto().getTipo().equals(DefinicionImpuesto.RetencionMonotributo)){
				if (this.getAlicuota() != null){
					throw new ValidationException("Retencion Monotributista no usa alicuota");
				}
			}
			else{
				if (this.getAlicuota() == null){
					throw new ValidationException("Falta asignar alicuota");
				}
			}
		}
	}
}
