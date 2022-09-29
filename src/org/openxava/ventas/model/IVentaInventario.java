package org.openxava.ventas.model;

import org.openxava.inventario.model.*;

public interface IVentaInventario {
	
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion);
	
	public Deposito getDeposito();
	
	public boolean validarStockDisponible();
}
