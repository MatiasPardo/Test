package org.openxava.codigobarras.model;

import java.math.BigDecimal;

import org.openxava.inventario.model.Lote;

public interface IItemControlCodigoBarras {

	public BigDecimal getCantidad();
	
	public void setCantidad(BigDecimal cantidad);
	
	public BigDecimal getControlado();
	
	public void setControlado(BigDecimal controlado);
	
	public Lote getLote();
	
	public void setLote(Lote lote);
	
	public boolean crearEntidadesPorControl();
	
	public BigDecimal convertirUnidadesLeidas(BigDecimal cantidadLeida);
}
