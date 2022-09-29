package org.openxava.ventas.model;

public enum TipoPuntoVenta {
	Electronico(false, true), Manual(false, false), ExportacionManual(true, false), ExportacionElectronico(true, true);
	
	private boolean expo;
	
	private boolean cae;
	
	public boolean exportacion(){
		return this.expo;
	}
	
	public boolean solicitarCae(){
		return this.cae;
	}
	
	TipoPuntoVenta(boolean expo, boolean cae){
		this.expo = expo;
		this.cae = cae;
	}
}
