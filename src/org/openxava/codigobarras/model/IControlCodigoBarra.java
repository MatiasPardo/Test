package org.openxava.codigobarras.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.openxava.ventas.model.Producto;

public interface IControlCodigoBarra {
	
	public void itemsParaControlarPorCodigoBarra(List<IItemControlCodigoBarras> items, Producto producto, BigDecimal cantidadControlar);

	public boolean permiteCantidadesNegativas();
	
	public IItemControlCodigoBarras crearItemDesdeCodigoBarras(Producto producto, BigDecimal cantidad, String codigoLote, String codigoSerie, Date vencimiento);
	
	public BigDecimal mostrarTotalLectorCodigoBarras();
}
