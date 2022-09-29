package org.openxava.inventario.model;


import org.openxava.negocio.model.*;
import org.openxava.ventas.model.*;

public interface IItemMovimientoInventario {

	public Producto getProducto();

	public DespachoImportacion getDespacho();
	
	public void setDespacho(DespachoImportacion despacho);
		
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion);
	
	public Deposito getDeposito();

	public Cantidad cantidadStock();
	
	public void actualizarCantidadItem(Cantidad cantidad);
	
	public UnidadMedida getUnidadMedida();
	
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem);
	
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem);

	public Lote getLote();
	
	public void setLote(Lote lote);
}
