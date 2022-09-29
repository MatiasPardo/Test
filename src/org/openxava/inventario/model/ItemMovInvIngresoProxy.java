package org.openxava.inventario.model;

public class ItemMovInvIngresoProxy extends ItemMovInventarioProxy{

	
	public ItemMovInvIngresoProxy(IItemMovimientoInventario item) {
		super(item);
	}

	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		if (!reversion){
			return new TipoMovInvIngreso();
		}
		else{
			return new TipoMovInvEgreso();
		}
	}

}
