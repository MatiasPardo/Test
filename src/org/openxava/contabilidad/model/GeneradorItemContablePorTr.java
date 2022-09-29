package org.openxava.contabilidad.model;

import java.math.*;

import org.openxava.base.model.*;

public class GeneradorItemContablePorTr implements IGeneradorItemContable{

	private CuentaContable cuenta;
	
	private Transaccion transaccion;
	
	private BigDecimal debe = BigDecimal.ZERO;
	
	private BigDecimal haber = BigDecimal.ZERO;
	
	private CentroCostos centroCostos;
	
	public GeneradorItemContablePorTr(Transaccion tr, CuentaContable cuenta){
		this.transaccion = tr;
		this.cuenta = cuenta;
	}
		
	@Override
	public CuentaContable igcCuentaContable() {
		return this.cuenta;
	}

	@Override
	public BigDecimal igcHaberOriginal() {		
		return this.haber;
	}

	@Override
	public BigDecimal igcDebeOriginal() {
		return this.debe;
	}

	@Override
	public CentroCostos igcCentroCostos() {
		return this.centroCostos;
	}

	@Override
	public UnidadNegocio igcUnidadNegocio() {
		return null;
	}

	@Override
	public String igcDetalle() {
		return "";
	}
	
	public void setDebe(BigDecimal importe){
		if (importe != null){
			this.debe = importe;
			if (importe.compareTo(BigDecimal.ZERO) != 0){
				this.haber = BigDecimal.ZERO;
			}
		}
	}

	public void setHaber(BigDecimal importe){
		if (importe != null){
			this.haber = importe;
			if (importe.compareTo(BigDecimal.ZERO) != 0){
				this.debe = BigDecimal.ZERO;
			}
		}
	}

	public Transaccion getTransaccion(){
		return this.transaccion;
	}
	
	public void setCentroCostos(CentroCostos centroCostos){
		this.centroCostos = centroCostos;
	}
}
