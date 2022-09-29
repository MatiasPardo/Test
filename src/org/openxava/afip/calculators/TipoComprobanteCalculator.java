package org.openxava.afip.calculators;


import org.openxava.afip.model.*;
import org.openxava.calculators.*;
import org.openxava.fisco.model.TipoComprobante;
import org.openxava.impuestos.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.*;

@SuppressWarnings("serial")
public class TipoComprobanteCalculator implements ICalculator{
	
	private String codigoPosicionIVA;
	
	public String getCodigoPosicionIVA() {
		return codigoPosicionIVA;
	}

	public void setCodigoPosicionIVA(String codigoPosicionIVA) {
		this.codigoPosicionIVA = codigoPosicionIVA;
	}
	
	private PosicionAnteImpuesto posicion;
	
	public PosicionAnteImpuesto getPosicion() {
		if (posicion == null){
			if (!Is.emptyString(this.getCodigoPosicionIVA())){
				this.posicion = XPersistence.getManager().find(PosicionAnteImpuesto.class, this.getCodigoPosicionIVA());
			}
		}
		return posicion;
	}

	public void setPosicion(PosicionAnteImpuesto posicion) {
		this.posicion = posicion;
	}

	private String idPuntoVenta;
	
	public String getIdPuntoVenta() {
		return idPuntoVenta;
	}

	public void setIdPuntoVenta(String idPuntoVenta) {
		this.idPuntoVenta = idPuntoVenta;
	}

	private PuntoVenta puntoVenta;
		
	public PuntoVenta getPuntoVenta() {
		if (puntoVenta == null){
			if (!Is.emptyString(this.getIdPuntoVenta())){
				this.puntoVenta = XPersistence.getManager().find(PuntoVenta.class, this.getIdPuntoVenta());
			}
		}
		return puntoVenta;
	}

	public void setPuntoVenta(PuntoVenta puntoVenta) {
		this.puntoVenta = puntoVenta;
	}

	private VentaElectronica venta;
	
	public VentaElectronica getVentaElectronica(){
		return venta;
	}
	
	public void setVentaElectronica(VentaElectronica ventaElectronica) {
		this.venta = ventaElectronica;
	}
	
	public Object calculate() throws Exception {
		// FALTA REGIONALIZAR
		TipoComprobanteArg tipo = null;
		if (this.getPuntoVenta() != null){
			if (this.getPuntoVenta().getTipo().exportacion()){
				tipo = TipoComprobanteArg.E;
			}
			else if (this.getPuntoVenta().empresaAsociada().esMonotributista()){
				tipo = TipoComprobanteArg.C;
			}
		}
		if (tipo == null){
			PosicionAnteImpuesto posicionIVA = this.getPosicion();
			if (posicionIVA != null){		
				tipo = TipoComprobanteArg.B;
				if (posicionIVA.esResponsableInscripto() || posicionIVA.esMonotributista()){
					tipo = TipoComprobanteArg.A;
				} 			
			}
		}
		
		if (tipo == null){
			tipo = TipoComprobanteArg.A;
		}
		
		TipoComprobante tipoComprobante = null;
		if (this.getVentaElectronica() != null){ 
			if (this.getVentaElectronica().revierteTransaccion()){
				tipoComprobante = this.getVentaElectronica().getRevierte().getTipo();
			}
			else if ((this.getPuntoVenta() != null) && (this.getPuntoVenta().getTipo().equals(TipoPuntoVenta.Electronico))){
				tipo = this.evaluarFacturaCreditoAfip(this.getVentaElectronica(), tipo);
			}
		}
			
		if (tipoComprobante == null){
			tipoComprobante = TipoComprobante.buscarPorId(tipo.ordinal());
		}		
		return tipoComprobante;	 	
	}

	private TipoComprobanteArg evaluarFacturaCreditoAfip(VentaElectronica venta, TipoComprobanteArg tipoCompArg) {	
		TipoComprobanteArg tipo = tipoCompArg;
		if (venta.debeAutorizaAfip()){			 
			ConfiguracionAfip configuracionAfip = ConfiguracionAfip.getConfigurador();
			boolean regimenFacturaCredito = configuracionAfip.evaluarRegimenFacturaCreditoElectronico(venta);			
			if (regimenFacturaCredito){
				if (tipoCompArg.equals(TipoComprobanteArg.A)){
					tipo = TipoComprobanteArg.FCE_A;
				}
				else if (tipoCompArg.equals(TipoComprobanteArg.B)){
					tipo = TipoComprobanteArg.FCE_B;
				}
				else if (tipoCompArg.equals(TipoComprobanteArg.C)){
					tipo = TipoComprobanteArg.FCE_C;
				}
				else{
					throw new ValidationException("El regimen de factura de crédito no es válido para el tipo de comprobante " + tipoCompArg.toString());
				}
			}
		}
		return tipo;		
	}
}
