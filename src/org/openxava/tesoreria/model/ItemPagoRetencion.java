package org.openxava.tesoreria.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.impuestos.model.*;

@Entity

@Views({
	@View(members=
		"impuesto;" + 
		"netoAcumulado, montoNoSujetoRetencion, netoGrabado;" +
		"alicuota, retencionTotal, retencionesAnteriores;" +
		"calculoManual, retencionActual;"
	)
})

@Tabs({
	@Tab(name="RetencionesConfirmadas",
		baseCondition="${pago.estado} in (1) and ${retencionActual} > 0",
		defaultOrder="${pago.fecha} desc",
		properties="pago.fecha, pago.numero, pago.proveedor.codigo, pago.proveedor.nombre, numero, impuesto.codigo, impuesto.nombre, retencionActual, netoAcumulado, montoNoSujetoRetencion, netoGrabado, alicuota, retencionTotal, retencionesAnteriores"),
	@Tab(baseCondition="${retencionActual} > 0",
		defaultOrder="${pago.fecha} desc",
		properties="pago.fecha, pago.estado, pago.numero, pago.proveedor.codigo, pago.proveedor.nombre, numero, impuesto.codigo, impuesto.nombre, retencionActual, netoAcumulado, montoNoSujetoRetencion, netoGrabado, alicuota, retencionTotal, retencionesAnteriores"),
})

public class ItemPagoRetencion extends ItemTransaccion{
	
	@Column(length=25)
	@ReadOnly
	private String numero;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private PagoProveedores pago;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)	
	@ReadOnly
	@ReferenceView("Simple")
	private Impuesto impuesto;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean calculoManual = Boolean.FALSE;
	
	private BigDecimal retencionActual;
	
	@ReadOnly
	private BigDecimal netoAcumulado;
	
	@ReadOnly
	private BigDecimal montoNoSujetoRetencion;
	
	@ReadOnly
	private BigDecimal netoGrabado;
	
	@ReadOnly
	private BigDecimal alicuota;
	
	@ReadOnly
	private BigDecimal retencionTotal;
	
	@ReadOnly
	private BigDecimal retencionesAnteriores;
	
	@Override
	public Transaccion transaccion() {
		return this.getPago();
	}

	@Override
	public void recalcular() {
		if (this.getPago() != null){
			this.getImpuesto().getTipo().calculadorImpuesto().calcular(this.getPago(), this, 0);
			this.calcularRetencionActual();
		}
		
	}
	
	public Boolean getCalculoManual() {
		return calculoManual == null ? Boolean.FALSE : calculoManual;
	}

	public void setCalculoManual(Boolean calculoManual) {
		this.calculoManual = calculoManual;
	}

	public PagoProveedores getPago() {
		return pago;
	}

	public void setPago(PagoProveedores pago) {
		this.pago = pago;
	}

	public Impuesto getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(Impuesto impuesto) {
		this.impuesto = impuesto;
	}

	public BigDecimal getRetencionActual() {		
		return retencionActual == null ? BigDecimal.ZERO : this.retencionActual;
	}

	public void setRetencionActual(BigDecimal retencionActual) {
		this.retencionActual = aplicarRedondeo(retencionActual);
	}

	public BigDecimal getNetoAcumulado() {
		return netoAcumulado == null ? BigDecimal.ZERO : this.netoAcumulado;
	}

	public void setNetoAcumulado(BigDecimal netoAcumulado) {
		this.netoAcumulado = netoAcumulado;
		calcularNetoGrabado();
	}

	public BigDecimal getMontoNoSujetoRetencion() {
		return montoNoSujetoRetencion == null ? BigDecimal.ZERO : this.montoNoSujetoRetencion;
	}

	public void setMontoNoSujetoRetencion(BigDecimal montoNoSujetoRetencion) {
		this.montoNoSujetoRetencion = montoNoSujetoRetencion;
		calcularNetoGrabado();
	}

	public BigDecimal getNetoGrabado() {
		calcularNetoGrabado();		
		return netoGrabado;
	}

	private boolean recalculaNetoGrabado(){
		boolean recalcula = true;
		if (this.getImpuesto() != null && this.getImpuesto().getTipo().equals(DefinicionImpuesto.RetencionMonotributo)){
			recalcula = false;
		}		
		return recalcula;
	}
	
	private void calcularNetoGrabado(){
		if (this.recalculaNetoGrabado()){
			BigDecimal neto = this.getNetoAcumulado().subtract(this.getMontoNoSujetoRetencion());
			if (neto.compareTo(BigDecimal.ZERO) < 0){
				neto = BigDecimal.ZERO;			
			}		
			this.netoGrabado = neto;
		}
	}
	
	public void setNetoGrabado(BigDecimal netoGrabado) {
		this.netoGrabado = netoGrabado;
	}

	public BigDecimal getAlicuota() {
		return alicuota == null ? BigDecimal.ZERO : this.alicuota;
	}

	public void setAlicuota(BigDecimal alicuota) {
		this.alicuota = alicuota;
	}

	public BigDecimal getRetencionTotal() {
		return retencionTotal == null ? BigDecimal.ZERO : this.retencionTotal;
	}

	public void setRetencionTotal(BigDecimal retencionTotal) {
		this.retencionTotal = aplicarRedondeo(retencionTotal);		
	}

	public BigDecimal getRetencionesAnteriores() {
		return retencionesAnteriores == null ? BigDecimal.ZERO : this.retencionesAnteriores;
	}

	public void setRetencionesAnteriores(BigDecimal retencionesAnteriores) {
		this.retencionesAnteriores = retencionesAnteriores;		
	}
	
	public void calcularRetencionActual(){
		if (!this.getCalculoManual()){
			this.retencionActual = this.getRetencionTotal().subtract(this.getRetencionesAnteriores());
			if (this.retencionActual.compareTo(BigDecimal.ZERO) < 0){
				this.retencionActual = BigDecimal.ZERO;
			}
		}
	}
	
	public void asignarNumeracion(String numeracion, Long numero){
		this.setNumero(numeracion);
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}
	
	@Override
	protected void onPrePersist(){
		super.onPrePersist();
		this.calcularRetencionActual();
	}

	@Override
	protected void onPreUpdate(){
		super.onPreUpdate();
		this.calcularRetencionActual();
	}
	
	private BigDecimal aplicarRedondeo(BigDecimal importe){
		if (importe != null){
			return importe.setScale(2, RoundingMode.HALF_EVEN);
		}
		else{
			return importe;
		}
	}
}
