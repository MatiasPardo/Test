package org.openxava.inventario.model;


public class ItemMovInvIngresoReservaProxy extends ItemMovInventarioProxy{
	
	
	public ItemMovInvIngresoReservaProxy(IItemMovimientoInventario item) {
		super(item);
	}

	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		if (!reversion){
			return new TipoMovInvIngresoReserva();
		}
		else{
			return new TipoMovInvEgresoDesreserva();
		}
	}
}
