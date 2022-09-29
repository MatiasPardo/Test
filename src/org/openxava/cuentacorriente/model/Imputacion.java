package org.openxava.cuentacorriente.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;

@MappedSuperclass

public abstract class Imputacion extends Transaccion{

	public static void imputarComprobantes(List<CuentaCorriente> listadoCtaCte, List<Imputacion> imputacionesGeneradas){
		OperadorComercial operador = null;
		Empresa empresa = null;
		
		List<CuentaCorriente> facturas = new LinkedList<CuentaCorriente>();
		List<CuentaCorriente> recibos = new LinkedList<CuentaCorriente>();
		for(CuentaCorriente ctacte: listadoCtaCte){
			if (ctacte.getAnulado()){
				throw new ValidationException(ctacte.toString() + " anulado");
			}
			else if (ctacte.saldado()){
				throw new ValidationException(ctacte.toString() + " saldado");
			}	
			
			if (operador == null){
				operador = ctacte.operadorCtaCte();
				empresa = ctacte.getEmpresa();
			}
			else{
				if (!operador.equals(ctacte.operadorCtaCte())){
					throw new ValidationException("Debe coincidir los destinatarios: " + operador.toString() + " - " + ctacte.operadorCtaCte().toString());
				}
				else if(!empresa.equals(ctacte.getEmpresa())){
					throw new ValidationException("Debe coincidir las empresas");
				}
			}
			
			if (ctacte.ingresa()){
				facturas.add(ctacte);
			}
			else{
				recibos.add(ctacte);
			}
		}
		
		if (facturas.isEmpty()){
			throw new ValidationException("No selecciono ninguna factura/débito");
		}
		else if (recibos.isEmpty()){
			throw new ValidationException("No seleccionó ningún recibo/crédito");
		}
		
		facturas.sort(new ComparatorCuentaCorrientePorFecha());
		recibos.sort(new ComparatorCuentaCorrientePorFecha());
		
		while (!facturas.isEmpty() && !recibos.isEmpty()){
			CuentaCorriente factura = facturas.get(0);
			CuentaCorriente recibo = recibos.get(0);
			
			Imputacion imputacion = factura.crearImputacion(factura, recibo);
			imputacionesGeneradas.add(imputacion);
			imputacion.setFecha(new Date());
			imputacion.setEmpresa(empresa);
			
			XPersistence.getManager().persist(imputacion);
			imputacion.confirmarTransaccion();
			
			boolean imputado = false;
			if (factura.saldadoMonedaOriginal()){
				facturas.remove(0);
				imputado = true;
			}
			if (recibo.saldadoMonedaOriginal() || recibo.saldadoEnMoneda(factura.getMonedaOriginal())){
				recibos.remove(0);
				imputado = true;
			}
			
			if (!imputado){
				throw new ValidationException("Se imputaron " + factura.toString() + " con " + recibo.toString() + " y ambos quedaron con saldo");
			}
		}			
	}
	
	@ReadOnly
	private BigDecimal importe;
	
	@ReadOnly @Hidden
	private BigDecimal importe1;
	
	@ReadOnly @Hidden
	private BigDecimal importe2;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre")
	private Moneda monedaImputacion;
	
	@ReadOnly
	private BigDecimal diferenciaCambio = BigDecimal.ZERO;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre")
	private Moneda monedaDifCambio;
	
	@ReadOnly
	@Hidden
	@Column(length=32)
	private String generadaPor;
	
