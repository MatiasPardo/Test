package org.openxava.afip.model;

import org.openxava.validators.*;

public enum TipoComprobanteArg {
	A("A", false, false), B("B", false, false), C("C", true, false), E("E", false, false), 
	FCE_A("A", false, true), FCE_B("B", false, true), FCE_C("C", true, true), // FCE: factura crédito electrónico
	M("M", false, false); 
	
	private String letra;
		
	public String getLetra() {
		return letra;
	}

	private boolean consumidorFinal;
	
	public boolean getConsumidorFinal(){
		return consumidorFinal;
	}
	
	private boolean regimenFacturaCredito;
	
	public boolean getRegimenFacturaCredito() {		
		return regimenFacturaCredito;
	}
	
	TipoComprobanteArg(String letra, boolean consumidorFinal, boolean regimenFacturaCredito){
		this.letra = letra;
		this.consumidorFinal = consumidorFinal;
		this.regimenFacturaCredito = regimenFacturaCredito;
	}
	
	static public Integer codigoAfipPorIndice(Integer indice, String tipoTransaccion){
		if (indice == null){
			throw new ValidationException("Error tipo de comprobante afip: indice nulo");
		}
		if (tipoTransaccion == null){
			throw new ValidationException("Error tipo de comprobante afip: tipo de comprobante nulo");
		}
		
		switch(indice){
		case 0:
			if (tipoTransaccion.equalsIgnoreCase("FacturaVenta") || tipoTransaccion.equalsIgnoreCase("FacturaVentaContado") || tipoTransaccion.equalsIgnoreCase("FacturaCompra")){
				return 1;
			}
			else if (tipoTransaccion.equalsIgnoreCase("DebitoVenta") || tipoTransaccion.equalsIgnoreCase("DebitoCompra")){
				return 2;
			}
			else if (tipoTransaccion.equalsIgnoreCase("CreditoVenta") || tipoTransaccion.equalsIgnoreCase("CreditoCompra")){
				return 3;
			}
			break;
		case 1:
			if (tipoTransaccion.equalsIgnoreCase("FacturaVenta") || tipoTransaccion.equalsIgnoreCase("FacturaVentaContado") || tipoTransaccion.equalsIgnoreCase("FacturaCompra")){
				return 6;
			}
			else if (tipoTransaccion.equalsIgnoreCase("DebitoVenta") || tipoTransaccion.equalsIgnoreCase("DebitoCompra")){
				return 7;
			}
			else if (tipoTransaccion.equalsIgnoreCase("CreditoVenta") || tipoTransaccion.equalsIgnoreCase("CreditoCompra")){
				return 8;
			}
			break;
		case 2:
			if (tipoTransaccion.equalsIgnoreCase("FacturaVenta") || tipoTransaccion.equalsIgnoreCase("FacturaVentaContado") || tipoTransaccion.equalsIgnoreCase("FacturaCompra")){
				return 11;
			}
			else if (tipoTransaccion.equalsIgnoreCase("DebitoVenta") || tipoTransaccion.equalsIgnoreCase("DebitoCompra")){
				return 12;
			}
			else if (tipoTransaccion.equalsIgnoreCase("CreditoVenta") || tipoTransaccion.equalsIgnoreCase("CreditoCompra")){
				return 13;
			}
			break;
		case 3:
			if (tipoTransaccion.equalsIgnoreCase("FacturaVenta") || tipoTransaccion.equalsIgnoreCase("FacturaVentaContado") || tipoTransaccion.equalsIgnoreCase("FacturaCompra")){
				return 19;
			}
			else if (tipoTransaccion.equalsIgnoreCase("DebitoVenta") || tipoTransaccion.equalsIgnoreCase("DebitoCompra")){
				return 20;
			}
			else if (tipoTransaccion.equalsIgnoreCase("CreditoVenta") || tipoTransaccion.equalsIgnoreCase("CreditoCompra")){
				return 21;
			}
			break;
		case 4:
			if (tipoTransaccion.equalsIgnoreCase("FacturaVenta") || tipoTransaccion.equalsIgnoreCase("FacturaVentaContado") || tipoTransaccion.equalsIgnoreCase("FacturaCompra")){
				return 201;
			}
			else if (tipoTransaccion.equalsIgnoreCase("DebitoVenta") || tipoTransaccion.equalsIgnoreCase("DebitoCompra")){
				return 202;
			}
			else if (tipoTransaccion.equalsIgnoreCase("CreditoVenta") || tipoTransaccion.equalsIgnoreCase("CreditoCompra")){
				return 203;
			}
			break;
		case 5:
			if (tipoTransaccion.equalsIgnoreCase("FacturaVenta") || tipoTransaccion.equalsIgnoreCase("FacturaVentaContado") || tipoTransaccion.equalsIgnoreCase("FacturaCompra")){
				return 206;
			}
			else if (tipoTransaccion.equalsIgnoreCase("DebitoVenta") || tipoTransaccion.equalsIgnoreCase("DebitoCompra")){
				return 207;
			}
			else if (tipoTransaccion.equalsIgnoreCase("CreditoVenta") || tipoTransaccion.equalsIgnoreCase("CreditoCompra")){
				return 208;
			}
			break;
		case 6:
			if (tipoTransaccion.equalsIgnoreCase("FacturaVenta") || tipoTransaccion.equalsIgnoreCase("FacturaVentaContado") || tipoTransaccion.equalsIgnoreCase("FacturaCompra")){
				return 211;
			}
			else if (tipoTransaccion.equalsIgnoreCase("DebitoVenta") || tipoTransaccion.equalsIgnoreCase("DebitoCompra")){
				return 212;
			}
			else if (tipoTransaccion.equalsIgnoreCase("CreditoVenta") || tipoTransaccion.equalsIgnoreCase("CreditoCompra")){
				return 213;
			}
			break;
		case 7: // M
			if (tipoTransaccion.equalsIgnoreCase("FacturaVenta") || tipoTransaccion.equalsIgnoreCase("FacturaVentaContado") || tipoTransaccion.equalsIgnoreCase("FacturaCompra")){
				return 51;
			}
			else if (tipoTransaccion.equalsIgnoreCase("DebitoVenta") || tipoTransaccion.equalsIgnoreCase("DebitoCompra")){
				return 52;
			}
			else if (tipoTransaccion.equalsIgnoreCase("CreditoVenta") || tipoTransaccion.equalsIgnoreCase("CreditoCompra")){
				return 53;
			}
			break;	
	}		
		throw new ValidationException("Tipo de comprobante AFIP Incorrecto: " + indice.toString() + " - " + tipoTransaccion.toString());
	}
	
	public static String descripcionTipoComprobantePorCodigoAfip(Integer codigoAfip){
		switch(codigoAfip){
			case 1: return "Factura A";
			case 2: return "Débito A";
			case 3: return "Crédito A";
			case 4: return "No utilizados";
			case 5: return "No utilizados";
			case 6: return "Factura B";
			case 7: return "Débito B";
			case 8: return "Crédito B";
			case 11: return "Factura C";
			case 12: return "Débito C";
			case 13: return "Crédito C";
			case 19: return "Factura E";
			case 20: return "Débito E";
			case 21: return "Crédito E";
			case 201: return "Factura (FCE) A";
			case 202: return "Débito (FCE) A";
			case 203: return "Crédito (FCE) A";
			case 206: return "Factura (FCE) B";
			case 207: return "Débito (FCE) B";
			case 208: return "Crédito (FCE) B";
			case 211: return "Factura (FCE) C";
			case 212: return "Débito (FCE) C";
			case 213: return "Crédito (FCE) C";
			default: return "";
		}	
	}
	
	public boolean discriminaIVA(){
		if (this.equals(TipoComprobanteArg.A) || this.equals(TipoComprobanteArg.FCE_A)){
			return true;
		}
		else{
			return false;
		}
	}
}