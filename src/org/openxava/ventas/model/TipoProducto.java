package org.openxava.ventas.model;

public enum TipoProducto {
	Producto(true), Concepto(false), Interes(false);
	
	private boolean manejaStock;
	
	TipoProducto(boolean stock){
		this.manejaStock = stock;
	}
	
	public boolean stock() {
		return manejaStock;
	}	
}