	@ReadOnly
	@Hidden
	@Column(length=100)
	private String tipoEntidad;
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Imputación";
	}
	
	public BigDecimal getImporte() {
		return importe == null ? BigDecimal.ZERO : importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public BigDecimal getImporte1() {
		return importe1 == null ? BigDecimal.ZERO : importe1;
	}

	public void setImporte1(BigDecimal importe1) {
		this.importe1 = importe1;
	}

	public BigDecimal getImporte2() {
		return importe2 == null ? BigDecimal.ZERO : importe2;
	}

	public void setImporte2(BigDecimal importe2) {
		this.importe2 = importe2;
	}

	
	public Moneda getMonedaImputacion() {
		return monedaImputacion;
	}

	public void setMonedaImputacion(Moneda monedaImputacion) {
		this.monedaImputacion = monedaImputacion;
	}

	public BigDecimal getDiferenciaCambio() {
		return diferenciaCambio == null ? BigDecimal.ZERO : this.diferenciaCambio;
	}

	public void setDiferenciaCambio(BigDecimal diferenciaCambio) {
		this.diferenciaCambio = diferenciaCambio;
	}

	public Moneda getMonedaDifCambio() {
		return monedaDifCambio;
	}

	public void setMonedaDifCambio(Moneda monedaDifCambio) {
		this.monedaDifCambio = monedaDifCambio;
	}
		
	public String getGeneradaPor() {
		return generadaPor;
	}

	public void setGeneradaPor(String generadaPor) {
		this.generadaPor = generadaPor;
	}

	public String getTipoEntidad() {
		return tipoEntidad;
	}

	public void setTipoEntidad(String tipoEntidad) {
		this.tipoEntidad = tipoEntidad;
	}

	public abstract CuentaCorriente comprobanteOrigen();
	
	public abstract CuentaCorriente comprobanteDestino();
	
	public abstract OperadorComercial operadorCtaCte();
	
	@Override
	protected void posConfirmarTransaccion(){
		super.posConfirmarTransaccion();
		
		this.imputar();
		
		this.registrarTrazabilidadImputacion();
	}
	
	private void imputar() {
		if (this.comprobanteOrigen().saldado()){
			throw new ValidationException(this.comprobanteOrigen().toString() + " ya esta saldado");
		}
		if (this.comprobanteDestino().saldado()){
			throw new ValidationException(this.comprobanteDestino().toString() + " ya esta saldado");
		}
		
		if ((!this.comprobanteOrigen().getDiferenciaCambio()) && (!this.comprobanteDestino().getDiferenciaCambio())){
			if (this.comprobanteOrigen().saldadoMonedaOriginal()){
				throw new ValidationException(this.comprobanteOrigen().toString() + " ya esta saldado");
			}
			else if (this.comprobanteDestino().saldadoMonedaOriginal()){
				throw new ValidationException(this.comprobanteDestino().toString() + " ya esta saldado");
			}
			else{
				imputarComprobantes();
			}
		}
		else if (this.comprobanteOrigen().getDiferenciaCambio() && (!this.comprobanteDestino().getDiferenciaCambio())){
			imputarDiferenciaCambio();
		}
		else if (!this.comprobanteOrigen().getDiferenciaCambio() && (this.comprobanteDestino().getDiferenciaCambio())){
			imputarDiferenciaCambio();
		}
		else{
			throw new ValidationException("No se puede imputar dos diferencias de cambio");
		}
	}
	
	private void imputarDiferenciaCambio(){
		CuentaCorriente generadoPorDiferenciaCambio = null;
		CuentaCorriente comprobante = null;
		if (this.comprobanteOrigen().getDiferenciaCambio()){
			generadoPorDiferenciaCambio = this.comprobanteOrigen();
			comprobante = this.comprobanteDestino();
		}
		else{
			generadoPorDiferenciaCambio = this.comprobanteDestino();
			comprobante = this.comprobanteOrigen();
		}
		
		Moneda monedaDiferenciaCambio = generadoPorDiferenciaCambio.getMonedaOriginal();
		BigDecimal importeDifCambio = generadoPorDiferenciaCambio.getImporteOriginal();
		BigDecimal importeAbsotuloDifCambio = generadoPorDiferenciaCambio.getImporteOriginal().abs();		
		if (comprobante.getMoneda1().equals(monedaDiferenciaCambio)){
			if (comprobante.getSaldo1().abs().compareTo(importeAbsotuloDifCambio) >= 0){
				comprobante.setSaldo1(comprobante.getSaldo1().add(importeDifCambio));
				generadoPorDiferenciaCambio.setSaldo1(BigDecimal.ZERO);
			}
		}
		else if (comprobante.getMoneda2().equals(monedaDiferenciaCambio)){
			if (comprobante.getSaldo2().abs().compareTo(importeAbsotuloDifCambio) >= 0){
				comprobante.setSaldo2(comprobante.getSaldo2().add(importeDifCambio));
				generadoPorDiferenciaCambio.setSaldo2(BigDecimal.ZERO);
			}
		}
		else{
			throw new ValidationException("No puede ser moneda de diferencia de cambio " + monedaDiferenciaCambio.toString());
		}
		
		// se actualiza el item de imputación
		actualizarDespuesImputar(monedaDiferenciaCambio, importeAbsotuloDifCambio, null, BigDecimal.ZERO, true);
	}
	
	private void imputarComprobantes(){
		Moneda monedaImputacion = this.comprobanteOrigen().getMonedaOriginal();
		Moneda moneda1 = this.getEmpresa().getMoneda1();
		Moneda moneda2 = this.getEmpresa().getMoneda2();
		BigDecimal importeAImputar;
		Moneda monedaDifCambio = null;
		BigDecimal difCambio = BigDecimal.ZERO;
				
		if (!this.comprobanteOrigen().tercerMoneda() && !this.comprobanteDestino().tercerMoneda()){
					
			// Los comprobantes son expresados en moneda 1 o moneda 2 (Pesos o dólares)
			BigDecimal saldoOrigen = this.comprobanteOrigen().saldoEnMoneda(monedaImputacion).abs();
			BigDecimal saldoDestino = this.comprobanteDestino().saldoEnMoneda(monedaImputacion).abs();
			
			if (saldoOrigen.compareTo(BigDecimal.ZERO) == 0){
				throw new ValidationException(this.comprobanteOrigen().toString() + " ya esta saldado");
			}
			if (saldoDestino.compareTo(BigDecimal.ZERO) == 0){
				throw new ValidationException(this.comprobanteDestino().toString() + " ya esta saldado");
			}
			
			boolean imputacionSaldaOrigen = true;
			importeAImputar = saldoOrigen;
			if (saldoDestino.compareTo(saldoOrigen) < 0){
				importeAImputar = saldoDestino;
				imputacionSaldaOrigen = false;
			}
						
			if (monedaImputacion.equals(moneda1)){
				// Ejemplo: PESOS vs DOLARES o PESOS vs PESOS
				this.comprobanteOrigen().setSaldo1(this.comprobanteOrigen().getSaldo1().subtract(importeAImputar));
				this.comprobanteDestino().setSaldo1(this.comprobanteDestino().getSaldo1().add(importeAImputar));
												
				this.setImporte1(importeAImputar);
				
				// fecha 11/05/2020
				// No se puede utilizar los saldos, porque las diferencias de cambio pueden no estar aplicadas
				// Si la moneda es la misma, no se convierte pare evitar problemas de redondeo
				BigDecimal imputadoCotizadoOrigen = null;
				BigDecimal imputadoCotizadoDestino = null;
				if (imputacionSaldaOrigen){
					imputadoCotizadoOrigen = importeAImputar.divide(this.comprobanteOrigen().getCotizacion2(), 2, RoundingMode.HALF_EVEN);
					imputadoCotizadoDestino = importeAImputar.divide(this.comprobanteDestino().getCotizacion2(), 2, RoundingMode.HALF_EVEN);
				}
				else{				
					if (this.comprobanteDestino().getMonedaOriginal().equals(moneda2)){					
						imputadoCotizadoDestino = importeAImputar.multiply(this.comprobanteDestino().getImporteOriginal()).divide(this.comprobanteDestino().getImporte1(), 2, RoundingMode.HALF_EVEN);
						imputadoCotizadoOrigen = imputadoCotizadoDestino.multiply(this.comprobanteDestino().getCotizacion2()).divide(this.comprobanteOrigen().getCotizacion2(), 2, RoundingMode.HALF_EVEN);
					}
					else{
						imputadoCotizadoDestino = importeAImputar.divide(this.comprobanteDestino().getCotizacion2(), 2, RoundingMode.HALF_EVEN);
						imputadoCotizadoOrigen = importeAImputar.divide(this.comprobanteOrigen().getCotizacion2(), 2, RoundingMode.HALF_EVEN);
					}				
				}
				difCambio = imputadoCotizadoDestino.subtract(imputadoCotizadoOrigen);
				
				BigDecimal importeAImputarMoneda2 = imputadoCotizadoOrigen;
				int comparacion = difCambio.compareTo(BigDecimal.ZERO); 
				if (comparacion != 0){
					// hay diferencia de cambio
					// La diferencia de cambio en moneda 2 se aplica directamente, ya que no se genera el credito/debito
					monedaDifCambio = moneda2;				
					if (comparacion < 0){
						// es un crédito por diferencia de cambio, se debe tomar el importe cotizado destino, la diferencia de cambio es el restante 
						importeAImputarMoneda2 = imputadoCotizadoDestino;
						
						// se aplica al la factura
						this.comprobanteOrigen().setSaldo2(this.comprobanteOrigen().getSaldo2().add(difCambio));
					}
					else{
						// es un débito por dif cambio, se aplica al recibo
						this.comprobanteDestino().setSaldo2(this.comprobanteDestino().getSaldo2().add(difCambio));
					}
				}
				this.comprobanteOrigen().setSaldo2(this.comprobanteOrigen().getSaldo2().subtract(importeAImputarMoneda2));
				this.comprobanteDestino().setSaldo2(this.comprobanteDestino().getSaldo2().add(importeAImputarMoneda2));
				
				this.setImporte2(importeAImputarMoneda2);
				
			}
			else if (monedaImputacion.equals(moneda2)){
				// Ejemplo: DOLARES vs PESOS o DOLARES vs DOLARES
				this.comprobanteOrigen().setSaldo2(this.comprobanteOrigen().getSaldo2().subtract(importeAImputar));
				this.comprobanteDestino().setSaldo2(this.comprobanteDestino().getSaldo2().add(importeAImputar));
							
				this.setImporte2(importeAImputar);
				
				// fecha 11/05/2020
				// No se puede utilizar los saldos, porque las diferencias de cambio pueden no estar aplicadas
				// Si la moneda es la misma, no se convierte pare evitar problemas de redondeo				
				BigDecimal imputadoCotizadoOrigen = null;
				BigDecimal imputadoCotizadoDestino = null;
				if (imputacionSaldaOrigen){
					imputadoCotizadoOrigen = importeAImputar.multiply(this.comprobanteOrigen().getCotizacion2()).setScale(2, RoundingMode.HALF_EVEN);
					imputadoCotizadoDestino = importeAImputar.multiply(this.comprobanteDestino().getCotizacion2()).setScale(2, RoundingMode.HALF_EVEN);
				}
				else{
					if (this.comprobanteDestino().getMonedaOriginal().equals(moneda1)){					
						imputadoCotizadoDestino = importeAImputar.multiply(this.comprobanteDestino().getImporteOriginal()).divide(this.comprobanteDestino().getImporte2(), 2, RoundingMode.HALF_EVEN);
						imputadoCotizadoOrigen = imputadoCotizadoDestino.divide(this.comprobanteDestino().getCotizacion2(), 8, RoundingMode.HALF_EVEN).multiply(this.comprobanteOrigen().getCotizacion2()).setScale(2, RoundingMode.HALF_EVEN);
					}
					else{
						imputadoCotizadoDestino = importeAImputar.multiply(this.comprobanteDestino().getCotizacion2()).setScale(2, RoundingMode.HALF_EVEN);
						imputadoCotizadoOrigen = importeAImputar.multiply(this.comprobanteOrigen().getCotizacion2()).setScale(2, RoundingMode.HALF_EVEN);
					}				
				}	
				difCambio = imputadoCotizadoDestino.subtract(imputadoCotizadoOrigen);
				
				BigDecimal importeAImputarMoneda1 = imputadoCotizadoOrigen;
				int comparacion = difCambio.compareTo(BigDecimal.ZERO); 
				if (comparacion != 0){
					// hay diferencia de cambio
					monedaDifCambio = moneda1;				
					if (comparacion < 0){
						// es un crédito por diferencia de cambio, se debe tomar el importe cotizado destino, la diferencia de cambio es el restante 
						importeAImputarMoneda1 = imputadoCotizadoDestino;
					}		
				}
				this.comprobanteOrigen().setSaldo1(this.comprobanteOrigen().getSaldo1().subtract(importeAImputarMoneda1));
				this.comprobanteDestino().setSaldo1(this.comprobanteDestino().getSaldo1().add(importeAImputarMoneda1));
				
				this.setImporte1(importeAImputarMoneda1);
				
				// se agregan la diferencia de cambio en pesos, porque todavía no están generadas, pero para que se pueda reflejar en el saldo 
				// si estos comprobantes se vuelven a imputar en un proceso de imputación masivo
				if (comparacion < 0){
					this.comprobanteOrigen().agregarDifenciasCambiosMoneda1NoImputadas(difCambio);
				}
				else if (comparacion > 0){
					this.comprobanteDestino().agregarDifenciasCambiosMoneda1NoImputadas(difCambio);
				}
			}
		}
		else{
			// TERCER MONEDA Ejemplo: EURO vs EURO
			
			// Solo se permite imputar misma comprobante de la misma moneda, porque: 
			// si se imputa EURO vs PESOS, podrían generarse diferencias de cambios para aplicar al recibo en pesos, que si no se aplican quedan mal los saldos.
			// Lo mismo sucede con PESOS vs EURO, pero al revés. 
			// en cambio EURO vs EURO, siempre se mantiene el saldo en euros
			if (!this.comprobanteDestino().getMonedaOriginal().equals(this.comprobanteOrigen().getMonedaOriginal())){				
				throw new ValidationException("El comprobante origen esta expresado en " + this.comprobanteOrigen().getMonedaOriginal().toString() + ". El comprobante origen esta expresado en " + this.comprobanteDestino().getMonedaOriginal().toString() + ". La moneda debe ser la misma");
			}
						
			BigDecimal saldoOrigen = this.comprobanteOrigen().saldoEnMoneda(monedaImputacion).abs();
			BigDecimal saldoDestino = this.comprobanteDestino().saldoEnMoneda(monedaImputacion).abs();
			if (saldoOrigen.compareTo(BigDecimal.ZERO) == 0){
				throw new ValidationException(this.comprobanteOrigen().toString() + " ya esta saldado");
			}
			if (saldoDestino.compareTo(BigDecimal.ZERO) == 0){
				throw new ValidationException(this.comprobanteDestino().toString() + " ya esta saldado");
			}
			
			importeAImputar = saldoOrigen;
			if (saldoDestino.compareTo(saldoOrigen) < 0){
				importeAImputar = saldoDestino;			
			}
			
			this.comprobanteOrigen().setSaldoOriginal(this.comprobanteOrigen().getSaldoOriginal().subtract(importeAImputar));
			this.comprobanteDestino().setSaldoOriginal(this.comprobanteDestino().getSaldoOriginal().add(importeAImputar));
				
			// moneda 1 (se calcula diferencia de cambio solo moneda contabilidad)
			BigDecimal imputadoCotizadoOrigen = importeAImputar.multiply(this.comprobanteOrigen().getCotizacion()).setScale(2, RoundingMode.HALF_EVEN);
			BigDecimal imputadoCotizadoDestino = importeAImputar.multiply(this.comprobanteDestino().getCotizacion()).setScale(2, RoundingMode.HALF_EVEN);
									
			difCambio = imputadoCotizadoDestino.subtract(imputadoCotizadoOrigen);
			
			BigDecimal importeAImputarMoneda1 = imputadoCotizadoOrigen;
			int comparacion = difCambio.compareTo(BigDecimal.ZERO); 
			if (comparacion != 0){
				// hay diferencia de cambio
				monedaDifCambio = moneda1;				
				if (comparacion < 0){
					// es un crédito por diferencia de cambio, se debe tomar el importe cotizado destino, la diferencia de cambio es el restante 
					importeAImputarMoneda1 = imputadoCotizadoDestino;
					this.comprobanteOrigen().agregarDifenciasCambiosMoneda1NoImputadas(difCambio);
				}
				else{
					this.comprobanteDestino().agregarDifenciasCambiosMoneda1NoImputadas(difCambio);
				}
			}
			this.comprobanteOrigen().setSaldo1(this.comprobanteOrigen().getSaldo1().subtract(importeAImputarMoneda1));
			this.comprobanteDestino().setSaldo1(this.comprobanteDestino().getSaldo1().add(importeAImputarMoneda1));
			
			this.setImporte1(importeAImputarMoneda1);
		}
		
		this.actualizarDespuesImputar(monedaImputacion, importeAImputar, monedaDifCambio, difCambio, false);
	}
	
	private void actualizarDespuesImputar(Moneda monedaImputacion, BigDecimal importeImputacion, Moneda monedaDifCambio, BigDecimal importeDifCambio, boolean imputacionPorDiferenciaCambio){
		this.setImporte(importeImputacion);	
		this.setMonedaImputacion(monedaImputacion);
		this.setMonedaDifCambio(monedaDifCambio);
		this.setDiferenciaCambio(importeDifCambio);
		this.posImputarComprobantes(imputacionPorDiferenciaCambio);
	}
	
	protected void posImputarComprobantes(boolean imputacionPorDiferenciaCambio){		
	}
		
	@Override
	public void grabarTransaccion(){
		super.grabarTransaccion();
		
		validarComprobanteCuentaCorriente(this.comprobanteOrigen());
		validarComprobanteCuentaCorriente(this.comprobanteDestino());
	}
	
	private void validarComprobanteCuentaCorriente(CuentaCorriente comprobante){
		if (!comprobante.getEmpresa().equals(this.getEmpresa())){
			throw new ValidationException("No coincide la empresa: " + comprobante.toString());
		}
		if (!comprobante.operadorCtaCte().equals(this.operadorCtaCte())){
			throw new ValidationException("No coincide el cliente: " + comprobante.toString());
		}
	}
	
	public void asignarGeneradaPor(ObjetoNegocio objeto){
		if (objeto != null){
			this.setGeneradaPor(objeto.getId());
			this.setTipoEntidad(objeto.getClass().getSimpleName());
		}
		else{
			this.setGeneradaPor(null);
			this.setTipoEntidad(null);
		}
	}
	
	@Override
	protected void posAnularTransaccion(){
		super.posAnularTransaccion();
		
		this.desimputar();
	}
	
	private void desimputar() {
		if ((!this.comprobanteOrigen().getDiferenciaCambio()) && (!this.comprobanteDestino().getDiferenciaCambio())){
			desimputarComprobantes();			
		}
		else if (this.comprobanteOrigen().getDiferenciaCambio() && (!this.comprobanteDestino().getDiferenciaCambio())){
			desimputarDiferenciaCambio();
		}
		else if (!this.comprobanteOrigen().getDiferenciaCambio() && (this.comprobanteDestino().getDiferenciaCambio())){
			desimputarDiferenciaCambio();
		}
		else{
			throw new ValidationException("No se puede imputar dos diferencias de cambio");
		}
	}
	
	private void desimputarComprobantes(){
		BigDecimal importeImputado1 = this.getImporte1();
		this.comprobanteOrigen().setSaldo1(this.comprobanteOrigen().getSaldo1().add(importeImputado1));
		this.comprobanteDestino().setSaldo1(this.comprobanteDestino().getSaldo1().subtract(importeImputado1));
				
		if (this.comprobanteOrigen().tercerMoneda()){
			this.comprobanteOrigen().setSaldoOriginal(this.comprobanteOrigen().getSaldoOriginal().add(this.getImporte()));
		}
		else{
			BigDecimal importeImputado2 = this.getImporte2();
			this.comprobanteOrigen().setSaldo2(this.comprobanteOrigen().getSaldo2().add(importeImputado2));			
		}
		
		if (this.comprobanteDestino().tercerMoneda()){
			this.comprobanteDestino().setSaldoOriginal(this.comprobanteDestino().getSaldoOriginal().subtract(this.getImporte()));
		}
		else{
			BigDecimal importeImputado2 = this.getImporte2();
			this.comprobanteDestino().setSaldo2(this.comprobanteDestino().getSaldo2().subtract(importeImputado2));
		}
		
		if (this.getMonedaImputacion().equals(this.getMoneda1())){
			// las diferencias en moneda2 se aplicaron directamente
			int compare = this.getDiferenciaCambio().compareTo(BigDecimal.ZERO);
			if (compare > 0){
				this.comprobanteDestino().setSaldo2(this.comprobanteDestino().getSaldo2().subtract(this.getDiferenciaCambio()));
			}
			else if (compare < 0){
				this.comprobanteOrigen().setSaldo2(this.comprobanteOrigen().getSaldo2().subtract(this.getDiferenciaCambio()));
			}
		}
	}
	
	private void desimputarDiferenciaCambio(){
		CuentaCorriente generadoPorDiferenciaCambio = null;
		CuentaCorriente comprobante = null;
		if (this.comprobanteOrigen().getDiferenciaCambio()){
			generadoPorDiferenciaCambio = this.comprobanteOrigen();
			comprobante = this.comprobanteDestino();
		}
		else{
			generadoPorDiferenciaCambio = this.comprobanteDestino();
			comprobante = this.comprobanteOrigen();
		}
		
		Moneda monedaDiferenciaCambio = generadoPorDiferenciaCambio.getMonedaOriginal();
		BigDecimal importeDifCambio = generadoPorDiferenciaCambio.getImporteOriginal();
		if (comprobante.getMoneda1().equals(monedaDiferenciaCambio)){
			// Falla con imputaciones parciales de diferencia de cambio
			//comprobante.setSaldo1(comprobante.getSaldo1().subtract(importeDifCambio));
			//generadoPorDiferenciaCambio.setSaldo1(importeDifCambio);
			
			this.comprobanteOrigen().setSaldo1(this.comprobanteOrigen().getSaldo1().add(this.getImporte()));
			this.comprobanteDestino().setSaldo1(this.comprobanteDestino().getSaldo1().subtract(this.getImporte()));
			
		}
		else if (comprobante.getMoneda2().equals(monedaDiferenciaCambio)){
			comprobante.setSaldo2(comprobante.getSaldo2().subtract(importeDifCambio));
			generadoPorDiferenciaCambio.setSaldo2(importeDifCambio);			
		}
		else{
			throw new ValidationException("No puede ser moneda de diferencia de cambio " + monedaDiferenciaCambio.toString());
		}
	}
	
	private void registrarTrazabilidadImputacion(){
		CuentaCorriente origen = this.comprobanteOrigen();
		CuentaCorriente destino = this.comprobanteDestino();
		Trazabilidad.crearTrazabilidad(origen.buscarTransaccion(), origen.getTipoEntidad(), destino.buscarTransaccion(), destino.getTipoEntidad());
	}
}
