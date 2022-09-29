package org.openxava.negocio.model;

import org.openxava.util.Is;
import org.openxava.validators.*;

public enum TipoDocumento {
	CUIL(86), CUIT(80), DNI(96);
	
	static public Integer codigoAfipPorIndice(Integer indice){
		switch(indice){
		case 0:
			return TipoDocumento.CUIL.getCodigoAfip();
		case 1:
			return TipoDocumento.CUIT.getCodigoAfip();
		case 2:
			return TipoDocumento.DNI.getCodigoAfip();
		}
		throw new ValidationException("Tipo de Documento de Afip no encontrodo: " + indice.toString());
	}
	
	static public TipoDocumento tipoDocumentoPorCodigoAfip(Integer codigoAfip){
		for(TipoDocumento tipo: TipoDocumento.values()){
			if (tipo.getCodigoAfip().equals(codigoAfip)){
				return tipo;
			}
		}
		throw new ValidationException("No se encontró tipo de documento, para el código de afip: " + codigoAfip.toString());
	}
	
	static public TipoDocumento tipoDocumentoPorCodigoMercadoLibre(String codigoMercadoLibre){
		for(TipoDocumento tipo: TipoDocumento.values()){
			if (Is.equalAsStringIgnoreCase(tipo.toString(), codigoMercadoLibre)){
				return tipo;
			}
		}
		throw new ValidationException("No se encontró tipo de documento mercado libre " + codigoMercadoLibre);
	}
	
	private Integer codigoAfip;
	
	private TipoDocumento(Integer value){
		this.setCodigoAfip(value);
	}
	
	public Integer getCodigoAfip() {
		return codigoAfip;
	}
	public void setCodigoAfip(Integer codigoAfip) {
		this.codigoAfip = codigoAfip;
	}
}
