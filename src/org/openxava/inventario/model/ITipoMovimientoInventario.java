package org.openxava.inventario.model;

public interface ITipoMovimientoInventario {

	boolean requiereAtributosInventario();

	void actualizarStockSinInventario(IItemMovimientoInventario movimiento, Kardex kardex, boolean stockObligatorio);

	boolean actualizarStock(Inventario inv, IItemMovimientoInventario movimiento, Kardex kardex,  boolean stockObligatorio);

	

}
